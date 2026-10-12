package dev.lexik.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "flashlightx");

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("flashlightx");

    // Whether the flashlight is switched on
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FLASHLIGHT_ON =
            COMPONENTS.register("flashlight_on",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build());

    // Ticks of light left in the loaded battery
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            COMPONENTS.register("charge",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    public static final DeferredItem<FlashlightItem> FLASHLIGHT =
            ITEMS.registerItem("flashlight",
                    FlashlightItem::new,
                    new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> BATTERY =
            ITEMS.registerItem("battery",
                    Item::new,
                    new Item.Properties().stacksTo(8));
}