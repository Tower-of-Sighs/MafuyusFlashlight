package com.mafuyu404.mafuyusflashlight.event;

import com.mafuyu404.mafuyusflashlight.Mafuyusflashlight;
import com.mafuyu404.mafuyusflashlight.init.ShaderManager;
import com.mafuyu404.mafuyusflashlight.init.Utils;
import com.mafuyu404.mafuyusflashlight.registry.Config;
import com.mafuyu404.mafuyusflashlight.registry.KeyBindings;
import com.mafuyu404.mafuyusflashlight.render.VolumetricLightRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Mafuyusflashlight.MODID, value = Dist.CLIENT)
public class ClientEvent {
    private static float previousYaw = 0.0f;
    private static float previousPitch = 0.0f;
    private static float currentOffsetX = 0.0f;
    private static float currentOffsetY = 0.0f;
    private static long lastTickTime = System.currentTimeMillis();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastTickTime;
        float deltaSeconds = Math.max((deltaTime / 1000.0f), 0.001f);
        lastTickTime = currentTime;

        boolean isUsingFlashlight = Utils.isUsingFlashlight(player);

        if (isUsingFlashlight) {
            // 根据配置选择渲染模式
            if (Config.USE_VOLUMETRIC_LIGHTING.get()) {
                // 使用体积光照模式
                ShaderManager.clean("flashlight"); // 清理后处理着色器
                // 体积光照在VolumetricRenderEvent中处理
            } else {
                // 使用传统后处理模式
                ShaderManager.loadShader("flashlight", "shaders/post/flashlight.json");

                // 视角变化量
                float currentYaw = player.getYRot();
                float currentPitch = player.getXRot();
                float deltaYaw = Mth.wrapDegrees(currentYaw - previousYaw);
                float deltaPitch = Mth.wrapDegrees(currentPitch - previousPitch);
                previousYaw = currentYaw;
                previousPitch = currentPitch;

                float sensitivity = 70.0f;
                // 反向偏移
                float offsetDeltaX = -deltaYaw * sensitivity * deltaSeconds;
                float offsetDeltaY = -deltaPitch * sensitivity * deltaSeconds;

                // 衰减
                currentOffsetX = currentOffsetX * 0.5f + offsetDeltaX;
                currentOffsetY = currentOffsetY * 0.5f + offsetDeltaY;

                if (mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT || Config.ENABLE_FIXED_FLASHLIGHT.get()) {
                    currentOffsetX = 0;
                    currentOffsetY = 0;
                }

                float radius = mc.getWindow().getHeight() * 0.48f;
                if (mc.options.getCameraType() != CameraType.FIRST_PERSON) radius /= 2;
                float finalRadius = radius;

                ShaderManager.getShader("flashlight").forEach(postPass -> {
                    EffectInstance effect = postPass.getEffect();
                    if (effect.getName().equals("mafuyusflashlight:flashlight")) {
                        effect.safeGetUniform("Offset").set(currentOffsetX, -currentOffsetY);
                        effect.safeGetUniform("Radius").set(finalRadius);
                        effect.safeGetUniform("IntensityAmount").set(Config.LIGHT_INTENSITY.get().floatValue());
                    }
                });
            }
        } else {
            ShaderManager.clean("flashlight");

            currentOffsetX = 0.0f;
            currentOffsetY = 0.0f;
            previousYaw = player.getYRot();
            previousPitch = player.getXRot();
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (event.getKey() == KeyBindings.FLASHLIGHT_SWITCH.getKey().getValue()) {
            if (event.getAction() == InputConstants.PRESS) {
                Utils.toggleFlashlight(player);
            }
        }
    }
}