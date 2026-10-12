package dev.lexik.sound;

import dev.lexik.FlashlightX;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FlashlightX.MOD_ID);

    public static final RegistryObject<SoundEvent> FLASHLIGHT_ON = register("flashlight_on");
    public static final RegistryObject<SoundEvent> FLASHLIGHT_OFF = register("flashlight_off");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        new ResourceLocation(FlashlightX.MOD_ID, name)));
    }
}
