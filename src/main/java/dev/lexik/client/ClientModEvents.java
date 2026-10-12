package dev.lexik.client;

import dev.lexik.config.FlashlightConfig;
import dev.lexik.item.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = "flashlightx", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private FlashlightRenderer renderer;

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand,
                                                    ItemStack stack) {
                // Raise the arm instead of letting it hang down.
                if (!FlashlightConfig.CLIENT.raiseArm.get()) {
                    return HumanoidModel.ArmPose.ITEM;
                }
                return FlashlightArmPose.HOLDING.getValue();
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new FlashlightRenderer();
                }
                return this.renderer;
            }
        }, ModItems.FLASHLIGHT.get());
    }
}
