package com.aozainkmc.core;

import com.aozainkmc.AozaiInkMc;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AozaiInkItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AozaiInkMc.MOD_ID);

    public static final DeferredItem<Item> INK_BRUSH = ITEMS.registerSimpleItem(
        "ink_brush",
        new Item.Properties().stacksTo(1)
    );

    private AozaiInkItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
