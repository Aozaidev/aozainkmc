package com.aozainkmc.api;

import java.util.Objects;
import java.util.UUID;

public record InkTarget(
    InkTargetType type,
    String dimension,
    UUID entityUuid,
    long packedBlockPos,
    int chunkX,
    int chunkZ,
    String slot
) {
    public InkTarget {
        type = Objects.requireNonNull(type, "type");
        dimension = dimension == null ? "unknown" : dimension;
        slot = slot == null ? "" : slot;
    }

    public static InkTarget player(String dimension, UUID uuid) {
        return new InkTarget(InkTargetType.PLAYER, dimension, uuid, 0L, 0, 0, "");
    }

    public static InkTarget entity(String dimension, UUID uuid) {
        return new InkTarget(InkTargetType.ENTITY, dimension, uuid, 0L, 0, 0, "");
    }

    public static InkTarget block(String dimension, long packedBlockPos) {
        return new InkTarget(InkTargetType.BLOCK, dimension, null, packedBlockPos, 0, 0, "");
    }

    public static InkTarget chunk(String dimension, int chunkX, int chunkZ) {
        return new InkTarget(InkTargetType.CHUNK, dimension, null, 0L, chunkX, chunkZ, "");
    }

    public static InkTarget marker(String dimension, long packedBlockPos, int chunkX, int chunkZ) {
        return new InkTarget(InkTargetType.MARKER, dimension, null, packedBlockPos, chunkX, chunkZ, "");
    }
}
