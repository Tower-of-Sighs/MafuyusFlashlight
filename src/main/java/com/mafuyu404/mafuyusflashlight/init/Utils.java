package com.mafuyu404.mafuyusflashlight.init;

import com.mafuyu404.mafuyusflashlight.Item.FlashlightItem;
import com.mafuyu404.mafuyusflashlight.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class Utils {
    public static boolean isUsingFlashlight(Player player) {
        boolean result = false;
        if (player.getMainHandItem().getItem() instanceof FlashlightItem) {
            result = FlashlightItem.getPowered(player.getMainHandItem()) == 1;
        }
        if (player.getOffhandItem().getItem() instanceof FlashlightItem) {
            result = FlashlightItem.getPowered(player.getMainHandItem()) == 1;
        }
        return result;
    }
}
