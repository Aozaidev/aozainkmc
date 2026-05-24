package com.aozainkmc.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InkGlyphRegistry {
    private static final Set<String> ACCEPTED_WORDS = ConcurrentHashMap.newKeySet();
    private static final Map<String, InkGlyphDefinition> DEFINITIONS = new ConcurrentHashMap<>();

    private InkGlyphRegistry() {
    }

    public static void register(String word) {
        if (word != null && !word.isBlank()) {
            ACCEPTED_WORDS.add(word);
            DEFINITIONS.putIfAbsent(word, InkGlyphDefinition.builder(word).build());
        }
    }

    public static void registerAll(Collection<String> words) {
        if (words == null) {
            return;
        }
        words.forEach(InkGlyphRegistry::register);
    }

    public static void register(InkGlyphDefinition definition) {
        if (definition == null) {
            return;
        }
        ACCEPTED_WORDS.add(definition.word());
        DEFINITIONS.put(definition.word(), definition);
        if (definition.closeAfterRecognize()) {
            InkGlyphClientBehaviorRegistry.registerCloseAfterRecognize(definition.word());
        }
    }

    public static void registerDefinitions(Collection<InkGlyphDefinition> definitions) {
        if (definitions == null) {
            return;
        }
        definitions.forEach(InkGlyphRegistry::register);
    }

    public static boolean isAccepted(String word) {
        return word != null && ACCEPTED_WORDS.contains(word);
    }

    public static Optional<InkGlyphDefinition> definition(String word) {
        return Optional.ofNullable(DEFINITIONS.get(word));
    }

    public static InkGlyphDefinition definitionOrDefault(String word) {
        return definition(word).orElseGet(() -> InkGlyphDefinition.builder(word).build());
    }

    public static Set<String> acceptedWords() {
        return Collections.unmodifiableSet(ACCEPTED_WORDS);
    }

    public static Collection<InkGlyphDefinition> definitions() {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }
}
