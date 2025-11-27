package jgl.graphics;

import jgl.math.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Represents a 2D textured quad rendered using legacy OpenGL (fixed-function pipeline).
 *
 * <p>This class manages a texture's transform (position, size, rotation, origin),
 * its UV region, and its vertex data. It pre-allocates vertex and UV buffers to avoid
 * repeated memory allocation and updates them incrementally whenever properties change.</p>
 *
 * <p>The rendering strategy uses {@code glVertexPointer}, {@code glTexCoordPointer},
 * and {@code glDrawArrays(GL_QUADS)}, which means this class is intended for
 * compatibility-style rendering or simple sprite systems without modern shaders.</p>
 *
 * <p>Key design goals:</p>
 * <ul>
 *     <li>Zero allocations during draw calls</li>
 *     <li>Local-space vertex generation for fast rotation & translation</li>
 *     <li>Support for arbitrary texture subregions (sprite sheets)</li>
 *     <li>Manual origin control for intuitive rotation + scaling behavior</li>
 * </ul>
 *
 * @author Albert Beaupre
 * @see TextureData
 * @since November 26th, 2025
 */
public class Texture {

    /**
     * Global flag indicating whether fixed-function texture rendering has been initialized.
     * Enables client states only once to avoid redundant calls.
     */
    private static boolean textureRenderingEnabled = false;

    /**
     * The OpenGL texture handle.
     */
    private final int textureID;

    /**
     * Vertex buffer storing 4 (x,y) pairs for the quad.
     * Pre-allocated once for performance. Updated on position/size/rotation changes.
     */
    private final FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(8);

    /**
     * UV coordinate buffer storing 4 (u,v) pairs.
     * Updated only when the texture region is changed.
     */
    private final FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(8);

    /**
     * Local-space vertex positions for the quad before rotation/translation.
     * Values represent offsets from the origin. Rotation and translation are applied
     * in the {@link #updateVertexBuffer()} step.
     */
    private final float[] localVertices = new float[8];

    /**
     * The origin point relative to the texture's top-left corner.
     * Rotation and translation pivot around this point.
     */
    private final Vector2f origin = new Vector2f(0f, 0f);

    /**
     * Current filtering mode used for this texture.
     */
    private TextureFilter filter = TextureFilter.LINEAR;

    /**
     * Width of the rendered quad in world units.
     */
    private float width;

    /**
     * Height of the rendered quad in world units.
     */
    private float height;

    /**
     * World-space X position of the quad.
     */
    private float x;

    /**
     * World-space Y position of the quad.
     */
    private float y;

    /**
     * UV region left coordinate.
     */
    private float texLeft;

    /**
     * UV region top coordinate.
     */
    private float texTop;

    /**
     * UV region right coordinate, defaults to 1.
     */
    private float texRight = 1f;

    /**
     * UV region bottom coordinate, defaults to 1.
     */
    private float texBottom = 1f;

    /**
     * Rotation angle in degrees.
     */
    private float rotation;

    /**
     * Cached sine and cosine of the rotation angle.
     */
    private float sinRot;
    private float cosRot = 1f;

    /**
     * Tracks whether the texture's UVs are currently flipped horizontally.
     */
    private boolean flippedX = false;

    /**
     * Tracks whether the texture's UVs are currently flipped vertically.
     */
    private boolean flippedY = false;

    /**
     * Horizontal scaling factor applied during vertex generation.
     */
    private float scaleX = 1f;

    /**
     * Vertical scaling factor applied during vertex generation.
     */
    private float scaleY = 1f;

    /**
     * Loads a texture from a file path using {@link TextureData}.
     *
     * @param path path to the image file
     */
    public Texture(String path) {
        this(TextureData.loadTexture(path));
    }

    /**
     * Loads a texture from raw image data.
     *
     * @param data PNG/JPEG/etc. byte array
     */
    public Texture(byte[] data) {
        this(TextureData.loadTexture(data));
    }

    /**
     * Internal constructor used after {@link TextureData} creation.
     * Initializes vertex data, UV buffers, and enables client states on first usage.
     *
     * @param texture the texture data container
     */
    public Texture(TextureData texture) {
        this.textureID = texture.ID();
        this.width = texture.width();
        this.height = texture.height();
        updateLocalVertices();
        updateUVBuffer();

        if (!textureRenderingEnabled) {
            textureRenderingEnabled = true;
            glEnableClientState(GL_VERTEX_ARRAY);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glEnable(GL_TEXTURE_2D);
        }
    }

    /**
     * Sets the texture filtering mode used when OpenGL scales the texture.
     *
     * <p>This updates both the MIN and MAG filters. The texture must be bound
     * before applying filter parameters, so this method binds the texture,
     * updates the filtering mode, and leaves the texture bound.</p>
     *
     * @param filter the filtering mode to apply
     */
    public void setFilter(TextureFilter filter) {
        this.filter = filter;
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter.minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter.magFilter);
    }

    /**
     * Sets the scale factors applied to the texture quad.
     *
     * <p>This modifies the size of the rendered quad without changing the
     * texture's width or height values. Both scaling factors are applied during
     * the vertex update step.</p>
     *
     * @param sx horizontal scale factor
     * @param sy vertical scale factor
     */
    public void setScale(float sx, float sy) {
        // Adjust origin proportionally to new scale
        float oldScaleX = this.scaleX;
        float oldScaleY = this.scaleY;

        // Prevent divide-by-zero and invalid first-scale
        if (oldScaleX != 0) origin.x = origin.x * (sx / oldScaleX);
        if (oldScaleY != 0) origin.y = origin.y * (sy / oldScaleY);

        this.scaleX = sx;
        this.scaleY = sy;

        updateLocalVertices();
        updateVertexBuffer();
    }


    /**
     * Retrieves the horizontal scaling factor applied to the texture.
     *
     * @return the current horizontal scale factor (scaleX) of the texture
     */
    public float getScaleX() {
        return scaleX;
    }

    /**
     * Retrieves the vertical scaling factor applied to the texture.
     *
     * @return the current vertical scale factor (scaleY) of the texture
     */
    public float getScaleY() {
        return scaleY;
    }

    /**
     * Flips the texture horizontally by swapping the left and right UV coordinates.
     *
     * <p>This operation mirrors the texture along the Y-axis (left ↔ right).
     * Only the UV mapping is altered—no vertex positions or orientation values
     * are modified. This makes the method extremely fast and suitable for
     * sprite mirroring, character direction changes, and flipping texture regions
     * defined via {@link #setTextureRegion(float, float, float, float)}.</p>
     *
     * <p>After swapping the UV values, {@link #updateUVBuffer()} is called
     * to commit the new UV layout to the GPU client-side buffer.</p>
     */
    public void flipX() {
        float temp = texLeft;
        texLeft = texRight;
        texRight = temp;
        updateUVBuffer();
    }

    /**
     * Flips the texture vertically by swapping the top and bottom UV coordinates.
     *
     * <p>This operation mirrors the texture along the X-axis (top ↔ bottom).
     * Like {@link #flipX()}, this affects only UV coordinates and leaves the
     * geometry, origin, and rotation untouched. Vertical flipping is useful
     * for texture formats loaded upside-down, sprite animations, or user-driven
     * visual transformations.</p>
     *
     * <p>After the UV swap, {@link #updateUVBuffer()} is invoked to refresh the
     * UV coordinate buffer used during rendering.</p>
     */
    public void flipY() {
        float temp = texTop;
        texTop = texBottom;
        texBottom = temp;
        updateUVBuffer();
    }

    /**
     * Enables or disables horizontal flipping of the texture's UV coordinates.
     *
     * <p>Horizontal flipping swaps the left and right UV values, causing the
     * texture to mirror along the Y-axis (left ↔ right). This is purely a UV
     * transformation and does not affect the vertex positions or rotation.</p>
     *
     * <p>The operation is performed only when the desired flip state differs from
     * the current {@link #flippedX} state, ensuring that UV values are not
     * repeatedly swapped.</p>
     *
     * @param flip {@code true} to flip the texture horizontally,
     *             {@code false} to restore the original UV orientation
     */
    public void setFlipX(boolean flip) {
        if (flip != flippedX) {
            flipX();           // Performs the actual UV swap
            flippedX = flip;   // Update state tracking
        }
    }

    /**
     * Enables or disables vertical flipping of the texture's UV coordinates.
     *
     * <p>Vertical flipping swaps the top and bottom UV values, causing the
     * texture to mirror along the X-axis (top ↔ bottom). Like horizontal flipping,
     * this affects only UV mapping and leaves geometry unchanged.</p>
     *
     * <p>The operation is applied only if the requested flip state differs from
     * the current {@link #flippedY} value, preventing repeated and unnecessary
     * UV swaps.</p>
     *
     * @param flip {@code true} to flip the texture vertically,
     *             {@code false} to restore the original UV orientation
     */
    public void setFlipY(boolean flip) {
        if (flip != flippedY) {
            flipY();           // Performs the UV swap
            flippedY = flip;   // Update state tracking
        }
    }

    /**
     * Sets both the horizontal and vertical flip states of the texture in a single call.
     *
     * <p>This is a convenience method that forwards the specified flip states to
     * {@link #setFlipX(boolean)} and {@link #setFlipY(boolean)}. It allows the caller
     * to efficiently configure both flip directions without needing to invoke each
     * method individually.</p>
     *
     * <p>Internally, the method relies on the existing flip-tracking fields
     * ({@code flippedX} and {@code flippedY}) to ensure that UV coordinates
     * are only modified when the desired state differs from the current one.
     * As a result, this method performs zero unnecessary UV buffer updates.</p>
     *
     * @param flipX whether the texture should be flipped horizontally
     * @param flipY whether the texture should be flipped vertically
     */
    public void setFlip(boolean flipX, boolean flipY) {
        setFlipX(flipX);
        setFlipY(flipY);
    }

    /**
     * Sets the world-space position of the texture.
     *
     * @param x world X coordinate
     * @param y world Y coordinate
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateVertexBuffer();
    }

    /**
     * @return current world-space X position
     */
    public float getX() {
        return x;
    }

    /**
     * @return current world-space Y position
     */
    public float getY() {
        return y;
    }

    /**
     * @return current width of the rendered quad
     */
    public float getWidth() {
        return width;
    }

    /**
     * @return current height of the rendered quad
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the quad width, recalculates vertices, and updates world coordinates.
     */
    public void setWidth(float w) {
        this.width = w;
        updateLocalVertices();
        updateVertexBuffer();
    }

    /**
     * Sets the quad height, recalculates vertices, and updates world coordinates.
     */
    public void setHeight(float h) {
        this.height = h;
        updateLocalVertices();
        updateVertexBuffer();
    }

    /**
     * Sets both width and height simultaneously.
     */
    public void setSize(float w, float h) {
        this.width = w;
        this.height = h;
        updateLocalVertices();
        updateVertexBuffer();
    }

    /**
     * Defines a sub-rectangle of the texture to draw.
     * Useful for sprite sheets or animation frames.
     *
     * @param left   u-coordinate of the left side
     * @param top    v-coordinate of the top side
     * @param right  u-coordinate of the right side
     * @param bottom v-coordinate of the bottom side
     */
    public void setTextureRegion(float left, float top, float right, float bottom) {
        this.texLeft = left;
        this.texTop = top;
        this.texRight = right;
        this.texBottom = bottom;
        updateUVBuffer();
    }

    /**
     * Updates the UV buffer with the current region.
     * Order: top-left → top-right → bottom-right → bottom-left.
     */
    private void updateUVBuffer() {
        uvBuffer.put(0, texLeft);
        uvBuffer.put(1, texTop);
        uvBuffer.put(2, texRight);
        uvBuffer.put(3, texTop);
        uvBuffer.put(4, texRight);
        uvBuffer.put(5, texBottom);
        uvBuffer.put(6, texLeft);
        uvBuffer.put(7, texBottom);
    }

    /**
     * Sets the rotation around the origin point.
     *
     * <p>The rotation is stored in degrees but converted to radians for calculation.
     * Sine and cosine are cached to avoid repeated trigonometric calls.</p>
     *
     * @param degrees clockwise rotation angle
     */
    public void setRotation(float degrees) {
        this.rotation = degrees;
        float rad = (float) Math.toRadians(-degrees);
        sinRot = (float) Math.sin(rad);
        cosRot = (float) Math.cos(rad);
        updateVertexBuffer();
    }

    /**
     * @return current rotation angle in degrees
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the origin/pivot used for rotation and positioning.
     *
     * @param ox origin X relative to texture
     * @param oy origin Y relative to texture
     */
    public void setOrigin(float ox, float oy) {
        this.origin.set(ox, oy);
        updateLocalVertices();
        updateVertexBuffer();
    }

    /**
     * Sets origin to the exact center of the quad.
     * Common for rotating sprites or UI elements.
     */
    public void setOriginCenter() {
        origin.set(width * scaleX / 2f, height * scaleY / 2f);
        updateLocalVertices();
        updateVertexBuffer();
    }


    /**
     * @return current origin vector
     */
    public Vector2f getOrigin() {
        return origin;
    }

    /**
     * Computes the local-space vertex positions relative to the origin.
     *
     * <p>No rotation or world offset is applied here. Values represent
     * the raw quad positions prior to transformation.</p>
     */
    private void updateLocalVertices() {
        float ox = origin.x;
        float oy = origin.y;

        float scaledW = width * scaleX;
        float scaledH = height * scaleY;

        localVertices[0] = -ox;
        localVertices[1] = -oy;

        localVertices[2] = scaledW - ox;
        localVertices[3] = -oy;

        localVertices[4] = scaledW - ox;
        localVertices[5] = scaledH - oy;

        localVertices[6] = -ox;
        localVertices[7] = scaledH - oy;
    }

    /**
     * Rebuilds the final vertex buffer by applying rotation and world translation.
     */
    private void updateVertexBuffer() {
        float px = x + origin.x;
        float py = y + origin.y;

        for (int i = 0; i < 4; i++) {
            float lx = localVertices[i * 2];
            float ly = localVertices[i * 2 + 1];

            float rx = lx * cosRot - ly * sinRot;
            float ry = lx * sinRot + ly * cosRot;

            vertexBuffer.put(i * 2, rx + px);
            vertexBuffer.put(i * 2 + 1, ry + py);
        }
    }

    /**
     * Renders the textured quad.
     *
     * <p>This call performs the following:</p>
     * <ul>
     *     <li>Binds the texture</li>
     *     <li>Assigns vertex and UV buffer pointers</li>
     *     <li>Draws a quad via {@code glDrawArrays}</li>
     * </ul>
     *
     * <p>No matrix manipulation is done here; your projection/view matrix
     * must already be active.</p>
     */
    public void draw() {
        glBindTexture(GL_TEXTURE_2D, textureID);
        glVertexPointer(2, GL_FLOAT, 0, vertexBuffer);
        glTexCoordPointer(2, GL_FLOAT, 0, uvBuffer);
        glDrawArrays(GL_QUADS, 0, 4);
    }

}
