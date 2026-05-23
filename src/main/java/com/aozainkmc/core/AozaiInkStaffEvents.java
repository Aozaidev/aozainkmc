package com.aozainkmc.core;

import com.aozainkmc.AozaiInkMc;
import com.aozainkmc.api.InkStaffProgress;
import com.aozainkmc.api.InkStaffTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

@EventBusSubscriber(modid = AozaiInkMc.MOD_ID)
public final class AozaiInkStaffEvents {
    private AozaiInkStaffEvents() {
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        InkStaffTier leftTier = AozaiInkItems.staffTier(left).orElse(null);
        InkStaffTier rightTier = AozaiInkItems.staffTier(right).orElse(null);
        if (leftTier == null || leftTier != rightTier || !left.isDamageableItem() || !right.isDamageableItem()) {
            return;
        }

        int repair = Math.max(1, right.getMaxDamage() - right.getDamageValue());
        if (left.getDamageValue() <= 0 || repair <= 0) {
            return;
        }

        ItemStack output = left.copy();
        output.setDamageValue(Math.max(0, left.getDamageValue() - repair));
        event.setOutput(output);
        event.setMaterialCost(1);
        event.setCost(Math.max(1, leftTier.ordinal() + 1));
    }

    public static void appendStaffTooltip(ItemStack stack, InkStaffTier tier, java.util.List<Component> tooltip) {
        int progress = InkStaffProgress.progress(stack);
        int target = InkStaffProgress.target(tier);
        tooltip.add(Component.literal("笔势 " + progress + " / " + target).withStyle(ChatFormatting.GRAY));
        if (InkStaffProgress.isBreakthroughReady(stack)) {
            tooltip.add(Component.literal("已渡劫，可升级").withStyle(ChatFormatting.GOLD));
        } else if (progress >= target && tier.next().isPresent()) {
            tooltip.add(Component.literal("境界已满，可写「劫」").withStyle(ChatFormatting.AQUA));
        } else if (tier.next().isEmpty()) {
            tooltip.add(Component.literal("已至极境").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
