package com.aozainkmc.api;

import java.util.Optional;

public final class InkStaffMetadata {
    private static final String STAFF_PREFIX = "staff=";
    private static final String POWER_PREFIX = "power=";

    private InkStaffMetadata() {
    }

    public static String source(String baseSource, InkStaffTier tier) {
        return baseSource + ";staff=" + tier.id() + ";power=" + tier.powerMultiplier();
    }

    public static InkStaffTier tier(InkMark mark) {
        return tier(mark.source()).orElse(InkStaffTier.WOOD);
    }

    public static Optional<InkStaffTier> tier(String source) {
        return field(source, STAFF_PREFIX).flatMap(InkStaffTier::byId);
    }

    public static float powerMultiplier(InkMark mark) {
        return powerMultiplier(mark.source()).orElse(tier(mark).powerMultiplier());
    }

    public static Optional<Float> powerMultiplier(String source) {
        return field(source, POWER_PREFIX).flatMap(value -> {
            try {
                return Optional.of(Float.parseFloat(value));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        });
    }

    private static Optional<String> field(String source, String prefix) {
        if (source == null || source.isBlank()) {
            return Optional.empty();
        }
        String[] parts = source.split(";");
        for (String part : parts) {
            if (part.startsWith(prefix)) {
                return Optional.of(part.substring(prefix.length()));
            }
        }
        return Optional.empty();
    }
}
