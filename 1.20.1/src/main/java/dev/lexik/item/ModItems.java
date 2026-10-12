package dev.lexik.item;

import dev.lexik.FlashlightX;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FlashlightX.MOD_ID);

    public static final RegistryObject<FlashlightItem> FLASHLIGHT =
            ITEMS.register("flashlight",
                    () -> new FlashlightItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BATTERY =
            ITEMS.register("battery",
                    () -> new Item(new Item.Properties().stacksTo(8)));
}
