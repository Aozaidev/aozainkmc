package com.aozainkmc.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class AozaiInkKeys {
    static final String CATEGORY = "key.categories.aozainkmc";

    public static final KeyMapping TOGGLE_WRITING = new KeyMapping(
        "key.aozainkmc.toggle_writing",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        CATEGORY
    );
    public static final KeyMapping TOGGLE_ANCHOR = new KeyMapping(
        "key.aozainkmc.toggle_anchor",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        CATEGORY
    );
    public static final KeyMapping RECOGNIZE = new KeyMapping(
        "key.aozainkmc.recognize",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_ENTER,
        CATEGORY
    );
    public static final KeyMapping CLEAR = new KeyMapping(
        "key.aozainkmc.clear",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        CATEGORY
    );
    public static final KeyMapping CANCEL = new KeyMapping(
        "key.aozainkmc.cancel",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        CATEGORY
    );

    private AozaiInkKeys() {
    }

    static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_WRITING);
        event.register(TOGGLE_ANCHOR);
        event.register(RECOGNIZE);
        event.register(CLEAR);
        event.register(CANCEL);
    }
}
