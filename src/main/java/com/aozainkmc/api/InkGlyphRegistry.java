package com.aozainkmc.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InkGlyphRegistry {
    private static final Set<String> ACCEPTED_WORDS = ConcurrentHashMap.newKeySet();

    private InkGlyphRegistry() {
    }

    public static void register(String word) {
        if (word != null && !word.isBlank()) {
            ACCEPTED_WORDS.add(word);
        }
    }

    public static void registerAll(Collection<String> words) {
        if (words == null) {
            return;
        }
        words.forEach(InkGlyphRegistry::register);
    }

    public static boolean isAccepted(String word) {
        return word != null && ACCEPTED_WORDS.contains(word);
    }

    public static Set<String> acceptedWords() {
        return Collections.unmodifiableSet(ACCEPTED_WORDS);
    }
}
