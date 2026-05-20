package com.aozainkmc.client.input;

public record InkStrokePoint(float u, float v, long timeMs) {
    public double distanceTo(float otherU, float otherV) {
        double du = u - otherU;
        double dv = v - otherV;
        return Math.sqrt(du * du + dv * dv);
    }
}
