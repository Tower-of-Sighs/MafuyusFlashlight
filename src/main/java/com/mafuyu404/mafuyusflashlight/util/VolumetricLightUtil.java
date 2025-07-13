package com.mafuyu404.mafuyusflashlight.util;

import com.mafuyu404.mafuyusflashlight.Item.FlashlightItem;
import com.mafuyu404.mafuyusflashlight.render.VolumetricLightRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class VolumetricLightUtil {

    public static Vec3 calculateFlashlightPosition(Player player) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 upVec = rightVec.cross(lookVec).normalize();

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isMainHand = mainHand.getItem() instanceof FlashlightItem && FlashlightItem.getPowered(mainHand) == 1;
        boolean isOffHand = offHand.getItem() instanceof FlashlightItem && FlashlightItem.getPowered(offHand) == 1;

        Vec3 handOffset = isMainHand ? rightVec.scale(0.25).add(upVec.scale(-0.15)).add(lookVec.scale(0.3)) :
                isOffHand ? rightVec.scale(-0.25).add(upVec.scale(-0.15)).add(lookVec.scale(0.3)) :
                        rightVec.scale(0.25).add(upVec.scale(-0.15)).add(lookVec.scale(0.3));

        if (player.isCrouching()) {
            handOffset = handOffset.add(0, -0.15, 0);
        }

        return eyePos.add(handOffset);
    }

    public static Vec3 calculateFlashlightDirection(Player player) {
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 adjustedUp = rightVec.cross(lookVec).normalize();

        double tiltAngle = Math.toRadians(-5);
        Vec3 tilted = lookVec.scale(Math.cos(tiltAngle)).add(adjustedUp.scale(Math.sin(tiltAngle)));
        return tilted.normalize();
    }

    public static boolean shouldRenderLight(VolumetricLightRenderer.FlashlightSource source, Vec3 cameraPos, Player cameraPlayer) {
        double distance = source.position.distanceTo(cameraPos);
        if (distance > source.range * 1.5) return false;

        if (source.player == cameraPlayer && Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return true;

        Vec3 toCameraDir = cameraPos.subtract(source.position).normalize();
        double dot = source.direction.dot(toCameraDir);
        double maxAngle = Math.cos(Math.toRadians(source.coneAngle * 0.5 + 60));
        return dot > maxAngle;
    }

    public static void addVertex(BufferBuilder builder, Vec3 pos, float r, float g, float b, float a, float u, float v) {
        builder.vertex(pos.x, pos.y, pos.z)
                .color(r, g, b, a)
                .uv(u, v)
                .uv2(240, 240)
                .endVertex();
    }
}
