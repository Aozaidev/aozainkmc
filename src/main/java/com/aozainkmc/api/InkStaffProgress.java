package com.aozainkmc.api;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class InkStaffProgress {
    private static final String PROGRESS_KEY = "aozainkmc_staff_progress";
    private static final String BREAKTHROUGH_KEY = "aozainkmc_staff_breakthrough";
    private static final String INSTANCE_ID_KEY = "aozainkmc_staff_instance_id";

    private InkStaffProgress() {
    }

    public static int target(InkStaffTier tier) {
        return switch (tier) {
            case WOOD -> 30;
            case STONE -> 45;
            case COPPER -> 55;
            case IRON -> 70;
            case GOLD -> 90;
            case DIAMOND -> 110;
            case NETHERITE -> 160;
        };
    }

    public static int progress(ItemStack stack) {
        return Math.max(0, tag(stack).getInt(PROGRESS_KEY));
    }

    public static Optional<UUID> instanceId(ItemStack stack) {
        String id = tag(stack).getString(INSTANCE_ID_KEY);
        if (id.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public static UUID ensureInstanceId(ItemStack stack) {
        Optional<UUID> existing = instanceId(stack);
        if (existing.isPresent()) {
            return existing.get();
        }
        UUID id = UUID.randomUUID();
        update(stack, tag -> tag.putString(INSTANCE_ID_KEY, id.toString()));
        return id;
    }

    public static boolean isFull(ItemStack stack, InkStaffTier tier) {
        return progress(stack) >= target(tier);
    }

    public static boolean addProgress(ItemStack stack, InkStaffTier tier, int amount) {
        if (amount <= 0 || stack.isEmpty()) {
            return false;
        }
        int before = progress(stack);
        int after = Math.min(target(tier), before + amount);
        setProgress(stack, after);
        return before < target(tier) && after >= target(tier);
    }

    public static void halveProgressRoundUp(ItemStack stack) {
        int current = progress(stack);
        setProgress(stack, (current + 1) / 2);
        setBreakthroughReady(stack, false);
    }

    public static boolean isBreakthroughReady(ItemStack stack) {
        return tag(stack).getBoolean(BREAKTHROUGH_KEY);
    }

    public static void setBreakthroughReady(ItemStack stack, boolean ready) {
        update(stack, tag -> {
            if (ready) {
                tag.putBoolean(BREAKTHROUGH_KEY, true);
            } else {
                tag.remove(BREAKTHROUGH_KEY);
            }
        });
    }

    public static void clearForNewTier(ItemStack stack) {
        update(stack, tag -> {
            tag.remove(PROGRESS_KEY);
            tag.remove(BREAKTHROUGH_KEY);
            tag.remove(INSTANCE_ID_KEY);
        });
    }

    private static void setProgress(ItemStack stack, int progress) {
        update(stack, tag -> {
            if (progress <= 0) {
                tag.remove(PROGRESS_KEY);
            } else {
                tag.putInt(PROGRESS_KEY, progress);
            }
        });
    }

    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void update(ItemStack stack, java.util.function.Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }
}
