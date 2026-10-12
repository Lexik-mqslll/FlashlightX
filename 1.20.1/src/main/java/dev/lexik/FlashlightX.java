package dev.lexik;

import dev.lexik.config.FlashlightConfig;
import dev.lexik.item.ModCreativeTabs;
import dev.lexik.item.ModItems;
import dev.lexik.sound.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FlashlightX.MOD_ID)
public class FlashlightX {

    public static final String MOD_ID = "flashlightx";

    public FlashlightX() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.CLIENT, FlashlightConfig.CLIENT_SPEC);
        context.registerConfig(ModConfig.Type.COMMON, FlashlightConfig.COMMON_SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }
}
