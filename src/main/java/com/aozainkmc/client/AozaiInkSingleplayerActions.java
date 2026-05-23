package com.aozainkmc.client;

import com.aozainkmc.AozaiInkMc;
import com.aozainkmc.api.AozaiInkApi;
import com.aozainkmc.api.InkGlyphRegistry;
import com.aozainkmc.api.InkMark;
import com.aozainkmc.api.InkStaffProgress;
import com.aozainkmc.api.InkStaffMetadata;
import com.aozainkmc.api.InkStaffTier;
import com.aozainkmc.api.InkTarget;
import com.aozainkmc.client.ocr.OcrCandidate;
import com.aozainkmc.core.AozaiInkItems;
import com.aozainkmc.core.SemanticTags;
import com.aozainkmc.core.event.InkBlockTargetSelectedEvent;
import com.aozainkmc.core.event.InkMarkBeforeAttachEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

final class AozaiInkSingleplayerActions {
    private static final long DEFAULT_MARK_TTL = 20L * 60L * 10L;

    private AozaiInkSingleplayerActions() {
    }

    static AttachResult attachRecognizedMark(
        Minecraft minecraft,
        UUID playerId,
        OcrCandidate candidate,
        boolean anchorMode,
        BlockPos markerPos
    ) {
        if (!minecraft.hasSingleplayerServer()) {
            return AttachResult.unavailable();
        }

        MinecraftServer server = minecraft.getSingleplayerServer();
        CompletableFuture<AttachResult> future = new CompletableFuture<>();
        server.execute(() -> {
            try {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                if (player == null || !player.isAlive()) {
                    future.complete(AttachResult.rejected(""));
                    return;
                }

                InkStaffTier staffTier = currentStaffTier(player);
                if (staffTier == null) {
                    future.complete(AttachResult.rejected("需要手持字灵魔杖"));
                    return;
                }
                if (!InkGlyphRegistry.isAccepted(candidate.character())) {
                    future.complete(AttachResult.rejected(candidate.character() + ": 此字未通"));
                    return;
                }

                String dimension = player.serverLevel().dimension().location().toString();
                InkTarget target = anchorMode
                    ? InkTarget.marker(dimension, markerPos.asLong(), markerPos.getX() >> 4, markerPos.getZ() >> 4)
                    : InkTarget.player(dimension, player.getUUID());
                String source = InkStaffMetadata.source(anchorMode ? "onnx.handwriting.anchor" : "onnx.handwriting.cast", staffTier);
                InkMark mark = new InkMark(
                    candidate.character(),
                    InkMark.hashWord(candidate.character()),
                    SemanticTags.forWord(candidate.character()),
                    candidate.confidence(),
                    player.getUUID(),
                    target,
                    player.serverLevel().getGameTime(),
                    DEFAULT_MARK_TTL,
                    source
                );

                InkMarkBeforeAttachEvent beforeAttach = new InkMarkBeforeAttachEvent(player, mark, staffTier);
                NeoForge.EVENT_BUS.post(beforeAttach);
                if (beforeAttach.isCanceled()) {
                    future.complete(AttachResult.canceled(beforeAttach.clientInstruction()));
                    return;
                }

                AozaiInkApi.marks().attach(mark);
                addProgressToCurrentStaff(player, staffTier, 1);
                damageCurrentStaff(player);
                future.complete(AttachResult.attached(beforeAttach.clientInstruction()));
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            AozaiInkMc.LOGGER.warn("Timed out while attaching ink mark {}", candidate.character());
            return AttachResult.unavailable();
        } catch (Exception exception) {
            AozaiInkMc.LOGGER.error("Failed to attach ink mark {}", candidate.character(), exception);
            return AttachResult.rejected("字灵施写失败");
        }
    }

    static void dispatchBlockTargetSelected(Minecraft minecraft, UUID playerId, String token, BlockPos pos) {
        if (!minecraft.hasSingleplayerServer()) {
            return;
        }
        MinecraftServer server = minecraft.getSingleplayerServer();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null || !player.isAlive()) {
                return;
            }
            InkBlockTargetSelectedEvent event = new InkBlockTargetSelectedEvent(player, token, pos);
            NeoForge.EVENT_BUS.post(event);
            applyClientInstruction(minecraft, event.clientInstruction());
        });
    }

    static void applyClientInstruction(Minecraft minecraft, InkMarkBeforeAttachEvent.ClientInstruction instruction) {
        if (instruction == null || instruction.action() == InkMarkBeforeAttachEvent.ClientAction.NONE) {
            return;
        }
        minecraft.execute(() -> {
            switch (instruction.action()) {
                case NONE -> {
                }
                case CLOSE_INPUT -> InkInputController.requestClose(minecraft, instruction.message());
                case OPEN_CAST_INPUT -> InkInputController.requestOpenCast(minecraft, instruction.message());
                case OPEN_ANCHOR_INPUT -> InkInputController.requestOpenAnchor(minecraft, instruction.message());
                case REQUEST_BLOCK_TARGET -> {
                    InkInputController.requestClose(minecraft, "");
                    if (minecraft.player != null) {
                        InkBlockTargetController.request(minecraft.player, instruction.token(), instruction.message(), instruction.maxDistance());
                    }
                }
            }
        });
    }

    enum AttachStatus {
        ATTACHED,
        CANCELED,
        REJECTED,
        UNAVAILABLE
    }

    record AttachResult(AttachStatus status, InkMarkBeforeAttachEvent.ClientInstruction instruction, String message) {
        static AttachResult attached(InkMarkBeforeAttachEvent.ClientInstruction instruction) {
            return new AttachResult(AttachStatus.ATTACHED, instruction, "");
        }

        static AttachResult canceled(InkMarkBeforeAttachEvent.ClientInstruction instruction) {
            return new AttachResult(AttachStatus.CANCELED, instruction, "");
        }

        static AttachResult rejected(String message) {
            return new AttachResult(AttachStatus.REJECTED, InkMarkBeforeAttachEvent.ClientInstruction.none(), message);
        }

        static AttachResult unavailable() {
            return new AttachResult(AttachStatus.UNAVAILABLE, InkMarkBeforeAttachEvent.ClientInstruction.none(), "当前只支持单人存档施写");
        }
    }

    private static InkStaffTier currentStaffTier(ServerPlayer player) {
        return AozaiInkItems.staffTier(player.getMainHandItem())
            .or(() -> AozaiInkItems.staffTier(player.getOffhandItem()))
            .orElse(null);
    }

    private static void damageCurrentStaff(ServerPlayer player) {
        if (player.getAbilities().instabuild) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        EquipmentSlot slot = EquipmentSlot.MAINHAND;
        if (!AozaiInkItems.isInkStaff(stack)) {
            stack = player.getOffhandItem();
            slot = EquipmentSlot.OFFHAND;
        }
        if (!AozaiInkItems.isInkStaff(stack) || !stack.isDamageableItem()) {
            return;
        }

        stack.hurtAndBreak(1, player, slot);
    }

    private static void addProgressToCurrentStaff(ServerPlayer player, InkStaffTier tier, int amount) {
        ItemStack stack = player.getMainHandItem();
        if (!AozaiInkItems.isInkStaff(stack)) {
            stack = player.getOffhandItem();
        }
        if (AozaiInkItems.staffTier(stack).orElse(null) == tier && InkStaffProgress.addProgress(stack, tier, amount)) {
            player.displayClientMessage(Component.literal("魔杖境界已满，可写「劫」"), true);
        }
    }
}
