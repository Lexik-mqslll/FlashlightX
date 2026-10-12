package dev.lexik.client;

import dev.lexik.FlashlightX;
import dev.lexik.item.FlashlightItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Resource locations of the geometry, texture and animation files. */
public class FlashlightModel extends GeoModel<FlashlightItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(FlashlightX.MOD_ID, "geo/flashlight.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FlashlightX.MOD_ID, "textures/item/flashlight_texture.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(FlashlightX.MOD_ID, "animations/flashlight.animation.json");

    @Override
    public ResourceLocation getModelResource(FlashlightItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FlashlightItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FlashlightItem animatable) {
        return ANIMATION;
    }
}
