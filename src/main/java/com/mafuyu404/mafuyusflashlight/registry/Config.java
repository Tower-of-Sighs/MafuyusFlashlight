package com.mafuyu404.mafuyusflashlight.registry;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue LIGHT_INTENSITY;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FIXED_FLASHLIGHT;
    public static final ForgeConfigSpec.BooleanValue USE_VOLUMETRIC_LIGHTING;
    public static final ForgeConfigSpec.DoubleValue VOLUMETRIC_RANGE;
    public static final ForgeConfigSpec.DoubleValue VOLUMETRIC_CONE_ANGLE;

    static {
        BUILDER.push("Flashlight Settings");

        LIGHT_INTENSITY = BUILDER
                .comment("Light intensity (0.1 - 5.0)")
                .defineInRange("light_intensity", 1.0, 0.1, 5.0);

        ENABLE_FIXED_FLASHLIGHT = BUILDER
                .comment("Enable fixed flashlight (no sway)")
                .define("enable_fixed_flashlight", false);

        USE_VOLUMETRIC_LIGHTING = BUILDER
                .comment("Use volumetric lighting instead of post-processing")
                .define("use_volumetric_lighting", true);

        VOLUMETRIC_RANGE = BUILDER
                .comment("Volumetric light range (5.0 - 64.0)")
                .defineInRange("volumetric_range", 32.0, 5.0, 64.0);

        VOLUMETRIC_CONE_ANGLE = BUILDER
                .comment("Volumetric light cone angle in degrees (15.0 - 90.0)")
                .defineInRange("volumetric_cone_angle", 45.0, 15.0, 90.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}