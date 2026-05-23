package com.aozainkmc.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InkGlyphClientBehaviorRegistry {
    private static final Set<String> CLOSE_AFTER_RECOGNIZE = ConcurrentHashMap.newKeySet();

    private InkGlyphClientBehaviorRegistry() {
    }

    public static void registerCloseAfterRecognize(String word) {
        if (word != null && !word.isBlank()) {
            CLOSE_AFTER_RECOGNIZE.add(word);
        }
    }

    public static void registerCloseAfterRecognize(Collection<String> words) {
        if (words == null) {
            return;
        }
        words.forEach(InkGlyphClientBehaviorRegistry::registerCloseAfterRecognize);
    }

    public static boolean closesAfterRecognize(String word) {
        return word != null && CLOSE_AFTER_RECOGNIZE.contains(word);
    }

    public static Set<String> closeAfterRecognizeWords() {
        return Collections.unmodifiableSet(CLOSE_AFTER_RECOGNIZE);
    }
}
