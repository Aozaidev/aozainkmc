package com.aozainkmc.api.event;

import com.aozainkmc.api.InkMark;
import net.neoforged.bus.api.Event;

public class InkMarkAttachedEvent extends Event {
    private final InkMark mark;

    public InkMarkAttachedEvent(InkMark mark) {
        this.mark = mark;
    }

    public InkMark mark() {
        return mark;
    }
}
