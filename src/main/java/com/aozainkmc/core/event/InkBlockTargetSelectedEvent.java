package com.aozainkmc.core.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public final class InkBlockTargetSelectedEvent extends Event {
    private final ServerPlayer player;
    private final String token;
    private final BlockPos pos;

    public InkBlockTargetSelectedEvent(ServerPlayer player, String token, BlockPos pos) {
        this.player = player;
        this.token = token;
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
}
