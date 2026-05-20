package com.aozainkmc.client;

import com.aozainkmc.AozaiInkMc;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = AozaiInkMc.MOD_ID, value = Dist.CLIENT)
public final class AozaiInkClientEvents {
    private AozaiInkClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        InkInputController.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        InkInputController.render(event);
    }
}
