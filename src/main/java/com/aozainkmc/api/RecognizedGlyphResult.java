package com.aozainkmc.api;

import java.util.List;

public record RecognizedGlyphResult(List<RecognizedGlyph> candidates) {
    public RecognizedGlyphResult {
        candidates = List.copyOf(candidates == null ? List.of() : candidates);
    }

    public RecognizedGlyph best() {
        if (candidates.isEmpty()) {
            return new RecognizedGlyph("", 0.0F);
        }
        return candidates.getFirst();
    }

    public static RecognizedGlyphResult single(String word, float confidence) {
        return new RecognizedGlyphResult(List.of(new RecognizedGlyph(word, confidence)));
    }
}
