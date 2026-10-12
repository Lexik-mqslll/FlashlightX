package dev.lexik.client;

import dev.lexik.config.FlashlightConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Client side of the flashlight item, kept apart from the item class so a
 * dedicated server never loads these types.
 */
public class FlashlightClientExtensions {

    private static FlashlightRenderer renderer;

    public static void accept(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand,
                                                    ItemStack stack) {
                // Raise the arm instead of letting it hang down.
                if (!FlashlightConfig.CLIENT.raiseArm.get()) {
                    return HumanoidModel.ArmPose.ITEM;
                }
                return FlashlightArmPose.HOLDING;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new FlashlightRenderer();
                }
                return renderer;
            }
        });
    }
}
