package com.mafuyu404.mafuyusflashlight.init;

import com.mafuyu404.mafuyusflashlight.Item.FlashlightItem;
import com.mafuyu404.mafuyusflashlight.compat.CuriosCompat;
import com.mafuyu404.mafuyusflashlight.network.FlashlightSyncPacket;
import com.mafuyu404.mafuyusflashlight.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

public class Utils {
    public static boolean isUsingFlashlight(Player player) {
        boolean result = false;
        if (player.getMainHandItem().getItem() instanceof FlashlightItem) {
            result = FlashlightItem.getPowered(player.getMainHandItem()) == 1;
        }
        if (player.getOffhandItem().getItem() instanceof FlashlightItem) {
            result = FlashlightItem.getPowered(player.getOffhandItem()) == 1;
        }
        return result || CuriosCompat.isUsingCuriosFlashlight(player);
    }

    public static boolean isFlashlight(ItemStack itemStack) {
        return itemStack.getItem() instanceof FlashlightItem;
    }

    public static void toggleFlashlight(Player player) {
        boolean wasUsing = isUsingFlashlight(player);

        if (player.getMainHandItem().getItem() instanceof FlashlightItem) {
            FlashlightItem.togglePowered(player.getMainHandItem());
        }
        if (player.getOffhandItem().getItem() instanceof FlashlightItem) {
            FlashlightItem.togglePowered(player.getOffhandItem());
        }
        CuriosCompat.toggleCuriosFlashlight(player);

        boolean isNowUsing = isUsingFlashlight(player);

        // 如果状态发生变化，发送网络包
        if (wasUsing != isNowUsing) {
            if (player instanceof ServerPlayer serverPlayer) {
                // 服务器端：发送给所有客户端
                FlashlightSyncPacket packet = new FlashlightSyncPacket(player.getUUID(), isNowUsing);
                NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
            } else if (player.level().isClientSide) {
                // 客户端：发送给服务器
                FlashlightSyncPacket packet = new FlashlightSyncPacket(player.getUUID(), isNowUsing);
                NetworkHandler.INSTANCE.sendToServer(packet);
            }
        }
    }
}