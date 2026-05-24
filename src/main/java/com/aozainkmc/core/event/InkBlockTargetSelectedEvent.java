package com.aozainkmc.core.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * @deprecated Use {@link com.aozainkmc.api.event.InkBlockTargetSelectedEvent}.
 */
@Deprecated(forRemoval = false)
public class InkBlockTargetSelectedEvent extends com.aozainkmc.api.event.InkBlockTargetSelectedEvent {
    public InkBlockTargetSelectedEvent(ServerPlayer player, String token, BlockPos pos) {
        super(player, token, pos);
    }
}
