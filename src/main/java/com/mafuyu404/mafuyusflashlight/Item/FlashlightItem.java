package com.mafuyu404.mafuyusflashlight.Item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FlashlightItem extends Item {
    public FlashlightItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        togglePowered(itemStack);
        return super.use(level, player, hand);
    }

    public static void togglePowered(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        int powered = nbt.getInt("powered");
        nbt.putInt("powered", powered ^ 1); // 在0和1之间切换
    }

    public static int getPowered(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        return nbt.getInt("powered");
    }
}
