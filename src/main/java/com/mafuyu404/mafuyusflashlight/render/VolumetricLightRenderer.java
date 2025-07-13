package com.mafuyu404.mafuyusflashlight.render;

import com.mafuyu404.mafuyusflashlight.Item.FlashlightItem;
import com.mafuyu404.mafuyusflashlight.init.Utils;
import com.mafuyu404.mafuyusflashlight.network.FlashlightStateManager;
import com.mafuyu404.mafuyusflashlight.registry.Config;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VolumetricLightRenderer {
    private static final Minecraft mc = Minecraft.getInstance();

    // 存储所有需要渲染的手电筒光源
    private static final List<FlashlightSource> lightSources = new CopyOnWriteArrayList<>();

    // 锥体网格数据
    private static VertexBuffer coneVertexBuffer;
    private static boolean meshInitialized = false;

    public static class FlashlightSource {
        public Vec3 position;
        public Vec3 direction;
        public float intensity;
        public float range;
        public float coneAngle;
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

        // 添加本地玩家的手电筒
        Player localPlayer = mc.player;
        if (localPlayer != null && Utils.isUsingFlashlight(localPlayer)) {
            addFlashlightSource(localPlayer);
        }

        // 添加其他玩家的手电筒
        for (Player player : mc.level.players()) {
            if (player != localPlayer && FlashlightStateManager.isPlayerUsingFlashlight(player.getUUID())) {
                addFlashlightSource(player);
            }
        }
    }

    private static void addFlashlightSource(Player player) {
        // 计算手电筒的世界位置
        Vec3 flashlightPos = calculateFlashlightPosition(player);
        Vec3 flashlightDir = calculateFlashlightDirection(player);

        lightSources.add(new FlashlightSource(
                player,
                flashlightPos,
                flashlightDir,
                Config.LIGHT_INTENSITY.get().floatValue() * 6.8f, // 降低亮度倍数
                Config.VOLUMETRIC_RANGE.get().floatValue(),
                Config.VOLUMETRIC_CONE_ANGLE.get().floatValue()
        ));
    }

    private static Vec3 calculateFlashlightPosition(Player player) {
        // 获取玩家眼睛位置作为基准
        Vec3 eyePos = player.getEyePosition(1.0f);

        // 获取玩家的视线方向
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 upVec = rightVec.cross(lookVec).normalize();

        // 检查手电筒在哪只手
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isMainHand = mainHand.getItem() instanceof FlashlightItem && FlashlightItem.getPowered(mainHand) == 1;
        boolean isOffHand = offHand.getItem() instanceof FlashlightItem && FlashlightItem.getPowered(offHand) == 1;

        // 计算手电筒位置偏移 - 调整偏移量使其更贴近手的实际位置
        Vec3 handOffset;
        if (isMainHand) {
            // 主手（右手）- 调整位置使其更靠近手的实际位置
            handOffset = rightVec.scale(0.25).add(upVec.scale(-0.15)).add(lookVec.scale(0.3));
        } else if (isOffHand) {
            // 副手（左手）
            handOffset = rightVec.scale(-0.25).add(upVec.scale(-0.15)).add(lookVec.scale(0.3));
        } else {
            // 如果是Curios装备，假设在右手
            handOffset = rightVec.scale(0.25).add(upVec.scale(-0.15)).add(lookVec.scale(0.3));
        }

        // 考虑玩家蹲下的情况
        if (player.isCrouching()) {
            handOffset = handOffset.add(0, -0.15, 0);
        }

        return eyePos.add(handOffset);
    }

    private static Vec3 calculateFlashlightDirection(Player player) {
        // 手电筒的方向基本跟随玩家的视线方向，但稍微向下倾斜
        Vec3 lookVec = player.getViewVector(1.0f);

        // 添加轻微的向下倾斜，模拟手电筒不是完全水平的
        Vec3 upVec = new Vec3(0, 1, 0);
        Vec3 rightVec = lookVec.cross(upVec).normalize();
        Vec3 adjustedUp = rightVec.cross(lookVec).normalize();

        // 向下倾斜5度
        double tiltAngle = Math.toRadians(-5);
        Vec3 tiltedDirection = lookVec.scale(Math.cos(tiltAngle)).add(adjustedUp.scale(Math.sin(tiltAngle)));

        return tiltedDirection.normalize();
    }

    private static boolean shouldRenderLight(FlashlightSource source, Vec3 cameraPos) {
        // 距离剔除
        double distance = source.position.distanceTo(cameraPos);
        if (distance > source.range * 1.5) {
            return false;
        }

        // 对于第一人称视角，始终渲染
        if (source.player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            return true;
        }

        // 其他玩家视锥剔除
        Vec3 toCameraDir = cameraPos.subtract(source.position).normalize();
        double dot = source.direction.dot(toCameraDir);

        double maxAngle = Math.cos(Math.toRadians(source.coneAngle * 0.5 + 60));
        return dot > maxAngle;
    }


    public static void render(PoseStack poseStack, float partialTick) {
        if (lightSources.isEmpty() || !meshInitialized) {
            return;
        }

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        // 过滤需要渲染的光源
        List<FlashlightSource> visibleSources = lightSources.stream()
                .filter(source -> shouldRenderLight(source, cameraPos))
                .toList();

        if (visibleSources.isEmpty()) {
            return;
        }

        ShaderInstance shader = ModShaders.getVolumetricLightShader();
        if (shader == null) {
            return;
        }

        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(false); // 不写入深度，只读取
        RenderSystem.disableCull(); // 禁用背面剔除，确保锥体内部可见

        shader.apply();

        for (FlashlightSource source : visibleSources) {
            renderVolumetricCone(poseStack, source, partialTick, shader);
        }

        shader.clear();

        // 恢复渲染状态
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void renderVolumetricCone(PoseStack poseStack, FlashlightSource source, float partialTick, ShaderInstance shader) {
        poseStack.pushPose();

        // 计算相机相对位置
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 relativePos = source.position.subtract(cameraPos);

        // 移动到光源位置
        poseStack.translate(relativePos.x, relativePos.y, relativePos.z);

        // 计算旋转角度
        Vec3 dir = source.direction;
        float yaw = (float) Math.atan2(dir.x, dir.z);
        float pitch = (float) Math.asin(-dir.y);

        poseStack.mulPose(Axis.YP.rotation(yaw));
        poseStack.mulPose(Axis.XP.rotation(pitch));

        // 缩放锥体
        float scale = source.range;
        poseStack.scale(scale, scale, scale);

        // 设置着色器uniforms - 使用安全的方式
        Matrix4f modelViewMatrix = poseStack.last().pose();

        try {
            if (shader.getUniform("ModelViewMat") != null) {
                shader.getUniform("ModelViewMat").set(modelViewMatrix);
            }
            if (shader.getUniform("ProjMat") != null) {
                shader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
            }
            if (shader.getUniform("LightPosition") != null) {
                shader.getUniform("LightPosition").set((float)source.position.x, (float)source.position.y, (float)source.position.z);
            }
            if (shader.getUniform("LightDirection") != null) {
                shader.getUniform("LightDirection").set((float)source.direction.x, (float)source.direction.y, (float)source.direction.z);
            }
            if (shader.getUniform("LightIntensity") != null) {
                shader.getUniform("LightIntensity").set(source.intensity);
            }
            if (shader.getUniform("LightRange") != null) {
                shader.getUniform("LightRange").set(source.range);
            }
            if (shader.getUniform("ConeAngle") != null) {
                shader.getUniform("ConeAngle").set(source.coneAngle);
            }
            if (shader.getUniform("Time") != null) {
                shader.getUniform("Time").set((float)(System.currentTimeMillis() % 100000) / 1000.0f);
            }
        } catch (Exception e) {
            // 忽略 uniform 设置错误
        }

        // 渲染锥体网格
        if (coneVertexBuffer != null) {
            coneVertexBuffer.bind();
            coneVertexBuffer.draw();
        }

        poseStack.popPose();
    }

    private static void createConeMesh() {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        int segments = 32; // 增加分段数以获得更平滑的效果
        float height = 1.0f;
        float radius = (float) Math.tan(Math.toRadians(22.5)); // 45度锥角的一半

        // 锥体顶点（光源位置）
        Vec3 apex = new Vec3(0, 0, 0);

        // 生成锥体底面圆周上的点
        Vec3[] baseVertices = new Vec3[segments];
        for (int i = 0; i < segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            float x = radius * Mth.cos(angle);
            float y = radius * Mth.sin(angle);
            baseVertices[i] = new Vec3(x, y, height);
        }

        // 生成锥体侧面三角形
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;

            // 降低透明度值，避免过亮
            float alpha1 = 0.6f; // 顶点处的透明度 - 降低
            float alpha2 = 0.15f; // 底面处的透明度 - 降低

            // 三角形：顶点 -> 当前底面点 -> 下一个底面点
            addVertex(builder, apex, 1.0f, 1.0f, 0.9f, alpha1, 0.5f, 0.0f);
            addVertex(builder, baseVertices[i], 1.0f, 1.0f, 0.9f, alpha2, 0.0f, 1.0f);
            addVertex(builder, baseVertices[next], 1.0f, 1.0f, 0.9f, alpha2, 1.0f, 1.0f);
        }

        // 添加锥体底面（可选，用于更好的视觉效果）
        Vec3 center = new Vec3(0, 0, height);
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            addVertex(builder, center, 1.0f, 1.0f, 0.9f, 0.05f, 0.5f, 0.5f);
            addVertex(builder, baseVertices[i], 1.0f, 1.0f, 0.9f, 0.05f, 0.0f, 1.0f);
            addVertex(builder, baseVertices[next], 1.0f, 1.0f, 0.9f, 0.05f, 1.0f, 1.0f);
        }

        coneVertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        coneVertexBuffer.bind();
        coneVertexBuffer.upload(builder.end());
        VertexBuffer.unbind();
    }

    private static void addVertex(BufferBuilder builder, Vec3 pos, float r, float g, float b, float a, float u, float v) {
        builder.vertex(pos.x, pos.y, pos.z)
                .color(r, g, b, a)
                .uv(u, v)
                .uv2(240, 240) // 最大亮度
                .endVertex();
    }
}