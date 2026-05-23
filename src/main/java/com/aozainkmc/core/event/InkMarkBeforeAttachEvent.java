package com.aozainkmc.core.event;

import com.aozainkmc.api.InkMark;
import com.aozainkmc.api.InkStaffTier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public final class InkMarkBeforeAttachEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final InkMark mark;
    private final InkStaffTier staffTier;
    private ClientInstruction clientInstruction = ClientInstruction.none();
    private boolean consumeOnCancel;
    private int extraDurabilityCost;

    public InkMarkBeforeAttachEvent(ServerPlayer player, InkMark mark, InkStaffTier staffTier) {
        this.player = player;
        this.mark = mark;
        this.staffTier = staffTier;
    }

    public ServerPlayer player() {
        return player;
    }

    public InkMark mark() {
        return mark;
    }

    public InkStaffTier staffTier() {
        return staffTier;
    }

    public ClientInstruction clientInstruction() {
        return clientInstruction;
    }

    public boolean consumeOnCancel() {
        return consumeOnCancel;
    }

    public void setConsumeOnCancel(boolean consumeOnCancel) {
        this.consumeOnCancel = consumeOnCancel;
    }

    public int extraDurabilityCost() {
        return extraDurabilityCost;
    }

    public void addExtraDurabilityCost(int amount) {
        this.extraDurabilityCost += Math.max(0, amount);
    }

    public void requestCloseInput(String message) {
        this.clientInstruction = new ClientInstruction(ClientAction.CLOSE_INPUT, "", message, 0);
    }

    public void requestOpenCastInput(String message) {
        this.clientInstruction = new ClientInstruction(ClientAction.OPEN_CAST_INPUT, "", message, 0);
    }

    public void requestOpenAnchorInput(String message) {
        this.clientInstruction = new ClientInstruction(ClientAction.OPEN_ANCHOR_INPUT, "", message, 0);
    }

    public void requestBlockTarget(String token, String message, int maxDistance) {
        this.clientInstruction = new ClientInstruction(ClientAction.REQUEST_BLOCK_TARGET, token, message, maxDistance);
    }

    public enum ClientAction {
        NONE,
        CLOSE_INPUT,
        OPEN_CAST_INPUT,
        OPEN_ANCHOR_INPUT,
        REQUEST_BLOCK_TARGET
    }

    public record ClientInstruction(ClientAction action, String token, String message, int maxDistance) {
        public static ClientInstruction none() {
            return new ClientInstruction(ClientAction.NONE, "", "", 0);
        }
    }
}
