package com.mafuyu404.mafuyusflashlight.registry;

import com.mafuyu404.mafuyusflashlight.Item.FlashlightItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.mafuyu404.mafuyusflashlight.Mafuyusflashlight.MODID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> FLASHLIGHT = ITEMS.register("flashlight", FlashlightItem::new);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> ITEM_GROUP = CREATIVE_MODE_TABS.register(MODID, () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.tab.mafuyusflashlight")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> FLASHLIGHT.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(FLASHLIGHT.get());
    }).build());
}
