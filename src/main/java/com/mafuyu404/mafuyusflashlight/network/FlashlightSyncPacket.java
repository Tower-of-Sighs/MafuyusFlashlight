package com.mafuyu404.mafuyusflashlight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class FlashlightSyncPacket {
    private final UUID playerId;
    private final boolean isOn;

    public FlashlightSyncPacket(UUID playerId, boolean isOn) {
        this.playerId = playerId;
        this.isOn = isOn;
    }

    public FlashlightSyncPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.isOn = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeBoolean(isOn);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 在客户端处理其他玩家的手电筒状态
            if (context.getDirection().getReceptionSide().isClient()) {
                FlashlightStateManager.setPlayerFlashlightState(playerId, isOn);
            }
        });
        context.setPacketHandled(true);
    }
}