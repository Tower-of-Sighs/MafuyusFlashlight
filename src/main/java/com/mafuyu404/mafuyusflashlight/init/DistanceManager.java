package com.mafuyu404.mafuyusflashlight.init;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DistanceManager {
    public static double MaxDistance = 32;

    public static double getTargetBlockDistance(Player player) {
        // 获取玩家眼睛位置（考虑高度，如1.62格）
        Vec3 eyePos = player.getEyePosition(1.0F);
        // 获取视线方向向量
        Vec3 viewVec = player.getViewVector(1.0F);
        // 计算射线终点
        Vec3 endPos = eyePos.add(viewVec.x * MaxDistance, viewVec.y * MaxDistance, viewVec.z * MaxDistance);

        // 创建ClipContext，设置检测模式（OUTLINE检测方块轮廓，忽略流体）
        ClipContext context = new ClipContext(eyePos, endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player);

        // 执行射线追踪
        BlockHitResult hitResult = player.level().clip(context);

        // 判断是否击中方块
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 获取击中点坐标
            Vec3 hitPos = hitResult.getLocation();
            // 计算距离
            return eyePos.distanceTo(hitPos);
        }

        // 未击中方块时返回-1或其它标识
        return -1;
    }
}
