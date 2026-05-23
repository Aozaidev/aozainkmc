package com.aozainkmc.core;

import com.aozainkmc.AozaiInkMc;
import com.aozainkmc.api.InkStaffTier;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AozaiInkItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AozaiInkMc.MOD_ID);

    public static final DeferredItem<Item> INK_BRUSH = ITEMS.register(
        "ink_brush",
        () -> new InkStaffItem(InkStaffTier.WOOD, staffProperties(InkStaffTier.WOOD))
    );
    public static final DeferredItem<Item> STONE_INK_STAFF = ITEMS.register(
        "stone_ink_staff",
        () -> new InkStaffItem(InkStaffTier.STONE, staffProperties(InkStaffTier.STONE))
    );
    public static final DeferredItem<Item> COPPER_INK_STAFF = ITEMS.register(
        "copper_ink_staff",
        () -> new InkStaffItem(InkStaffTier.COPPER, staffProperties(InkStaffTier.COPPER))
    );
    public static final DeferredItem<Item> IRON_INK_STAFF = ITEMS.register(
        "iron_ink_staff",
        () -> new InkStaffItem(InkStaffTier.IRON, staffProperties(InkStaffTier.IRON))
    );
    public static final DeferredItem<Item> GOLD_INK_STAFF = ITEMS.register(
        "gold_ink_staff",
        () -> new InkStaffItem(InkStaffTier.GOLD, staffProperties(InkStaffTier.GOLD))
    );
    public static final DeferredItem<Item> DIAMOND_INK_STAFF = ITEMS.register(
        "diamond_ink_staff",
        () -> new InkStaffItem(InkStaffTier.DIAMOND, staffProperties(InkStaffTier.DIAMOND))
    );
    public static final DeferredItem<Item> NETHERITE_INK_STAFF = ITEMS.register(
        "netherite_ink_staff",
        () -> new InkStaffItem(InkStaffTier.NETHERITE, staffProperties(InkStaffTier.NETHERITE).fireResistant())
    );

    public static final List<DeferredItem<Item>> INK_STAVES = List.of(
        INK_BRUSH,
        STONE_INK_STAFF,
        COPPER_INK_STAFF,
        IRON_INK_STAFF,
        GOLD_INK_STAFF,
        DIAMOND_INK_STAFF,
        NETHERITE_INK_STAFF
    );

    private AozaiInkItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static boolean isInkStaff(ItemStack stack) {
        return staffTier(stack).isPresent();
    }

    public static Optional<InkStaffTier> staffTier(ItemStack stack) {
        if (stack.getItem() instanceof InkStaffItem staff) {
            return Optional.of(staff.tier());
        }
        return Optional.empty();
    }

    public static Item itemForTier(InkStaffTier tier) {
        return switch (tier) {
            case WOOD -> INK_BRUSH.get();
            case STONE -> STONE_INK_STAFF.get();
            case COPPER -> COPPER_INK_STAFF.get();
            case IRON -> IRON_INK_STAFF.get();
            case GOLD -> GOLD_INK_STAFF.get();
            case DIAMOND -> DIAMOND_INK_STAFF.get();
            case NETHERITE -> NETHERITE_INK_STAFF.get();
        };
    }

    private static Item.Properties staffProperties(InkStaffTier tier) {
        return new Item.Properties().durability(tier.durability());
    }
}
