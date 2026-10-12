package dev.lexik.client;

import dev.lexik.FlashlightX;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = FlashlightX.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Must run before the first render: vanilla's switch over ArmPose builds
        // a lookup table sized from values().length the first time it executes,
        // and a constant added after that falls outside the table.
        event.enqueueWork(FlashlightArmPose::register);
    }
}
