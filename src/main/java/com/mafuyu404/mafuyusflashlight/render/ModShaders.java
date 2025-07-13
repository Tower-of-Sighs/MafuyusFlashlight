package com.mafuyu404.mafuyusflashlight.render;

import com.mafuyu404.mafuyusflashlight.Mafuyusflashlight;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = Mafuyusflashlight.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModShaders {
    private static ShaderInstance volumetricLightShader;

    public static ShaderInstance getVolumetricLightShader() {
        return volumetricLightShader;
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation(Mafuyusflashlight.MODID, "volumetric_light"),
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP
                ),
                shader -> volumetricLightShader = shader
        );
    }
}