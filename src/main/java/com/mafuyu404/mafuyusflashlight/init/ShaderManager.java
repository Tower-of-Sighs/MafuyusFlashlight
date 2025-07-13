package com.mafuyu404.mafuyusflashlight.init;

import com.mafuyu404.mafuyusflashlight.Mafuyusflashlight;
import com.mafuyu404.mafuyusflashlight.api.PostChainAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Mafuyusflashlight.MODID, value = Dist.CLIENT)
public class ShaderManager {
    private static final Minecraft mc = Minecraft.getInstance();
    // 按加载顺序存放所有 PostChain 实例
    public static final Map<String, PostChain> CHAINS = new LinkedHashMap<>();

    /**
     * 注册资源重加载监听器，确保在资源管理器重新加载时刷新着色器链
     */
//    @SubscribeEvent
//    public static void onAddReloadListener(AddReloadListenerEvent event) {

//    }
    public static List<PostPass> getShader(String name) {
        PostChainAccessor postChain = (PostChainAccessor) CHAINS.get(name);
        return postChain.getPasses();
    }

    public static void loadShader(String name, String jsonPath) {
        if (!CHAINS.containsKey(name)) CHAINS.put(name, createPostChain(jsonPath));
    }

    public static boolean isLoading(String name) {
        return CHAINS.containsKey(name);
    }

    public static void initAll() {
        CHAINS.replaceAll((name, chain) -> createPostChain(chain.getName()));
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            CHAINS.values().forEach(chain -> {
                chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                chain.process(event.getPartialTick());
            });

            mc.getMainRenderTarget().bindWrite(false);
        }
    }


    public static void clean(String name) {
        if (CHAINS.containsKey(name)) {
            CHAINS.get(name).close();
            CHAINS.remove(name);
        }
    }

    public static void cleanup() {
        CHAINS.values().forEach(PostChain::close);
        CHAINS.clear();
    }

    private static PostChain createPostChain(String name) {
        RenderSystem.assertOnRenderThread();
        ResourceLocation rl = new ResourceLocation(Mafuyusflashlight.MODID, name);
        try {
            return new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), rl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}