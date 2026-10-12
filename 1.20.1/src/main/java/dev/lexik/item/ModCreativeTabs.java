package dev.lexik.item;

import dev.lexik.FlashlightX;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FlashlightX.MOD_ID);

    public static final RegistryObject<CreativeModeTab> FLASHLIGHTX =
            TABS.register("flashlightx", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.flashlightx"))
                    .icon(() -> new ItemStack(ModItems.FLASHLIGHT.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FLASHLIGHT.get());
                        output.accept(ModItems.BATTERY.get());
                    })
                    .build());
}
