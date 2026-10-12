package dev.lexik.client;

import dev.lexik.config.FlashlightConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

/**
 * Extra ArmPose that raises the arm holding the flashlight. Display transforms
 * only move the item inside the hand, so the limb itself is posed here.
 * The enum entry is declared in META-INF/enumextensions.json.
 */
public class FlashlightArmPose {

    public static final EnumProxy<HumanoidModel.ArmPose> HOLDING = new EnumProxy<>(
            HumanoidModel.ArmPose.class, false,
            (IArmPoseTransformer) FlashlightArmPose::apply);

    private static void apply(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart limb = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        limb.xRot = (float) Math.toRadians(FlashlightConfig.CLIENT.armAngle.get());
        limb.yRot = 0.0f;
        limb.zRot = 0.0f;
    }
}
