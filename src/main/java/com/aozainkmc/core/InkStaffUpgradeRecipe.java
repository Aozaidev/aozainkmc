package com.aozainkmc.core;

import com.aozainkmc.api.InkStaffProgress;
import com.aozainkmc.api.InkStaffTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class InkStaffUpgradeRecipe extends CustomRecipe {
    public InkStaffUpgradeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return resultFor(input).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return resultFor(input).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AozaiInkRecipes.STAFF_UPGRADE.get();
    }

    private static java.util.Optional<ItemStack> resultFor(CraftingInput input) {
        if (input.ingredientCount() != 2) {
            return java.util.Optional.empty();
        }

        int minX = input.width();
        int minY = input.height();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                if (!input.getItem(x, y).isEmpty()) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX - minX != 1 || maxY - minY != 1) {
            return java.util.Optional.empty();
        }

        ItemStack material = input.getItem(minX + 1, minY);
        ItemStack staff = input.getItem(minX, minY + 1);
        if (staff.isEmpty() || material.isEmpty() || !AozaiInkItems.isInkStaff(staff)) {
            return java.util.Optional.empty();
        }

        InkStaffTier tier = AozaiInkItems.staffTier(staff).orElse(null);
        if (tier == null || !InkStaffProgress.isBreakthroughReady(staff)) {
            return java.util.Optional.empty();
        }

        InkStaffTier target = determineUpgradeTarget(tier, material);
        if (target == null) {
            return java.util.Optional.empty();
        }

        ItemStack result = new ItemStack(AozaiInkItems.itemForTier(target));
        InkStaffProgress.clearForNewTier(result);
        return java.util.Optional.of(result);
    }

    private static InkStaffTier determineUpgradeTarget(InkStaffTier tier, ItemStack material) {
        InkStaffTier mainNext = tier.mainPathNext().orElse(null);
        if (mainNext != null && matchesMaterial(mainNext, material)) {
            return mainNext;
        }

        if (tier.hasBranch()) {
            InkStaffTier branch = tier.branchPath().orElse(null);
            if (branch != null && matchesMaterial(branch, material)) {
                return branch;
            }
        }

        return null;
    }

    private static boolean matchesMaterial(InkStaffTier next, ItemStack material) {
        return switch (next) {
            case STONE -> material.is(ItemTags.STONE_TOOL_MATERIALS);
            case COPPER -> material.is(Items.COPPER_INGOT);
            case IRON -> material.is(Items.IRON_INGOT);
            case GOLD -> material.is(Items.GOLD_INGOT);
            case DIAMOND -> material.is(Items.DIAMOND);
            case NETHERITE -> material.is(Items.NETHERITE_INGOT);
            case WOOD -> false;
        };
    }
}
