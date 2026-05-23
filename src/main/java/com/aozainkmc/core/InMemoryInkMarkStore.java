package com.aozainkmc.core;

import com.aozainkmc.api.InkMark;
import com.aozainkmc.api.InkMarkStore;
import com.aozainkmc.api.InkTarget;
import com.aozainkmc.core.event.InkMarkAttachedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.neoforge.common.NeoForge;

public final class InMemoryInkMarkStore implements InkMarkStore {
    private final Map<InkTarget, List<InkMark>> marks = new ConcurrentHashMap<>();

    @Override
    public void attach(InkMark mark) {
        marks.computeIfAbsent(mark.target(), ignored -> new ArrayList<>()).add(mark);
        NeoForge.EVENT_BUS.post(new InkMarkAttachedEvent(mark));
    }

    @Override
    public List<InkMark> marksOn(InkTarget target) {
        return List.copyOf(marks.getOrDefault(target, List.of()));
    }

    @Override
    public List<InkMark> allMarks() {
        return marks.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public void clear(InkTarget target) {
        marks.remove(target);
    }

    @Override
    public void clearAll() {
        marks.clear();
    }

    @Override
    public int pruneExpired(long gameTime) {
        int before = allMarks().size();
        marks.values().forEach(list -> list.removeIf(mark -> mark.expired(gameTime)));
        marks.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        return before - allMarks().size();
    }
}
