package com.mafuyu404.mafuyusflashlight.network;

import com.mafuyu404.mafuyusflashlight.Mafuyusflashlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mafuyusflashlight.MODID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.messageBuilder(FlashlightSyncPacket.class, id++)
                .encoder(FlashlightSyncPacket::encode)
                .decoder(FlashlightSyncPacket::new)
                .consumerMainThread(FlashlightSyncPacket::handle)
                .add();
    }
}