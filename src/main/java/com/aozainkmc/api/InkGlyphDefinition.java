package com.aozainkmc.api;

import java.util.EnumSet;
import java.util.Set;

public record InkGlyphDefinition(
    String word,
    Set<InkCastMode> modes,
    InkStaffTier minimumStaffTier,
    boolean closeAfterRecognize
) {
    public InkGlyphDefinition {
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("word cannot be blank");
        }
        modes = modes == null || modes.isEmpty() ? EnumSet.allOf(InkCastMode.class) : EnumSet.copyOf(modes);
        minimumStaffTier = minimumStaffTier == null ? InkStaffTier.WOOD : minimumStaffTier;
    }

    public boolean allows(InkCastMode mode) {
        return modes.contains(mode);
    }

    public static Builder builder(String word) {
        return new Builder(word);
    }

    public static final class Builder {
        private final String word;
        private Set<InkCastMode> modes = EnumSet.allOf(InkCastMode.class);
        private InkStaffTier minimumStaffTier = InkStaffTier.WOOD;
        private boolean closeAfterRecognize;

        private Builder(String word) {
            this.word = word;
        }

        public Builder castOnly() {
            this.modes = EnumSet.of(InkCastMode.CAST);
            return this;
        }

        public Builder anchorOnly() {
            this.modes = EnumSet.of(InkCastMode.ANCHOR);
            return this;
        }

        public Builder modes(Set<InkCastMode> modes) {
            this.modes = modes;
            return this;
        }

        public Builder minimumStaffTier(InkStaffTier tier) {
            this.minimumStaffTier = tier;
            return this;
        }

        public Builder closeAfterRecognize() {
            this.closeAfterRecognize = true;
            return this;
        }

        public InkGlyphDefinition build() {
            return new InkGlyphDefinition(word, modes, minimumStaffTier, closeAfterRecognize);
        }
    }
}
