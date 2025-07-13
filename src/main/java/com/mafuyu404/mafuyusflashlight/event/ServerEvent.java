package com.mafuyu404.mafuyusflashlight.event;

import com.mafuyu404.mafuyusflashlight.Mafuyusflashlight;
import com.mafuyu404.mafuyusflashlight.init.Utils;
import com.mafuyu404.mafuyusflashlight.network.FlashlightSyncPacket;
import com.mafuyu404.mafuyusflashlight.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Mafuyusflashlight.MODID)
public class ServerEvent {
    private static final Map<UUID, Boolean> lastFlashlightStates = new HashMap<>();
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        // 检查所有玩家的手电筒状态变化
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            boolean currentState = Utils.isUsingFlashlight(player);
            Boolean lastState = lastFlashlightStates.get(playerId);
            
            // 如果状态发生变化，同步给所有客户端
            if (lastState == null || lastState != currentState) {
                lastFlashlightStates.put(playerId, currentState);
                
                // 发送给所有玩家（包括自己）
                FlashlightSyncPacket packet = new FlashlightSyncPacket(playerId, currentState);
                NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
            }
        }
    }
}