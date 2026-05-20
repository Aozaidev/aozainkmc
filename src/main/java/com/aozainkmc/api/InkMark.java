package com.aozainkmc.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public record InkMark(
    String word,
    String semanticHash,
    List<String> tags,
    float confidence,
    UUID owner,
    InkTarget target,
    long bornGameTime,
    long ttlTicks,
    String source
) {
    public InkMark {
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("word cannot be blank");
        }
        semanticHash = semanticHash == null || semanticHash.isBlank() ? hashWord(word) : semanticHash;
        tags = List.copyOf(tags == null ? List.of() : tags);
        confidence = Math.max(0.0F, Math.min(1.0F, confidence));
        ttlTicks = Math.max(1L, ttlTicks);
        source = source == null || source.isBlank() ? "unknown" : source;
    }

    public boolean expired(long gameTime) {
        return gameTime - bornGameTime >= ttlTicks;
    }

    public static String hashWord(String word) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(word.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
