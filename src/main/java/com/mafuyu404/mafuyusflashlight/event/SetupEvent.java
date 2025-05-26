package com.mafuyu404.mafuyusflashlight.event;

import com.mafuyu404.mafuyusflashlight.Item.FlashlightItem;
import com.mafuyu404.mafuyusflashlight.Mafuyusflashlight;
import com.mafuyu404.mafuyusflashlight.compat.CuriosCompat;
import com.mafuyu404.mafuyusflashlight.compat.FlashlightRender;
import com.mafuyu404.mafuyusflashlight.registry.KeyBindings;
import com.mafuyu404.mafuyusflashlight.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.mafuyu404.mafuyusflashlight.Mafuyusflashlight.MODID;

@Mod.EventBusSubscriber(modid = Mafuyusflashlight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SetupEvent {
    @SubscribeEvent
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.FLASHLIGHT.get(),
                    new ResourceLocation(MODID, "powered"),
                    (stack, world, entity, seed) -> FlashlightItem.getPowered(stack)
            );
        });
        event.enqueueWork(CuriosCompat::init);
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.FLASHLIGHT_SWITCH);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CuriosCompat::init);
    }
}
