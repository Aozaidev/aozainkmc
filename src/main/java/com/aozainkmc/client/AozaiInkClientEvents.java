package com.aozainkmc.client;

import com.aozainkmc.AozaiInkMc;
import com.aozainkmc.api.AozaiInkApi;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = AozaiInkMc.MOD_ID, value = Dist.CLIENT)
public final class AozaiInkClientEvents {
    private AozaiInkClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        InkInputController.tick(minecraft);
        InkBlockTargetController.clientTick(minecraft);
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        InkInputController.render(event);
    }

    @SubscribeEvent
    public static void interactionKey(InputEvent.InteractionKeyMappingTriggered event) {
        InkBlockTargetController.interactionKey(event);
        if (event.isCanceled()) {
            return;
        }
        if (InkInputController.isActive() && (event.isAttack() || event.isPickBlock())) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void clientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        InkInputController.resetSession();
        InkBlockTargetController.cancel();
        AozaiInkApi.marks().clearAll();
    }
}
