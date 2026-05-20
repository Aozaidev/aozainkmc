package com.aozainkmc.client.input;

import java.util.Optional;
import net.minecraft.world.phys.Vec3;

public record InkPlane(Vec3 center, Vec3 normal, Vec3 right, Vec3 up, float radius) {
    private static final double EPSILON = 1.0E-5D;

    public static InkPlane create(Vec3 eyePos, Vec3 lookDir) {
        Vec3 normal = lookDir.normalize();
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = normal.cross(worldUp);
        if (right.lengthSqr() < EPSILON) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        Vec3 up = right.cross(normal).normalize();
        Vec3 center = eyePos.add(normal.scale(2.2D));
        return new InkPlane(center, normal, right, up, 1.0F);
    }

    public Optional<PlaneHit> raycast(Vec3 origin, Vec3 rayDir) {
        double denom = rayDir.dot(normal);
        if (Math.abs(denom) < EPSILON) {
            return Optional.empty();
        }

        double t = center.subtract(origin).dot(normal) / denom;
        if (t <= 0.0D) {
            return Optional.empty();
        }

        Vec3 hit = origin.add(rayDir.scale(t));
        Vec3 local = hit.subtract(center);
        float u = (float) local.dot(right);
        float v = (float) local.dot(up);
        if (u * u + v * v > radius * radius) {
            return Optional.empty();
        }

        return Optional.of(new PlaneHit(u, v, hit));
    }

    public Vec3 pointAt(float u, float v) {
        return center.add(right.scale(u)).add(up.scale(v));
    }
}
