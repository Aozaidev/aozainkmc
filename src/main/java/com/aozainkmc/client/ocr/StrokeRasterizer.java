package com.aozainkmc.client.ocr;

import com.aozainkmc.client.input.InkPlane;
import com.aozainkmc.client.input.InkStroke;
import com.aozainkmc.client.input.InkStrokePoint;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

public final class StrokeRasterizer {
    private static final int IMAGE_SIZE = 64;
    private static final int BACKGROUND = 255;
    private static final int INK = 60;

    public float[] rasterize(List<InkStroke> strokes, InkPlane plane) {
        BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(BACKGROUND, BACKGROUND, BACKGROUND));
        graphics.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);

        Bounds bounds = Bounds.from(strokes, plane.radius());
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(INK, INK, INK));
        graphics.setStroke(new BasicStroke(4.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (InkStroke stroke : strokes) {
            List<InkStrokePoint> points = stroke.points();
            if (points.size() == 1) {
                int x = bounds.x(points.getFirst().u());
                int y = bounds.y(points.getFirst().v());
                graphics.fillOval(x - 2, y - 2, 4, 4);
                continue;
            }
            for (int i = 1; i < points.size(); i++) {
                InkStrokePoint a = points.get(i - 1);
                InkStrokePoint b = points.get(i);
                graphics.drawLine(bounds.x(a.u()), bounds.y(a.v()), bounds.x(b.u()), bounds.y(b.v()));
            }
        }
        graphics.dispose();

        float[] input = new float[IMAGE_SIZE * IMAGE_SIZE];
        for (int y = 0; y < IMAGE_SIZE; y++) {
            for (int x = 0; x < IMAGE_SIZE; x++) {
                int gray = image.getRaster().getSample(x, y, 0);
                input[y * IMAGE_SIZE + x] = (gray / 255.0F - 0.5F) / 0.5F;
            }
        }
        return input;
    }

    private record Bounds(float minU, float minV, float scale, float offsetX, float offsetY) {
        static Bounds from(List<InkStroke> strokes, float radius) {
            float minU = Float.POSITIVE_INFINITY;
            float minV = Float.POSITIVE_INFINITY;
            float maxU = Float.NEGATIVE_INFINITY;
            float maxV = Float.NEGATIVE_INFINITY;

            for (InkStroke stroke : strokes) {
                for (InkStrokePoint point : stroke.points()) {
                    minU = Math.min(minU, point.u());
                    minV = Math.min(minV, point.v());
                    maxU = Math.max(maxU, point.u());
                    maxV = Math.max(maxV, point.v());
                }
            }

            if (!Float.isFinite(minU)) {
                return fullPlane(radius);
            }

            float width = Math.max(maxU - minU, 0.001F);
            float height = Math.max(maxV - minV, 0.001F);
            float pad = Math.max(width, height) * 0.18F;
            minU -= pad;
            maxU += pad;
            minV -= pad;
            maxV += pad;
            width = maxU - minU;
            height = maxV - minV;

            float scale = (IMAGE_SIZE - 4.0F) / Math.max(width, height);
            float drawW = width * scale;
            float drawH = height * scale;
            float offsetX = (IMAGE_SIZE - drawW) * 0.5F;
            float offsetY = (IMAGE_SIZE - drawH) * 0.5F;
            return new Bounds(minU, minV, scale, offsetX, offsetY);
        }

        static Bounds fullPlane(float radius) {
            float min = -radius;
            float scale = (IMAGE_SIZE - 4.0F) / (radius * 2.0F);
            return new Bounds(min, min, scale, 2.0F, 2.0F);
        }

        int x(float u) {
            return Math.round(offsetX + (u - minU) * scale);
        }

        int y(float v) {
            return Math.round(IMAGE_SIZE - 1.0F - (offsetY + (v - minV) * scale));
        }
    }
}
