package com.aozainkmc.client.input;

import java.util.ArrayList;
import java.util.List;

public final class InkStroke {
    private final List<InkStrokePoint> points = new ArrayList<>();

    public void add(InkStrokePoint point) {
        points.add(point);
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public InkStrokePoint last() {
        return points.get(points.size() - 1);
    }

    public List<InkStrokePoint> points() {
        return points;
    }
}
