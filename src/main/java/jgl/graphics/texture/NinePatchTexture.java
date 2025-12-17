package jgl.graphics.texture;

import jgl.graphics.Color;

import java.util.function.BiConsumer;

/**
 * Simple nine-patch renderer built on top of {@link Texture}.
 *
 * <p>This class does <b>not</b> do any CPU-side pixel baking or resizing.
 * It simply interprets the source {@link TextureData} as a 3x3 nine-patch
 * (corners, edges, center), and in {@link #draw()} it renders nine scaled
 * quads using the original texture buffer.</p>
 *
 * <p>The nine-patch is defined by pixel borders: left, right, top, bottom.
 * These borders are measured in the source texture's pixel space.</p>
 *
 * <p>At render time, the nine slices are scaled to fit the current
 * {@link #getWidth()} and {@link #getHeight()} dimensions. No additional
 * textures or FBOs are created, and memory usage stays low and stable.</p>
 *
 * <p><b>Note:</b> This class assumes you will not rotate or flip the
 * nine-patch. It is intended for axis-aligned UI panels. Using
 * {@link #setRotation(float)} or flip methods may produce undefined results.</p>
 *
 * @author Albert Beaupre
 * @since December 1st, 2025
 */
public class NinePatchTexture extends Texture {

    /**
     * Pixel border sizes in the source texture.
     */
    private int left;
    private int right;
    private int top;
    private int bottom;

    /**
     * Creates a NinePatchTexture from an image path and border sizes.
     *
     * @param path   image file path
     * @param left   left border in pixels
     * @param right  right border in pixels
     * @param top    top border in pixels
     * @param bottom bottom border in pixels
     */
    public NinePatchTexture(String path, int left, int right, int top, int bottom) {
        this(TextureData.load(path), left, right, top, bottom);
    }

    /**
     * Creates a NinePatchTexture from an existing {@link TextureData}
     * and pixel border sizes.
     *
     * @param data   source texture buffer
     * @param left   left border in pixels
     * @param right  right border in pixels
     * @param top    top border in pixels
     * @param bottom bottom border in pixels
     */
    public NinePatchTexture(TextureData data, int left, int right, int top, int bottom) {
        super(data);
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;

        // By default, the logical size of the patch is the full source size.
        setSize(data.width(), data.height());
    }

    /**
     * Draws the nine-patch using 9 quads from the same underlying texture.
     *
     * <p>This method:
     * <ol>
     *     <li>Computes the destination rectangles for corners, edges, and center</li>
     *     <li>For each slice, sets an appropriate source region</li>
     *     <li>Sets position and size for that slice</li>
     *     <li>Calls {@link Texture#draw()} to render a quad</li>
     * </ol>
     *
     * <p>All slices share the same texture object and GL state, so the cost
     * is very low (9 small draw calls).</p>
     */
    @Override
    public void draw() {

        TextureData data = getData();
        int srcW = data.width();
        int srcH = data.height();

        float baseX = getX();
        float baseY = getY();
        float totalW = getWidth();
        float totalH = getHeight();

        // Rotation pivot (same as Texture)
        float originX = getOrigin().getX();
        float originY = getOrigin().getY();

        float rot = getRotation();
        float rad = (float) Math.toRadians(-rot);
        float sin = (float) Math.sin(rad);
        float cos = (float) Math.cos(rad);

        float stretchW = Math.max(0f, totalW - left - right);
        float stretchH = Math.max(0f, totalH - top - bottom);

        float x1 = baseX + left;
        float x2 = baseX + left + stretchW;

        float y1 = baseY + top;
        float y2 = baseY + top + stretchH;

        Color originalColor = getColor();

        // Rotation helper
        BiConsumer<float[], float[]> rotatePoint = (in, out) -> {
            float lx = in[0] - originX;
            float ly = in[1] - originY;

            float rx = lx * cos - ly * sin;
            float ry = lx * sin + ly * cos;

            out[0] = rx + originX;
            out[1] = ry + originY;
        };

        float[] p = new float[2];
        float[] r = new float[2];

        // Slice drawer using rotated top-left corners
        BiConsumer<float[], float[]> drawSlice = (pos, size) -> {
            p[0] = pos[0];
            p[1] = pos[1];
            rotatePoint.accept(p, r);

            super.setPosition(r[0], r[1]);
            super.setSize(size[0], size[1]);
            super.draw();
        };

        // Store source regions in an array so rotation code doesn't clutter logic
        float[][] slicePos = {{baseX, baseY}, {x1, baseY}, {x2, baseY}, {baseX, y1}, {x1, y1}, {x2, y1}, {baseX, y2}, {x1, y2}, {x2, y2}};
        float[][] sliceSize = {{left, top}, {stretchW, top}, {right, top}, {left, stretchH}, {stretchW, stretchH}, {right, stretchH}, {left, bottom}, {stretchW, bottom}, {right, bottom}};
        int[][] srcRegions = {{0, 0, left, top}, {left, 0, srcW - right, top}, {srcW - right, 0, srcW, top}, {0, top, left, srcH - bottom}, {left, top, srcW - right, srcH - bottom}, {srcW - right, top, srcW, srcH - bottom}, {0, srcH - bottom, left, srcH}, {left, srcH - bottom, srcW - right, srcH}, {srcW - right, srcH - bottom, srcW, srcH}};

        for (int i = 0; i < 9; i++) {
            int[] uv = srcRegions[i];
            setRegion(uv[0], uv[1], uv[2], uv[3]);
            drawSlice.accept(slicePos[i], sliceSize[i]);
        }

        // Restore full UV and original transform
        setRegion(0, 0, srcW, srcH);
        super.setPosition(baseX, baseY);
        super.setSize(totalW, totalH);
        setColor(originalColor);
    }


    /**
     * Retrieves the size of the left border in pixels.
     *
     * @return the left border size in pixels
     */
    public int getLeft() {
        return left;
    }

    /**
     * Retrieves the size of the right border in pixels.
     *
     * @return the right border size in pixels
     */
    public int getRight() {
        return right;
    }

    /**
     * Retrieves the size of the top border in pixels.
     *
     * @return the top border size in pixels
     */
    public int getTop() {
        return top;
    }

    /**
     * Retrieves the size of the bottom border in pixels.
     *
     * @return the bottom border size in pixels
     */
    public int getBottom() {
        return bottom;
    }

    /**
     * Sets the size of the left border in pixels.
     *
     * @param left the left border size in pixels to be set
     */
    public void setLeft(int left) {
        this.left = left;
    }

    /**
     * Sets the size of the right border in pixels.
     *
     * @param right the right border size in pixels to be set
     */
    public void setRight(int right) {
        this.right = right;
    }

    /**
     * Sets the size of the top border in pixels.
     *
     * @param top the top border size in pixels to be set
     */
    public void setTop(int top) {
        this.top = top;
    }

    /**
     * Sets the size of the bottom border in pixels.
     *
     * @param bottom the bottom border size in pixels to be set
     */
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }
}
