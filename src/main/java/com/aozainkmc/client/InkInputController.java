package com.aozainkmc.client;

import com.aozainkmc.AozaiInkMc;
import com.aozainkmc.api.InkGlyphClientBehaviorRegistry;
import com.aozainkmc.api.InkStaffTier;
import com.aozainkmc.client.input.InkPlane;
import com.aozainkmc.client.input.InkStroke;
import com.aozainkmc.client.input.InkStrokePoint;
import com.aozainkmc.client.input.PlaneHit;
import com.aozainkmc.client.ocr.OcrCandidate;
import com.aozainkmc.client.ocr.OcrEngine;
import com.aozainkmc.client.ocr.OnnxOcrEngine;
import com.aozainkmc.client.ocr.StrokeRasterizer;
import com.aozainkmc.core.AozaiInkItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.glfw.GLFW;

public final class InkInputController {
    private static final double MIN_POINT_DISTANCE = 0.012D;
    private static final long MAX_POINT_INTERVAL_MS = 22L;
    private static final int MAX_POINTS_PER_STROKE = 512;
    private static final int MAX_TOTAL_POINTS = 2048;
    private static final double MAX_ACTIVE_DISTANCE_SQR = 10.0D * 10.0D;
    private static final long CLOSING_DURATION_MS = 500L;
    private static final StrokeRasterizer RASTERIZER = new StrokeRasterizer();
    private static final InkCircleRenderer RENDERER = new InkCircleRenderer();
    private static final List<InkStroke> STROKES = new ArrayList<>();
    private static OcrEngine ocrEngine;
    private static InkPlane plane;
    private static InkStroke currentStroke;
    private static PlaneHit currentHit;
    private static boolean active;
    private static CircleMode activeMode;
    private static boolean lastMouseDown;
    private static boolean ocrUnavailable;
    private static int totalPoints;
    private static long lastPenUpTimeMs;
    private static boolean autoRecognizePending;
    private static boolean recognizedSinceChange;
    private static boolean fromBack;
    private static ItemStack lastHeldItem;

    private static InkPlane closingPlane;
    private static List<InkStroke> closingStrokes;
    private static long closingStartTimeMs;
    private static boolean closingFromBack;
    private static boolean closingParticlesSpawned;

    private InkInputController() {
    }

    public static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            close();
            return;
        }

        while (AozaiInkKeys.TOGGLE_WRITING.consumeClick()) {
            if (active && activeMode == CircleMode.CAST) {
                startClosing();
                say(player, "施写阵已关闭");
            } else {
                open(player, CircleMode.CAST);
            }
        }
        while (AozaiInkKeys.TOGGLE_ANCHOR.consumeClick()) {
            if (active && activeMode == CircleMode.ANCHOR) {
                startClosing();
                say(player, "铭刻阵已关闭");
            } else {
                open(player, CircleMode.ANCHOR);
            }
        }
        while (AozaiInkKeys.CLEAR.consumeClick()) {
            clear();
            say(player, "笔迹已清空");
        }
        while (AozaiInkKeys.CANCEL.consumeClick()) {
            startClosing();
            say(player, "字灵书写已取消");
        }
        while (AozaiInkKeys.RECOGNIZE.consumeClick()) {
            recognize(minecraft, player);
        }

        if (active && lastHeldItem != null) {
            ItemStack mainHand = player.getMainHandItem();
            boolean switchedAway = !ItemStack.isSameItemSameComponents(mainHand, lastHeldItem);
            if (switchedAway && !isHoldingInkBrush(player)) {
                finishStroke(true);
                if (!STROKES.isEmpty()) {
                    recognize(minecraft, player);
                }
                close();
                say(player, "切换物品，字灵阵已关闭");
                return;
            }
        }

        if (active && plane != null && player.position().distanceToSqr(plane.center()) > MAX_ACTIVE_DISTANCE_SQR) {
            close();
            say(player, "字灵书写已中断");
            return;
        }

        if (active && plane != null) {
            Vec3 eye = player.getEyePosition(1.0F);
            Vec3 toEye = eye.subtract(plane.center());
            fromBack = toEye.dot(plane.normal()) > 0;
        }

        if (closingPlane != null) {
            long elapsed = System.currentTimeMillis() - closingStartTimeMs;
            float progress = Math.min(1.0F, elapsed / (float) CLOSING_DURATION_MS);
            if (progress >= 0.75F && !closingParticlesSpawned) {
                closingParticlesSpawned = true;
                float easedProgress = 1.0F - (1.0F - progress) * (1.0F - progress);
                float scale = 1.0F - easedProgress * 0.75F;
                spawnCloseParticles(closingPlane, scale);
            }
            if (elapsed >= CLOSING_DURATION_MS) {
                closingPlane = null;
                closingStrokes = null;
            }
        }

        if (!active || minecraft.screen != null) {
            finishStroke(false);
            lastMouseDown = false;
            return;
        }

        sampleMouseStroke(minecraft, player);
        updateAutoRecognize(minecraft, player);
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }

        if (closingPlane != null) {
            long elapsed = System.currentTimeMillis() - closingStartTimeMs;
            if (elapsed < CLOSING_DURATION_MS) {
                float progress = elapsed / (float) CLOSING_DURATION_MS;
                float easedProgress = 1.0F - (1.0F - progress) * (1.0F - progress);
                float scale = 1.0F - easedProgress * 0.75F;
                float alpha = 1.0F - progress;
                RENDERER.renderClosing(
                    event.getPoseStack(),
                    event.getCamera().getPosition(),
                    closingPlane,
                    closingStrokes,
                    scale,
                    alpha
                );
            }
        }

        if (!active || plane == null) {
            return;
        }
        RENDERER.render(
            event.getPoseStack(),
            event.getCamera().getPosition(),
            plane,
            STROKES,
            currentStroke,
            currentHit,
            confirmProgress()
        );
    }

    public static boolean isActive() {
        return active;
    }

    public static void resetSession() {
        close();
    }

    public static void requestOpenCast(Minecraft minecraft, String message) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        open(player, CircleMode.CAST, message);
    }

    public static void requestOpenAnchor(Minecraft minecraft, String message) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        open(player, CircleMode.ANCHOR, message);
    }

    public static void requestClose(Minecraft minecraft, String message) {
        LocalPlayer player = minecraft.player;
        close();
        if (player != null && message != null && !message.isBlank()) {
            say(player, message);
        }
    }

    private static void open(LocalPlayer player, CircleMode mode) {
        open(player, mode, "");
    }

    private static void open(LocalPlayer player, CircleMode mode, String messageOverride) {
        if (!isHoldingInkBrush(player)) {
            say(player, "需要手持字灵符笔");
            return;
        }
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F).normalize();
        plane = InkPlane.create(eye, look);
        active = true;
        activeMode = mode;
        lastHeldItem = player.getMainHandItem().copy();
        clear();
        if (messageOverride != null && !messageOverride.isBlank()) {
            say(player, messageOverride);
        } else {
            say(player, switch (mode) {
                case CAST -> "施写阵已展开：左键写，Enter 识别并附着到自身";
                case ANCHOR -> "铭刻阵已展开：左键写，Enter 识别并锚定到世界";
            });
        }
        spawnOpenParticles(plane);
    }

    private static void startClosing() {
        if (plane != null) {
            closingPlane = plane;
            closingStrokes = STROKES.isEmpty() ? null : new ArrayList<>(STROKES);
            closingStartTimeMs = System.currentTimeMillis();
            closingFromBack = fromBack;
            closingParticlesSpawned = false;
        }
        close();
    }

    private static void close() {
        active = false;
        activeMode = null;
        plane = null;
        currentStroke = null;
        lastMouseDown = false;
        fromBack = false;
        lastHeldItem = null;
        clear();
    }

    private static void clear() {
        STROKES.clear();
        currentStroke = null;
        currentHit = null;
        totalPoints = 0;
        autoRecognizePending = false;
        recognizedSinceChange = false;
        lastPenUpTimeMs = 0L;
    }

    private static void sampleMouseStroke(Minecraft minecraft, LocalPlayer player) {
        boolean mouseDown = GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (!mouseDown) {
            finishStroke(lastMouseDown);
            lastMouseDown = false;
            return;
        }

        if (plane == null) {
            lastMouseDown = true;
            return;
        }

        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F).normalize();
        Optional<PlaneHit> hit = plane.raycast(eye, look);
        currentHit = hit.orElse(null);
        hit.ifPresent(InkInputController::addPoint);
        lastMouseDown = true;
    }

    private static void addPoint(PlaneHit hit) {
        if (totalPoints >= MAX_TOTAL_POINTS) {
            return;
        }

        long now = System.currentTimeMillis();
        if (currentStroke == null || !lastMouseDown) {
            currentStroke = new InkStroke();
            autoRecognizePending = false;
            recognizedSinceChange = false;
        }

        if (!currentStroke.isEmpty()) {
            InkStrokePoint last = currentStroke.last();
            if (currentStroke.points().size() >= MAX_POINTS_PER_STROKE) {
                finishStroke(false);
                currentStroke = new InkStroke();
            } else if (last.distanceTo(hit.u(), hit.v()) < MIN_POINT_DISTANCE && now - last.timeMs() < MAX_POINT_INTERVAL_MS) {
                return;
            }
        }

        currentStroke.add(new InkStrokePoint(hit.u(), hit.v(), now));
        autoRecognizePending = false;
        recognizedSinceChange = false;
        totalPoints++;
    }

    private static void finishStroke(boolean commit) {
        if (currentStroke != null && !currentStroke.isEmpty() && commit) {
            STROKES.add(currentStroke);
            lastPenUpTimeMs = System.currentTimeMillis();
            autoRecognizePending = true;
            recognizedSinceChange = false;
        }
        currentStroke = null;
    }

    private static void recognize(Minecraft minecraft, LocalPlayer player) {
        if (!active || plane == null) {
            say(player, "请先按 G 展开施写阵，或按 V 展开铭刻阵");
            return;
        }
        finishStroke(true);
        if (STROKES.isEmpty()) {
            say(player, "还没有笔迹");
            return;
        }
        if (ocrUnavailable) {
            say(player, "ONNX OCR 不可用，请检查运行环境");
            return;
        }

        try {
            if (ocrEngine == null) {
                say(player, "正在加载 ONNX 字形识别");
                ocrEngine = new OnnxOcrEngine(minecraft);
            }
            float[] input = RASTERIZER.rasterize(STROKES, plane, fromBack);
            List<OcrCandidate> candidates = ocrEngine.recognize(input, 5);
            if (candidates.isEmpty()) {
                spawnFailParticles(plane);
                say(player, "OCR 无汉字结果");
                return;
            }

            OcrCandidate best = candidates.getFirst();
            recognizedSinceChange = true;
            AozaiInkSingleplayerActions.AttachResult attachResult = AozaiInkSingleplayerActions.attachRecognizedMark(
                minecraft,
                player.getUUID(),
                candidates,
                activeMode == CircleMode.ANCHOR,
                markerPosForMode(player)
            );
            AozaiInkSingleplayerActions.applyClientInstruction(minecraft, attachResult.instruction());
            if (attachResult.status() == AozaiInkSingleplayerActions.AttachStatus.UNAVAILABLE
                || attachResult.status() == AozaiInkSingleplayerActions.AttachStatus.REJECTED) {
                spawnFailParticles(plane);
                if (attachResult.message() != null && !attachResult.message().isBlank()) {
                    say(player, attachResult.message());
                }
                clear();
                return;
            }
            if (attachResult.status() == AozaiInkSingleplayerActions.AttachStatus.CANCELED) {
                clear();
                return;
            }
            boolean closeAfterRecognize = InkGlyphClientBehaviorRegistry.closesAfterRecognize(best.character());
            String resultMessage = formatCandidates(candidates) + (activeMode == CircleMode.ANCHOR ? "  已铭刻" : "  已附着");
            if (closeAfterRecognize) {
                close();
            } else {
                clear();
            }
            say(player, resultMessage);
        } catch (Throwable throwable) {
            ocrUnavailable = true;
            spawnFailParticles(plane);
            say(player, "OCR 失败: " + throwable.getClass().getSimpleName());
            AozaiInkMc.LOGGER.error("Aozai Ink OCR failed", throwable);
        }
    }

    private static void updateAutoRecognize(Minecraft minecraft, LocalPlayer player) {
        if (!autoRecognizePending || recognizedSinceChange || STROKES.isEmpty()) {
            return;
        }
        long elapsed = System.currentTimeMillis() - lastPenUpTimeMs;
        if (elapsed >= 700L) {
            autoRecognizePending = false;
            recognize(minecraft, player);
        }
    }

    private static float confirmProgress() {
        if (!autoRecognizePending || recognizedSinceChange || STROKES.isEmpty()) {
            return 0.0F;
        }
        long elapsed = System.currentTimeMillis() - lastPenUpTimeMs;
        return Math.min(1.0F, Math.max(0.0F, elapsed / 700.0F));
    }

    private static String formatCandidates(List<OcrCandidate> candidates) {
        StringBuilder builder = new StringBuilder("识别:");
        int count = Math.min(3, candidates.size());
        for (int i = 0; i < count; i++) {
            OcrCandidate candidate = candidates.get(i);
            builder.append(' ')
                .append(candidate.character())
                .append(' ')
                .append(Math.round(candidate.confidence() * 1000.0F) / 10.0F)
                .append('%');
        }
        return builder.toString();
    }

    private static boolean isHoldingInkBrush(LocalPlayer player) {
        return isInkBrush(player.getMainHandItem()) || isInkBrush(player.getOffhandItem());
    }

    private static boolean isInkBrush(ItemStack stack) {
        return AozaiInkItems.isInkStaff(stack);
    }

    private static InkStaffTier currentStaffTier(LocalPlayer player) {
        return AozaiInkItems.staffTier(player.getMainHandItem())
            .or(() -> AozaiInkItems.staffTier(player.getOffhandItem()))
            .orElse(InkStaffTier.WOOD);
    }

    private static BlockPos markerPosForMode(LocalPlayer player) {
        if (activeMode == CircleMode.ANCHOR && plane != null) {
            return BlockPos.containing(plane.center());
        }
        return player.blockPosition();
    }

    private static void say(LocalPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    private static void spawnOpenParticles(InkPlane plane) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;
            Vec3 center = plane.center();
            Vec3 normal = plane.normal();
            Vec3 right = normal.cross(new Vec3(0, 1, 0)).normalize();
            if (right.lengthSqr() < 0.01) right = normal.cross(new Vec3(1, 0, 0)).normalize();
            Vec3 up = right.cross(normal).normalize();
            float radius = 1.105F;
            for (int i = 0; i < 48; i++) {
                double angle = Math.PI * 2.0 * i / 48.0;
                Vec3 offset = right.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
                Vec3 pos = center.add(offset);
                double vx = Math.cos(angle) * 0.02;
                double vy = 0.01;
                double vz = Math.sin(angle) * 0.02;
                minecraft.level.addParticle(ParticleTypes.SCRAPE, pos.x, pos.y, pos.z, vx, vy, vz);
            }
        } catch (Exception e) {
            // Silently ignore particle errors
        }
    }

    private static void spawnCloseParticles(InkPlane plane, float scale) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;
            Vec3 center = plane.center();
            Vec3 normal = plane.normal();
            Vec3 right = normal.cross(new Vec3(0, 1, 0)).normalize();
            if (right.lengthSqr() < 0.01) right = normal.cross(new Vec3(1, 0, 0)).normalize();
            Vec3 up = right.cross(normal).normalize();
            float radius = 1.105F * scale;
            for (int i = 0; i < 32; i++) {
                double angle = Math.PI * 2.0 * i / 32.0;
                Vec3 offset = right.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
                Vec3 pos = center.add(offset);
                double vx = (Math.random() - 0.5) * 0.05;
                double vy = (Math.random() - 0.5) * 0.05;
                double vz = (Math.random() - 0.5) * 0.05;
                minecraft.level.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, vx, vy, vz);
            }
        } catch (Exception e) {
            // Silently ignore particle errors
        }
    }

    private static void spawnFailParticles(InkPlane plane) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;
            Vec3 center = plane.center();
            Vec3 normal = plane.normal();
            Vec3 right = normal.cross(new Vec3(0, 1, 0)).normalize();
            if (right.lengthSqr() < 0.01) right = normal.cross(new Vec3(1, 0, 0)).normalize();
            Vec3 up = right.cross(normal).normalize();
            float radius = 1.105F;
            for (int i = 0; i < 48; i++) {
                double angle = Math.PI * 2.0 * i / 48.0;
                Vec3 offset = right.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
                Vec3 pos = center.add(offset);
                double vx = Math.cos(angle) * 0.03;
                double vy = 0.02;
                double vz = Math.sin(angle) * 0.03;
                minecraft.level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, vx, vy, vz);
            }
        } catch (Exception e) {
            // Silently ignore particle errors
        }
    }

    private enum CircleMode {
        CAST,
        ANCHOR
    }
}
