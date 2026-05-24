package com.aozainkmc.api;

import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;

public record InkCastContext(
    ServerPlayer player,
    InkMark mark,
    InkStaffTier staffTier,
    InkCastMode mode,
    RecognizedGlyphResult recognition
) {
    public InkCastContext {
        player = Objects.requireNonNull(player, "player");
        mark = Objects.requireNonNull(mark, "mark");
        staffTier = staffTier == null ? InkStaffTier.WOOD : staffTier;
        mode = mode == null ? InkCastMode.CAST : mode;
        recognition = recognition == null ? RecognizedGlyphResult.single(mark.word(), mark.confidence()) : recognition;
    }

    public static InkCastContext legacy(ServerPlayer player, InkMark mark, InkStaffTier staffTier) {
        InkCastMode inferredMode = mark != null && mark.source().contains("anchor") ? InkCastMode.ANCHOR : InkCastMode.CAST;
        return new InkCastContext(
            player,
            mark,
            staffTier,
            inferredMode,
            RecognizedGlyphResult.single(mark == null ? "" : mark.word(), mark == null ? 0.0F : mark.confidence())
        );
    }
}
