package com.mafuyu404.mafuyusflashlight.mixin;

import com.mafuyu404.mafuyusflashlight.api.DepthBindable;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderTarget.class)
public class RenderTargetMixin implements DepthBindable {

    @Shadow
    protected int depthBufferId;

    @Override
    public void bindDepthTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthBufferId);
    }
}
