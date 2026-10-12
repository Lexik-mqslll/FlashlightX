package dev.lexik;

import dev.lexik.config.FlashlightConfig;
import dev.lexik.item.ModCreativeTabs;
import dev.lexik.item.ModItems;
import dev.lexik.sound.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(FlashlightX.MOD_ID)
public class FlashlightX {

    public static final String MOD_ID = "flashlightx";

    public FlashlightX(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, FlashlightConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, FlashlightConfig.COMMON_SPEC);
    }
}