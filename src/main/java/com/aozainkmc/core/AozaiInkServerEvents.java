package com.aozainkmc.core;

import com.aozainkmc.AozaiInkMc;
import com.aozainkmc.api.AozaiInkApi;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = AozaiInkMc.MOD_ID)
public final class AozaiInkServerEvents {
    private AozaiInkServerEvents() {
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        AozaiInkApi.marks().clearAll();
    }
}
