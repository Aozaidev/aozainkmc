package com.aozainkmc.api;

import java.util.Locale;
import java.util.Optional;

public enum InkStaffTier {
    WOOD("wood", 295, 1.00F),
    STONE("stone", 655, 1.10F),
    COPPER("copper", 950, 1.15F),
    IRON("iron", 1250, 1.25F),
    GOLD("gold", 160, 1.35F),
    DIAMOND("diamond", 7805, 1.60F),
    NETHERITE("netherite", 10155, 1.80F);

    private final String id;
    private final int durability;
    private final float powerMultiplier;

    InkStaffTier(String id, int durability, float powerMultiplier) {
        this.id = id;
        this.durability = durability;
        this.powerMultiplier = powerMultiplier;
    }

    public String id() {
        return id;
    }

    public int durability() {
        return durability;
    }

    public float powerMultiplier() {
        return powerMultiplier;
    }

    public Optional<InkStaffTier> next() {
        int nextOrdinal = ordinal() + 1;
        InkStaffTier[] tiers = values();
        return nextOrdinal >= tiers.length ? Optional.empty() : Optional.of(tiers[nextOrdinal]);
    }

    public static Optional<InkStaffTier> byId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        for (InkStaffTier tier : values()) {
            if (tier.id.equals(normalized)) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }
}
