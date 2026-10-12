package dev.lexik.event;

import dev.lexik.FlashlightX;
import dev.lexik.config.FlashlightConfig;
import dev.lexik.item.FlashlightItem;
import dev.lexik.item.ModItems;
import dev.lexik.light.FlashlightLightManager;
import dev.lexik.light.LightCone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Drives the lighting each client tick. The server only syncs the on/off state
 * of the item; every client computes and draws the beams itself.
 */
@Mod.EventBusSubscriber(modid = FlashlightX.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    private static final FlashlightLightManager lightManager = new FlashlightLightManager();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player self = mc.player;
        ClientLevel level = mc.level;

        // Guard: no world or player loaded
        if (self == null || level == null) {
            return;
        }

        List<Player> sources = collectSources(level, self);

        // Nobody nearby has a lit flashlight → clear all lights
        if (sources.isEmpty()) {
            if (lightManager.hasActiveLights()) {
                lightManager.clearAll(level);
            }
            return;
        }

        // Merge the beams, brightest value wins per position
        Map<BlockPos, Integer> combined = new LinkedHashMap<>();
        for (Player source : sources) {
            for (Map.Entry<BlockPos, Integer> entry : LightCone.compute(source).entrySet()) {
                combined.merge(entry.getKey(), entry.getValue(), Math::max);
            }
        }

        lightManager.update(level, combined);
    }

    /** Nearby players holding a lit flashlight, nearest first and capped. */
    private static List<Player> collectSources(ClientLevel level, Player self) {
        FlashlightConfig.Client config = FlashlightConfig.CLIENT;
        boolean others = config.showOtherPlayers.get();
        double range = config.maxSourceDistance.get();
        int cap = config.maxSources.get();

        List<Player> sources = new ArrayList<>();

        for (AbstractClientPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            if (player != self) {
                if (!others) continue;
                if (player.distanceToSqr(self) > range * range) continue;
            }
            if (!hasLitFlashlight(player)) continue;

            sources.add(player);
        }

        if (sources.size() > cap) {
            sources.sort((a, b) -> Double.compare(a.distanceToSqr(self), b.distanceToSqr(self)));
            return sources.subList(0, cap);
        }

        return sources;
    }

    /** Whether either hand holds a lit flashlight. */
    private static boolean hasLitFlashlight(Player player) {
        return isLitFlashlight(player.getItemInHand(InteractionHand.MAIN_HAND))
                || isLitFlashlight(player.getItemInHand(InteractionHand.OFF_HAND));
    }

    private static boolean isLitFlashlight(ItemStack stack) {
        return stack.is(ModItems.FLASHLIGHT.get()) && FlashlightItem.isOn(stack);
    }
}
