package dev.lexik.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lexik.config.FlashlightConfig;
import dev.lexik.item.FlashlightItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Renders the flashlight model, drawing the lens full-bright while it is lit. */
public class FlashlightRenderer extends GeoItemRenderer<FlashlightItem> {

    private static final String BONE_LENS = "Flashlight_Lens";

    public FlashlightRenderer() {
        super(new FlashlightModel());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, FlashlightItem animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        if (BONE_LENS.equals(bone.getName()) && FlashlightConfig.CLIENT.glowingLens.get()) {
            ItemStack stack = getCurrentItemStack();

            if (stack != null && FlashlightItem.isOn(stack)) {
                // Ignore world lighting so the lens stays bright.
                packedLight = LightTexture.FULL_BRIGHT;
            }
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
