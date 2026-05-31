package com.aozainkmc.api;

import java.util.Locale;
import java.util.Optional;

public enum InkStaffTier {
    WOOD("wood", 295, 1.00F),
    STONE("stone", 655, 1.10F),
    COPPER("copper", 500, 1.25F),
    IRON("iron", 1250, 1.25F),
    GOLD("gold", 200, 1.60F),
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

    public boolean canUpgrade() {
        return this != COPPER && this != GOLD && this != NETHERITE;
    }

    public Optional<InkStaffTier> mainPathNext() {
        return switch (this) {
            case WOOD -> Optional.of(STONE);
            case STONE -> Optional.of(IRON);
            case IRON -> Optional.of(DIAMOND);
            case DIAMOND -> Optional.of(NETHERITE);
            default -> Optional.empty();
        };
    }

    public boolean hasBranch() {
        return this == STONE || this == IRON;
    }

    public Optional<InkStaffTier> branchPath() {
        return switch (this) {
            case STONE -> Optional.of(COPPER);
            case IRON -> Optional.of(GOLD);
            default -> Optional.empty();
        };
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
