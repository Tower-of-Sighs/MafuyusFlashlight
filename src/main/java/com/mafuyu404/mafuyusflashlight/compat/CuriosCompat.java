package com.mafuyu404.mafuyusflashlight.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public class CuriosCompat {
    private static final String MOD_ID = "curios";
    private static boolean INSTALLED = false;

    public static void init() {
        INSTALLED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isUsingCuriosFlashlight(Player player) {
        if (INSTALLED) {
            return CuriosCompatInner.isUsingCuriosFlashlight(player);
        }
        return false;
    }

    public static void toggleCuriosFlashlight(Player player) {
        if (INSTALLED) {
            CuriosCompatInner.toggleCuriosFlashlight(player);
        }
    }
}
