package com.mafuyu404.mafuyusflashlight.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping("key.mafuyusflashlight.flashlight_switch.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.mafuyusflashlight"
    );
}
