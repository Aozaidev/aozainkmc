package com.aozainkmc.core;

import com.aozainkmc.AozaiInkMc;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AozaiInkRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER, AozaiInkMc.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InkStaffUpgradeRecipe>> STAFF_UPGRADE =
        SERIALIZERS.register("staff_upgrade", () -> new SimpleCraftingRecipeSerializer<>(InkStaffUpgradeRecipe::new));

    private AozaiInkRecipes() {
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
