package jgl.ui;

import org.lwjgl.nanovg.NVGPaint;

import static org.lwjgl.nanovg.NanoVG.*;

/**
 * Simple NanoVG 9-patch renderer.
 * <p>
 * Usage:
 * NvgNinePatch np = new NvgNinePatch(imageId, imgW, imgH, left, right, top, bottom);
 * NinePatch.drawNinePatch(vg, np, x, y, w, h);
 */
public final class NinePatch {

    private NinePatch() {
    }

    /**
     * Metadata for one 9-patch image.
     */
    public static class NvgNinePatch {
        public final int imageId;
        public final float imgW, imgH;
        public final float left, right, top, bottom;

        public NvgNinePatch(int imageId, float imgW, float imgH, float left, float right, float top, float bottom) {
            this.imageId = imageId;
            this.imgW = imgW;
            this.imgH = imgH;
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
    }

    /**
     * Draw a 9-patch scaled to (x,y,w,h).
     */
    public static void drawNinePatch(long vg, NvgNinePatch np, float x, float y, float w, float h) {

        float L = np.left;
        float R = np.right;
        float T = np.top;
        float B = np.bottom;

        float midW = Math.max(0.0f, w - L - R);
        float midH = Math.max(0.0f, h - T - B);

        NVGPaint paint = NVGPaint.calloc();

        // Corners
        drawPatch(vg, np, x, y, L, T, 0, 0, paint);          // TL
        drawPatch(vg, np, x + w - R, y, R, T, np.imgW - R, 0, paint);          // TR
        drawPatch(vg, np, x, y + h - B, L, B, 0, np.imgH - B, paint);          // BL
        drawPatch(vg, np, x + w - R, y + h - B, R, B, np.imgW - R, np.imgH - B, paint);          // BR

        // Edges
        drawPatch(vg, np, x + L, y, midW, T, L, 0, paint);          // Top
        drawPatch(vg, np, x + L, y + h - B, midW, B, L, np.imgH - B, paint);          // Bottom
        drawPatch(vg, np, x, y + T, L, midH, 0, T, paint);          // Left
        drawPatch(vg, np, x + w - R, y + T, R, midH, np.imgW - R, T, paint);          // Right

        // Center
        drawPatch(vg, np, x + L, y + T, midW, midH, L, T, paint);

        paint.free();
    }

    /**
     * Draw a single patch region.
     *
     * @param sx,sy source top-left in the original image
     *              (in pixels, same space as imgW/imgH).
     */
    private static void drawPatch(long vg, NvgNinePatch np, float x, float y, float w, float h, float sx, float sy, NVGPaint paint) {

        if (w <= 0 || h <= 0) return;

        // We use an image pattern for the whole texture, but shift the pattern
        // so that the destination rect samples from (sx,sy) of the source.
        nvgSave(vg);

        nvgBeginPath(vg);
        nvgRect(vg, x, y, w, h);

        // Pattern covers the entire original image (imgW x imgH)
        // and is offset so that the dest rect maps to the wanted sub-rect.
        nvgFillPaint(vg, nvgImagePattern(vg, x - sx,           // shift so (x,y) corresponds to (sx,sy) in texture
                y - sy, np.imgW, np.imgH, 0.0f, np.imageId, 1.0f, paint));

        nvgFill(vg);
        nvgRestore(vg);
    }
}
