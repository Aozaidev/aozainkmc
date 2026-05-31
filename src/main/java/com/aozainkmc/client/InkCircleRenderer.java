package com.aozainkmc.client;

import com.aozainkmc.AozaiInkMc;
import com.aozainkmc.client.input.InkPlane;
import com.aozainkmc.client.input.InkStroke;
import com.aozainkmc.client.input.InkStrokePoint;
import com.aozainkmc.client.input.PlaneHit;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public final class InkCircleRenderer {
    private static final int PROGRESS_SEGMENTS = 128;
    private static final ResourceLocation CIRCLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(AozaiInkMc.MOD_ID, "textures/effect/ink_circle.png");

    private static final float PROGRESS_RING_RADIUS = 1.105F;
    private static final float PROGRESS_RING_WIDTH = 0.095F;
    private static final float STROKE_OUTLINE_WIDTH = 0.085F;
    private static final float STROKE_CORE_WIDTH = 0.056F;
    private static final float CURSOR_SIZE = 0.04F;
    private static final double TEXTURE_LAYER_OFFSET = -0.006D;
    private static final double EFFECT_LAYER_OFFSET = -0.014D;
    private static final double STROKE_LAYER_OFFSET = -0.022D;

    public void render(PoseStack poseStack, Vec3 camera, InkPlane plane, List<InkStroke> strokes, InkStroke currentStroke, PlaneHit currentHit, float confirmProgress) {
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        PoseStack.Pose pose = poseStack.last();
        beginStableTranslucent();
        renderCircleTexture(pose, plane);
        renderDynamicGeometry(pose, plane, strokes, currentStroke, currentHit, confirmProgress);
        endStableTranslucent();

        poseStack.popPose();
    }

    public void renderClosing(PoseStack poseStack, Vec3 camera, InkPlane plane, List<InkStroke> strokes, float scale, float alpha) {
        poseStack.pushPose();

        Vec3 center = plane.center();
        poseStack.translate(center.x - camera.x, center.y - camera.y, center.z - camera.z);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-center.x, -center.y, -center.z);

        PoseStack.Pose pose = poseStack.last();
        beginStableTranslucent();
        renderCircleTexture(pose, plane, alpha);
        if (strokes != null && !strokes.isEmpty()) {
            renderClosingStrokes(pose, plane, strokes, alpha);
        }
        endStableTranslucent();

        poseStack.popPose();
    }

    private void renderCircleTexture(PoseStack.Pose pose, InkPlane plane, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, CIRCLE_TEXTURE);

        double angle = -(System.currentTimeMillis() % 45000L) / 45000.0 * Math.PI * 2.0;
        float cosA = (float) Math.cos(angle);
        float sinA = (float) Math.sin(angle);

        int segments = 64;
        float size = 1.22F;
        int alphaByte = Math.round(245 * alpha);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX_COLOR);

        textureVertex(pose, buffer, plane, new Uv(0, 0),
            rotateU(0.5F, 0.5F, cosA, sinA), rotateV(0.5F, 0.5F, cosA, sinA),
            255, 255, 255, alphaByte);

        for (int i = 0; i <= segments; i++) {
            double a = Math.PI * 2.0D * i / segments;
            float u = (float) Math.cos(a) * size;
            float v = (float) Math.sin(a) * size;
            float texU = (u / size + 1.0F) * 0.5F;
            float texV = 1.0F - (v / size + 1.0F) * 0.5F;
            textureVertex(pose, buffer, plane, new Uv(u, v),
                rotateU(texU, texV, cosA, sinA), rotateV(texU, texV, cosA, sinA),
                255, 255, 255, alphaByte);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void renderClosingStrokes(PoseStack.Pose pose, InkPlane plane, List<InkStroke> strokes, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (InkStroke stroke : strokes) {
            renderStroke(pose, buffer, plane, stroke, alpha);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void renderStroke(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, InkStroke stroke, float alpha) {
        List<InkStrokePoint> points = stroke.points();
        if (points.isEmpty()) {
            return;
        }

        int outlineAlpha = Math.round(255 * alpha);
        int coreAlpha = Math.round(255 * alpha);

        for (InkStrokePoint point : points) {
            disk(pose, quads, plane, point.u(), point.v(), STROKE_OUTLINE_WIDTH * 0.75F, STROKE_LAYER_OFFSET + 0.002D, 180, 140, 60, outlineAlpha);
        }
        for (int i = 1; i < points.size(); i++) {
            InkStrokePoint a = points.get(i - 1);
            InkStrokePoint b = points.get(i);
            ribbon(pose, quads, plane, new Uv(a.u(), a.v()), new Uv(b.u(), b.v()), STROKE_OUTLINE_WIDTH * 1.4F, STROKE_LAYER_OFFSET + 0.002D, 180, 140, 60, outlineAlpha);
        }

        for (InkStrokePoint point : points) {
            disk(pose, quads, plane, point.u(), point.v(), STROKE_OUTLINE_WIDTH * 0.55F, STROKE_LAYER_OFFSET, 170, 55, 35, outlineAlpha);
        }
        for (int i = 1; i < points.size(); i++) {
            InkStrokePoint a = points.get(i - 1);
            InkStrokePoint b = points.get(i);
            ribbon(pose, quads, plane, new Uv(a.u(), a.v()), new Uv(b.u(), b.v()), STROKE_OUTLINE_WIDTH, STROKE_LAYER_OFFSET, 170, 55, 35, outlineAlpha);
        }

        for (InkStrokePoint point : points) {
            disk(pose, quads, plane, point.u(), point.v(), STROKE_CORE_WIDTH * 0.5F, STROKE_LAYER_OFFSET - 0.004D, 130, 40, 25, coreAlpha);
        }
        for (int i = 1; i < points.size(); i++) {
            InkStrokePoint a = points.get(i - 1);
            InkStrokePoint b = points.get(i);
            ribbon(pose, quads, plane, new Uv(a.u(), a.v()), new Uv(b.u(), b.v()), STROKE_CORE_WIDTH, STROKE_LAYER_OFFSET - 0.004D, 130, 40, 25, coreAlpha);
        }
    }

    private void beginStableTranslucent() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
    }

    private void endStableTranslucent() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderCircleTexture(PoseStack.Pose pose, InkPlane plane) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, CIRCLE_TEXTURE);

        double angle = -(System.currentTimeMillis() % 45000L) / 45000.0 * Math.PI * 2.0;
        float cosA = (float) Math.cos(angle);
        float sinA = (float) Math.sin(angle);

        int segments = 64;
        float size = 1.22F;

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX_COLOR);

        textureVertex(pose, buffer, plane, new Uv(0, 0),
            rotateU(0.5F, 0.5F, cosA, sinA), rotateV(0.5F, 0.5F, cosA, sinA),
            255, 255, 255, 245);

        for (int i = 0; i <= segments; i++) {
            double a = Math.PI * 2.0D * i / segments;
            float u = (float) Math.cos(a) * size;
            float v = (float) Math.sin(a) * size;
            float texU = (u / size + 1.0F) * 0.5F;
            float texV = 1.0F - (v / size + 1.0F) * 0.5F;
            textureVertex(pose, buffer, plane, new Uv(u, v),
                rotateU(texU, texV, cosA, sinA), rotateV(texU, texV, cosA, sinA),
                255, 255, 255, 245);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static float rotateU(float u, float v, float cosA, float sinA) {
        return cosA * (u - 0.5F) - sinA * (v - 0.5F) + 0.5F;
    }

    private static float rotateV(float u, float v, float cosA, float sinA) {
        return sinA * (u - 0.5F) + cosA * (v - 0.5F) + 0.5F;
    }

    private void renderDynamicGeometry(PoseStack.Pose pose, InkPlane plane, List<InkStroke> strokes, InkStroke currentStroke, PlaneHit currentHit, float confirmProgress) {
        if (confirmProgress <= 0.0F && strokes.isEmpty() && currentStroke == null && currentHit == null) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderConfirmProgress(pose, buffer, plane, confirmProgress);
        renderStrokes(pose, buffer, plane, strokes, currentStroke);
        renderCursor(pose, buffer, plane, currentHit);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void renderConfirmProgress(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, float progress) {
        if (progress <= 0.0F) {
            return;
        }

        float clamped = Math.min(1.0F, progress);
        int segments = Math.max(1, Math.round(PROGRESS_SEGMENTS * clamped));
        double startAngle = Math.PI / 2.0D;
        double sweep = -Math.PI * 2.0D * clamped;

        renderArc(pose, quads, plane, PROGRESS_RING_RADIUS, PROGRESS_RING_WIDTH * 0.5F, startAngle, sweep, segments, 70, 130, 140, 255);
        renderArc(pose, quads, plane, PROGRESS_RING_RADIUS, PROGRESS_RING_WIDTH * 0.35F, startAngle, sweep, segments, 90, 95, 100, 255);
    }

    private void renderArc(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, float radius, float width, double startAngle, double sweep, int segments, int red, int green, int blue, int alpha) {
        renderArcAt(pose, quads, plane, radius, width, startAngle, sweep, segments, red, green, blue, alpha, EFFECT_LAYER_OFFSET);
    }

    private void renderArcAt(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, float radius, float width, double startAngle, double sweep, int segments, int red, int green, int blue, int alpha, double layerOffset) {
        float inner = radius - width * 0.5F;
        float outer = radius + width * 0.5F;
        for (int i = 0; i < segments; i++) {
            double a0 = startAngle + sweep * i / segments;
            double a1 = startAngle + sweep * (i + 1) / segments;
            Uv p0 = polar(inner, a0);
            Uv p1 = polar(outer, a0);
            Uv p2 = polar(outer, a1);
            Uv p3 = polar(inner, a1);
            quad(pose, quads, plane, p0, p1, p2, p3, layerOffset, red, green, blue, alpha);
        }
    }

    private void renderStrokes(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, List<InkStroke> strokes, InkStroke currentStroke) {
        for (InkStroke stroke : strokes) {
            renderStroke(pose, quads, plane, stroke);
        }
        if (currentStroke != null) {
            renderStroke(pose, quads, plane, currentStroke);
        }
    }

    private void renderStroke(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, InkStroke stroke) {
        List<InkStrokePoint> points = stroke.points();
        if (points.isEmpty()) {
            return;
        }

        for (InkStrokePoint point : points) {
            disk(pose, quads, plane, point.u(), point.v(), STROKE_OUTLINE_WIDTH * 0.75F, STROKE_LAYER_OFFSET + 0.002D, 180, 140, 60, 255);
        }
        for (int i = 1; i < points.size(); i++) {
            InkStrokePoint a = points.get(i - 1);
            InkStrokePoint b = points.get(i);
            ribbon(pose, quads, plane, new Uv(a.u(), a.v()), new Uv(b.u(), b.v()), STROKE_OUTLINE_WIDTH * 1.4F, STROKE_LAYER_OFFSET + 0.002D, 180, 140, 60, 255);
        }

        for (InkStrokePoint point : points) {
            disk(pose, quads, plane, point.u(), point.v(), STROKE_OUTLINE_WIDTH * 0.55F, STROKE_LAYER_OFFSET, 170, 55, 35, 255);
        }
        for (int i = 1; i < points.size(); i++) {
            InkStrokePoint a = points.get(i - 1);
            InkStrokePoint b = points.get(i);
            ribbon(pose, quads, plane, new Uv(a.u(), a.v()), new Uv(b.u(), b.v()), STROKE_OUTLINE_WIDTH, STROKE_LAYER_OFFSET, 170, 55, 35, 255);
        }

        for (InkStrokePoint point : points) {
            disk(pose, quads, plane, point.u(), point.v(), STROKE_CORE_WIDTH * 0.5F, STROKE_LAYER_OFFSET - 0.004D, 130, 40, 25, 255);
        }
        for (int i = 1; i < points.size(); i++) {
            InkStrokePoint a = points.get(i - 1);
            InkStrokePoint b = points.get(i);
            ribbon(pose, quads, plane, new Uv(a.u(), a.v()), new Uv(b.u(), b.v()), STROKE_CORE_WIDTH, STROKE_LAYER_OFFSET - 0.004D, 130, 40, 25, 255);
        }
    }

    private void renderCursor(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, PlaneHit currentHit) {
        if (currentHit == null) {
            return;
        }

        float u = currentHit.u();
        float v = currentHit.v();
        ribbon(pose, quads, plane, new Uv(u - CURSOR_SIZE, v), new Uv(u + CURSOR_SIZE, v), 0.014F, STROKE_LAYER_OFFSET - 0.008D, 35, 40, 45, 230);
        ribbon(pose, quads, plane, new Uv(u, v - CURSOR_SIZE), new Uv(u, v + CURSOR_SIZE), 0.014F, STROKE_LAYER_OFFSET - 0.008D, 35, 40, 45, 230);
        ribbon(pose, quads, plane, new Uv(u - CURSOR_SIZE, v), new Uv(u + CURSOR_SIZE, v), 0.007F, STROKE_LAYER_OFFSET - 0.012D, 255, 255, 255, 255);
        ribbon(pose, quads, plane, new Uv(u, v - CURSOR_SIZE), new Uv(u, v + CURSOR_SIZE), 0.007F, STROKE_LAYER_OFFSET - 0.012D, 255, 255, 255, 255);
    }

    private void ribbon(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, Uv a, Uv b, float width, double layerOffset, int red, int green, int blue, int alpha) {
        float du = b.u - a.u;
        float dv = b.v - a.v;
        float length = (float) Math.sqrt(du * du + dv * dv);
        if (length < 0.0001F) {
            disk(pose, quads, plane, a.u, a.v, width * 0.5F, layerOffset, red, green, blue, alpha);
            return;
        }

        float offsetU = -dv / length * width * 0.5F;
        float offsetV = du / length * width * 0.5F;
        quad(pose, quads, plane,
            new Uv(a.u + offsetU, a.v + offsetV),
            new Uv(a.u - offsetU, a.v - offsetV),
            new Uv(b.u - offsetU, b.v - offsetV),
            new Uv(b.u + offsetU, b.v + offsetV),
            layerOffset, red, green, blue, alpha);
    }

    private void disk(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, float centerU, float centerV, float radius, double layerOffset, int red, int green, int blue, int alpha) {
        int segments = 18;
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0D * i / segments;
            double a1 = Math.PI * 2.0D * (i + 1) / segments;
            Uv p0 = new Uv(centerU, centerV);
            Uv p1 = polarOffset(centerU, centerV, radius, a0);
            Uv p2 = polarOffset(centerU, centerV, radius, (a0 + a1) * 0.5D);
            Uv p3 = polarOffset(centerU, centerV, radius, a1);
            quad(pose, quads, plane, p0, p1, p2, p3, layerOffset, red, green, blue, alpha);
        }
    }

    private void quad(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, Uv a, Uv b, Uv c, Uv d, double layerOffset, int red, int green, int blue, int alpha) {
        vertex(pose, quads, plane, a, layerOffset, red, green, blue, alpha);
        vertex(pose, quads, plane, b, layerOffset, red, green, blue, alpha);
        vertex(pose, quads, plane, c, layerOffset, red, green, blue, alpha);
        vertex(pose, quads, plane, d, layerOffset, red, green, blue, alpha);
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer quads, InkPlane plane, Uv uv, double layerOffset, int red, int green, int blue, int alpha) {
        Vec3 point = plane.pointAt(uv.u, uv.v).add(plane.normal().scale(layerOffset));
        quads.addVertex(pose, (float) point.x, (float) point.y, (float) point.z).setColor(red, green, blue, alpha);
    }

    private void textureVertex(PoseStack.Pose pose, VertexConsumer texture, InkPlane plane, Uv uv, float textureU, float textureV, int red, int green, int blue, int alpha) {
        Vec3 point = plane.pointAt(uv.u, uv.v).add(plane.normal().scale(TEXTURE_LAYER_OFFSET));
        texture.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
            .setUv(textureU, textureV)
            .setColor(red, green, blue, alpha);
    }

    private Uv polar(float radius, double angle) {
        return new Uv((float) Math.cos(angle) * radius, (float) Math.sin(angle) * radius);
    }

    private Uv polarOffset(float centerU, float centerV, float radius, double angle) {
        return new Uv(centerU + (float) Math.cos(angle) * radius, centerV + (float) Math.sin(angle) * radius);
    }

    private record Uv(float u, float v) {
    }
}
