package com.mafuyu404.mafuyusflashlight.init;

import com.mafuyu404.mafuyusflashlight.Item.FlashlightItem;
import com.mafuyu404.mafuyusflashlight.compat.CuriosCompat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
        if (player.getMainHandItem().getItem() instanceof FlashlightItem) {
            FlashlightItem.togglePowered(player.getMainHandItem());
        }
        if (player.getOffhandItem().getItem() instanceof FlashlightItem) {
            FlashlightItem.togglePowered(player.getOffhandItem());
        }
        CuriosCompat.toggleCuriosFlashlight(player);

    }
}
