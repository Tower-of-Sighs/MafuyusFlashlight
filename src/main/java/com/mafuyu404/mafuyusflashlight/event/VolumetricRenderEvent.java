package com.mafuyu404.mafuyusflashlight.event;

import com.mafuyu404.mafuyusflashlight.Mafuyusflashlight;
import com.mafuyu404.mafuyusflashlight.render.VolumetricLightRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Mafuyusflashlight.MODID, value = Dist.CLIENT)
public class VolumetricRenderEvent {
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            // 在半透明方块渲染后，粒子渲染前进行体积光照渲染
            VolumetricLightRenderer.updateLightSources();
            VolumetricLightRenderer.render(event.getPoseStack(), event.getPartialTick());
        }
    }
}