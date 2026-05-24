package com.aozainkmc.core.event;

import com.aozainkmc.api.InkCastContext;
import com.aozainkmc.api.InkMark;
import com.aozainkmc.api.InkStaffTier;
import net.minecraft.server.level.ServerPlayer;

/**
 * @deprecated Use {@link com.aozainkmc.api.event.InkMarkBeforeAttachEvent}.
 */
@Deprecated(forRemoval = false)
public class InkMarkBeforeAttachEvent extends com.aozainkmc.api.event.InkMarkBeforeAttachEvent {
    public InkMarkBeforeAttachEvent(InkCastContext context) {
        super(context);
    }

    public InkMarkBeforeAttachEvent(ServerPlayer player, InkMark mark, InkStaffTier staffTier) {
        super(player, mark, staffTier);
    }
}
