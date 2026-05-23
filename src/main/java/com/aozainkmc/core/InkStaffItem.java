package com.aozainkmc.core;

import com.aozainkmc.api.InkStaffTier;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class InkStaffItem extends Item {
    private final InkStaffTier tier;

    public InkStaffItem(InkStaffTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public InkStaffTier tier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        AozaiInkStaffEvents.appendStaffTooltip(stack, tier, tooltip);
    }
}
