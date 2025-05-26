package com.mafuyu404.mafuyusflashlight.registry;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_FIXED_FLASHLIGHT;
    public static final ForgeConfigSpec.ConfigValue<Float> LIGHT_INTENSITY;

    static {
        BUILDER.push("Client Setting");

        ENABLE_FIXED_FLASHLIGHT = BUILDER
                .comment("是否固定手电筒光的位置。")
                .define("enableFixedFlashlight", false);
        LIGHT_INTENSITY = BUILDER
                .comment("手电筒光强度。")
                .define("LightIntensity", 1.0f);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
