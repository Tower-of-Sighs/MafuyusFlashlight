package com.mafuyu404.mafuyusflashlight.api;

import net.minecraft.client.renderer.PostPass;

import java.util.List;

public interface PostChainAccessor {
    List<PostPass> getPasses();
}
