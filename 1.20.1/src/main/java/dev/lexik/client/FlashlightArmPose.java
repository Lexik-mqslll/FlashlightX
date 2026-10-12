package dev.lexik.client;

import dev.lexik.config.FlashlightConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Extra ArmPose that raises the arm holding the flashlight. Display transforms
 * only move the item inside the hand, so the limb itself is posed here.
 */
public class FlashlightArmPose {

    public static final HumanoidModel.ArmPose HOLDING = HumanoidModel.ArmPose.create(
            "FLASHLIGHTX_HOLDING", false, FlashlightArmPose::apply);

    /** Forces the class to initialise so the enum constant exists. */
    public static void register() {
        if (HOLDING == null) {
            throw new IllegalStateException("flashlight arm pose failed to register");
        }
    }

    private static void apply(HumanoidModel<?> model, net.minecraft.world.entity.LivingEntity entity,
                              HumanoidArm arm) {
        ModelPart limb = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        limb.xRot = (float) Math.toRadians(FlashlightConfig.CLIENT.armAngle.get());
        limb.yRot = 0.0f;
        limb.zRot = 0.0f;
    }
}
