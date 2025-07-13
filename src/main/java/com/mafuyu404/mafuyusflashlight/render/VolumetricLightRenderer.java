package com.mafuyu404.mafuyusflashlight.render;

import com.mafuyu404.mafuyusflashlight.init.Utils;
import com.mafuyu404.mafuyusflashlight.network.FlashlightStateManager;
import com.mafuyu404.mafuyusflashlight.registry.Config;
import com.mafuyu404.mafuyusflashlight.util.VolumetricLightUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VolumetricLightRenderer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final List<FlashlightSource> lightSources = new CopyOnWriteArrayList<>();
    private static VertexBuffer coneVertexBuffer;
    private static boolean meshInitialized = false;

    public static class FlashlightSource {
        public Vec3 position, direction;
        public float intensity, range, coneAngle;
        public Player player;

        public FlashlightSource(Player player, Vec3 position, Vec3 direction, float intensity, float range, float coneAngle) {
            this.player = player;
            this.position = position;
            this.direction = direction;
            this.intensity = intensity;
            this.range = range;
            this.coneAngle = coneAngle;
        }
    }

    public static void init() {
        if (!meshInitialized) {
            createConeMesh();
            meshInitialized = true;
        }
    }

    public static void cleanup() {
        if (coneVertexBuffer != null) {
            coneVertexBuffer.close();
            coneVertexBuffer = null;
        }
        meshInitialized = false;
    }

    public static void updateLightSources() {
        lightSources.clear();
        if (mc.level == null) return;

        Player localPlayer = mc.player;
        if (localPlayer != null && Utils.isUsingFlashlight(localPlayer)) {
            addFlashlightSource(localPlayer);
        }

        for (Player player : mc.level.players()) {
            if (player != localPlayer && FlashlightStateManager.isPlayerUsingFlashlight(player.getUUID())) {
                addFlashlightSource(player);
            }
        }
    }

    private static void addFlashlightSource(Player player) {
        Vec3 pos = VolumetricLightUtil.calculateFlashlightPosition(player);
        Vec3 dir = VolumetricLightUtil.calculateFlashlightDirection(player);
        lightSources.add(new FlashlightSource(
                player, pos, dir,
                Config.LIGHT_INTENSITY.get().floatValue() * 6.8f,
                Config.VOLUMETRIC_RANGE.get().floatValue(),
                Config.VOLUMETRIC_CONE_ANGLE.get().floatValue()
        ));
    }

    public static void render(PoseStack poseStack, float partialTick) {
        if (lightSources.isEmpty() || !meshInitialized) return;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        List<FlashlightSource> visible = lightSources.stream()
                .filter(src -> VolumetricLightUtil.shouldRenderLight(src, cameraPos, mc.player))
                .toList();
        if (visible.isEmpty()) return;

        ShaderInstance shader = ModShaders.getVolumetricLightShader();
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        shader.apply();

        for (FlashlightSource source : visible) {
            renderVolumetricCone(poseStack, source, partialTick, shader);
        }

        shader.clear();

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void renderVolumetricCone(PoseStack poseStack, FlashlightSource source, float partialTick, ShaderInstance shader) {
        poseStack.pushPose();

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 relPos = source.position.subtract(cameraPos);
        poseStack.translate(relPos.x, relPos.y, relPos.z);

        float yaw = (float) Math.atan2(source.direction.x, source.direction.z);
        float pitch = (float) Math.asin(-source.direction.y);
        poseStack.mulPose(Axis.YP.rotation(yaw));
        poseStack.mulPose(Axis.XP.rotation(pitch));

        float scale = source.range;
        poseStack.scale(scale, scale, scale);

        Matrix4f modelView = poseStack.last().pose();

        try {
            if (shader.getUniform("ModelViewMat") != null)
                shader.getUniform("ModelViewMat").set(modelView);
            if (shader.getUniform("ProjMat") != null)
                shader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
            if (shader.getUniform("LightPosition") != null)
                shader.getUniform("LightPosition").set((float) source.position.x, (float) source.position.y, (float) source.position.z);
            if (shader.getUniform("LightDirection") != null)
                shader.getUniform("LightDirection").set((float) source.direction.x, (float) source.direction.y, (float) source.direction.z);
            if (shader.getUniform("LightIntensity") != null)
                shader.getUniform("LightIntensity").set(source.intensity);
            if (shader.getUniform("LightRange") != null)
                shader.getUniform("LightRange").set(source.range);
            if (shader.getUniform("ConeAngle") != null)
                shader.getUniform("ConeAngle").set(source.coneAngle);
            if (shader.getUniform("Time") != null)
                shader.getUniform("Time").set((float) (System.currentTimeMillis() % 100000) / 1000f);
        } catch (Exception ignored) {
        }

        if (coneVertexBuffer != null) {
            coneVertexBuffer.bind();
            coneVertexBuffer.draw();
        }

        poseStack.popPose();
    }

    private static void createConeMesh() {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        int segments = 32;
        float height = 1.0f;
        float radius = (float) Math.tan(Math.toRadians(22.5));
        Vec3 apex = new Vec3(0, 0, 0);

        Vec3[] base = new Vec3[segments];
        for (int i = 0; i < segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            float x = radius * Mth.cos(angle);
            float y = radius * Mth.sin(angle);
            base[i] = new Vec3(x, y, height);
        }

        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            VolumetricLightUtil.addVertex(builder, apex, 1f, 1f, 0.9f, 0.6f, 0.5f, 0f);
            VolumetricLightUtil.addVertex(builder, base[i], 1f, 1f, 0.9f, 0.15f, 0f, 1f);
            VolumetricLightUtil.addVertex(builder, base[next], 1f, 1f, 0.9f, 0.15f, 1f, 1f);
        }

        Vec3 center = new Vec3(0, 0, height);
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            VolumetricLightUtil.addVertex(builder, center, 1f, 1f, 0.9f, 0.05f, 0.5f, 0.5f);
            VolumetricLightUtil.addVertex(builder, base[i], 1f, 1f, 0.9f, 0.05f, 0f, 1f);
            VolumetricLightUtil.addVertex(builder, base[next], 1f, 1f, 0.9f, 0.05f, 1f, 1f);
        }

        coneVertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        coneVertexBuffer.bind();
        coneVertexBuffer.upload(builder.end());
        VertexBuffer.unbind();
    }
}
