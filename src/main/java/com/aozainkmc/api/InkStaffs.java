package com.aozainkmc.api;

import com.aozainkmc.core.AozaiInkItems;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class InkStaffs {
    private InkStaffs() {
    }

    public static boolean isStaff(ItemStack stack) {
        return AozaiInkItems.isInkStaff(stack);
    }

    public static Optional<InkStaffTier> tier(ItemStack stack) {
        return AozaiInkItems.staffTier(stack);
    }

    public static Item itemForTier(InkStaffTier tier) {
        return AozaiInkItems.itemForTier(tier);
    }
}
