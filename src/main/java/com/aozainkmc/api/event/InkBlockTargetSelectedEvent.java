package com.aozainkmc.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class InkBlockTargetSelectedEvent extends Event {
    private final ServerPlayer player;
    private final String token;
    private final BlockPos pos;
    private InkMarkBeforeAttachEvent.ClientInstruction clientInstruction = InkMarkBeforeAttachEvent.ClientInstruction.none();

    public InkBlockTargetSelectedEvent(ServerPlayer player, String token, BlockPos pos) {
        this.player = player;
        this.token = token == null ? "" : token;
        this.pos = pos.immutable();
    }

    public ServerPlayer player() {
        return player;
    }

    public String token() {
        return token;
    }

    public BlockPos pos() {
        return pos;
    }

    public InkMarkBeforeAttachEvent.ClientInstruction clientInstruction() {
        return clientInstruction;
    }

    public void requestCloseInput(String message) {
        this.clientInstruction = new InkMarkBeforeAttachEvent.ClientInstruction(
            InkMarkBeforeAttachEvent.ClientAction.CLOSE_INPUT,
            "",
            message,
            0
        );
    }

    public void requestOpenCastInput(String message) {
        this.clientInstruction = new InkMarkBeforeAttachEvent.ClientInstruction(
            InkMarkBeforeAttachEvent.ClientAction.OPEN_CAST_INPUT,
            "",
            message,
            0
        );
    }

    public void requestOpenAnchorInput(String message) {
        this.clientInstruction = new InkMarkBeforeAttachEvent.ClientInstruction(
            InkMarkBeforeAttachEvent.ClientAction.OPEN_ANCHOR_INPUT,
            "",
            message,
            0
        );
    }

    public void requestBlockTarget(String token, String message, int maxDistance) {
        this.clientInstruction = new InkMarkBeforeAttachEvent.ClientInstruction(
            InkMarkBeforeAttachEvent.ClientAction.REQUEST_BLOCK_TARGET,
            token,
            message,
            maxDistance
        );
    }
}
