package com.mafuyu404.mafuyusflashlight;

import com.mafuyu404.mafuyusflashlight.network.NetworkHandler;
import com.mafuyu404.mafuyusflashlight.registry.Config;
import com.mafuyu404.mafuyusflashlight.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Mafuyusflashlight.MODID)
public class Mafuyusflashlight {

    public static final String MODID = "mafuyusflashlight";

    public Mafuyusflashlight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);

        // 注册网络
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                Config.SPEC
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }
}