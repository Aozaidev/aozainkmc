package com.aozainkmc.core.event;

import com.aozainkmc.api.InkMark;

/**
 * @deprecated Use {@link com.aozainkmc.api.event.InkMarkAttachedEvent}.
 */
@Deprecated(forRemoval = false)
public class InkMarkAttachedEvent extends com.aozainkmc.api.event.InkMarkAttachedEvent {
    public InkMarkAttachedEvent(InkMark mark) {
        super(mark);
    }
}
