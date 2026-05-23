package com.aozainkmc.client;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.InputEvent;

final class InkBlockTargetController {
    private static String pendingToken;
    private static int maxDistance;

    private InkBlockTargetController() {
    }

    static void request(LocalPlayer player, String token, String message, int requestedMaxDistance) {
        pendingToken = token == null ? "" : token;
        maxDistance = requestedMaxDistance > 0 ? requestedMaxDistance : 64;
        say(player, message == null || message.isBlank() ? "请选择一个方块" : message);
    }

    static boolean hasPendingTarget() {
        return pendingToken != null;
    }

    static void cancel() {
        pendingToken = null;
        maxDistance = 0;
    }

    static void clientTick(Minecraft minecraft) {
        if (pendingToken == null) {
            return;
        }
        if (minecraft.player == null || minecraft.level == null) {
            cancel();
        }
    }

    static void interactionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (pendingToken == null || !event.isUseItem()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        if (player.blockPosition().distSqr(pos) > (double) maxDistance * (double) maxDistance) {
            say(player, "目标太远");
            event.setCanceled(true);
            return;
        }

        String token = pendingToken;
        UUID playerId = player.getUUID();
        cancel();
        AozaiInkSingleplayerActions.dispatchBlockTargetSelected(minecraft, playerId, token, pos);
        event.setCanceled(true);
    }

    private static void say(LocalPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }
}
