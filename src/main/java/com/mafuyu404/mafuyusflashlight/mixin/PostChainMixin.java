package com.mafuyu404.mafuyusflashlight.mixin;

import com.mafuyu404.mafuyusflashlight.api.PostChainAccessor;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(value = PostChain.class)
@Implements(@Interface(iface = PostChainAccessor.class, prefix = "lazy$")) // 动态附加接口
public class PostChainMixin implements PostChainAccessor {
    @Shadow
    @Final
    private List<PostPass> passes;

    public List<PostPass> getPasses() {
        return passes;
    }
}
