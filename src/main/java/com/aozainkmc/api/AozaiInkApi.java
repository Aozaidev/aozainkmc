package com.aozainkmc.api;

import java.util.Objects;

public final class AozaiInkApi {
    private static InkMarkStore store;

    private AozaiInkApi() {
    }

    public static InkMarkStore marks() {
        if (store == null) {
            throw new IllegalStateException("Aozai Ink API is not installed yet");
        }
        return store;
    }

    public static void install(InkMarkStore newStore) {
        if (store != null) {
            throw new IllegalStateException("Aozai Ink API has already been installed");
        }
        store = Objects.requireNonNull(newStore, "newStore");
    }
}
