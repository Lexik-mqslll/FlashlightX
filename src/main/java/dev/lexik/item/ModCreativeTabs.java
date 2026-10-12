package dev.lexik.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "flashlightx");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FLASHLIGHTX =
            TABS.register("flashlightx", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.flashlightx"))
                    .icon(() -> new ItemStack(ModItems.FLASHLIGHT.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FLASHLIGHT.get());
                        output.accept(ModItems.BATTERY.get());
                    })
                    .build());
}
