package jgl.math.geometry;

import jgl.Window;
import jgl.viewport.Viewport;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

/**
 * The ShapeRenderer class is responsible for managing and rendering a collection
 * of shapes using OpenGL instanced drawing. This renderer is optimized for handling
 * dynamic per-instance data, reusing buffers to minimize overhead during rendering.
 *
 * @author Albert Beaupre
 * @since November 11th, 2025
 */
public class ShapeRenderer {

    private static final float[] SCREEN_QUAD = {
            -1f, -1f,
            1f, -1f,
            1f,  1f,
            -1f, -1f,
            1f,  1f,
            -1f,  1f
    };

    // Per-instance layout:
    //  0: x
    //  1: y
    //  2: halfWidth
    //  3: halfHeight
    //  4: scaleX
    //  5: scaleY
    //  6: rotation
    //  7: originX
    //  8: originY
    //  9: transformX
    // 10: transformY
    // 11: type
    // 12-15: fill RGBA
    // 16: border thickness
    // 17-20: border RGBA
    // 21-24: additionalVariables (vec4), used per shape type
    private static final int FLOATS_PER_INSTANCE = 25;
    private static final int STRIDE = FLOATS_PER_INSTANCE * Float.BYTES;
    private static final int INITIAL_SHAPES = 16;

    private float[] buffer = new float[FLOATS_PER_INSTANCE * INITIAL_SHAPES];
    private Shape[] shapes = new Shape[INITIAL_SHAPES];

    private final int shader;
    private final int vao;
    private final int vbo;

    private int size = 0;
    private boolean dirty = false;

    /**
     * Creates a new {@code ShapeRenderer} and initializes all GL resources.
     */
    public ShapeRenderer() {
        shader = ShaderUtils.createProgram("shaders/shapes.vert", "shaders/shapes.frag");

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Quad
        int quadVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        glBufferData(GL_ARRAY_BUFFER, SCREEN_QUAD, org.lwjgl.opengl.GL15.GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Instance VBO
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) buffer.length * Float.BYTES, GL_DYNAMIC_DRAW);

        int offset = 0;
        addAttr(1, 2, offset);
        offset += 8;    // location (2 floats)
        addAttr(2, 2, offset);
        offset += 8;    // half-size (2 floats)
        addAttr(3, 2, offset);
        offset += 8;    // scale (2 floats)
        addAttr(4, 1, offset);
        offset += 4;    // rotation (1 float)
        addAttr(5, 2, offset);
        offset += 8;    // origin (2 floats)
        addAttr(6, 2, offset);
        offset += 8;    // transform (2 floats)
        addAttr(7, 1, offset);
        offset += 4;    // type (1 float)
        addAttr(8, 4, offset);
        offset += 16;   // fill RGBA (4 floats)
        addAttr(9, 1, offset);
        offset += 4;    // border thick (1 float)
        addAttr(10, 4, offset);
        offset += 16;   // border RGBA (4 floats)
        addAttr(11, 4, offset); // additional (4 floats)

        glBindVertexArray(0);
    }

    /**
     * Enables and describes a per-instance vertex attribute.
     *
     * @param loc    the attribute location
     * @param count  number of float components
     * @param offset byte offset in the per-instance structure
     */
    private void addAttr(int loc, int count, int offset) {
        glEnableVertexAttribArray(loc);
        glVertexAttribPointer(loc, count, GL_FLOAT, false, STRIDE, offset);
        glVertexAttribDivisor(loc, 1);
    }

    /**
     * Ensures that internal arrays and buffers can hold at least one more shape.
     * If capacity is exceeded, arrays, the staging FloatBuffer, and the GPU VBO
     * are resized.
     */
    private void ensureCapacity() {
        if (size < shapes.length)
            return;

        int newCount = shapes.length * 2;
        Shape[] ns = new Shape[newCount];
        float[] nb = new float[newCount * FLOATS_PER_INSTANCE];

        System.arraycopy(shapes, 0, ns, 0, size);
        System.arraycopy(buffer, 0, nb, 0, size * FLOATS_PER_INSTANCE);

        this.shapes = ns;
        this.buffer = nb;

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) buffer.length * Float.BYTES, GL_DYNAMIC_DRAW);
    }

    /**
     * Adds a shape to this renderer.
     *
     * @param shape the shape to add
     */
    public void add(Shape shape) {
        ensureCapacity();

        shapes[size] = shape;
        shape.setIndex(size);

        update(shape);

        size++;
        dirty = true;
    }

    /**
     * Removes a shape from this renderer. The last shape in the array is moved
     * into the removed slot to keep the instance data contiguous.
     *
     * @param s the shape to remove
     */
    public void remove(Shape s) {
        int index = s.getIndex();
        if (index < 0 || index >= size) {
            return;
        }

        int last = size - 1;

        if (index != last) {
            Shape moved = shapes[last];
            moved.setIndex(index);
            shapes[index] = moved;
        }

        shapes[last] = null;
        size--;
        s.setIndex(-1);
        dirty = true;
    }

    public void render() {
        render(null);
    }

    public void render(Viewport viewport) {
        glUseProgram(shader);
        int locUseProj = glGetUniformLocation(shader, "u_useProjection");

        if (viewport != null) {
            int locProj = glGetUniformLocation(shader, "u_projection");
            glUniform1i(locUseProj, 1);
            glUniformMatrix4fv(locProj, false, viewport.getCamera().getProjection().get());
        } else {
            int locRes = glGetUniformLocation(shader, "u_resolution");
            glUniform1i(locUseProj, 0);
            glUniform2f(locRes, Window.getWidth(), Window.getHeight());
        }

        if (dirty) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
            dirty = false;
        }

        glBindVertexArray(vao);
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, size);

        glUseProgram(0);
        glBindVertexArray(0);
    }

    /**
     * Updates the per-instance data for a single shape in the backing float array.
     *
     * @param shape the shape to update
     */
    public void update(Shape shape) {
        int pointer = shape.getIndex() * FLOATS_PER_INSTANCE;

        Color fill = shape.getColor();
        Border border = shape.getBorder();
        if (border == null)
            border = new Border(Color.TRANSPARENT, 0);

        Color stroke = border.getColor();

        // Base instance data
        buffer[pointer++] = shape.getX();
        buffer[pointer++] = shape.getY();
        buffer[pointer++] = shape.getWidth() * 0.5f;
        buffer[pointer++] = shape.getHeight() * 0.5f;
        buffer[pointer++] = shape.getScaleX();
        buffer[pointer++] = shape.getScaleY();
        buffer[pointer++] = shape.getRotation();
        buffer[pointer++] = 0; // originX (can be used later)
        buffer[pointer++] = 0; // originY (can be used later)
        buffer[pointer++] = shape.getTransformX();
        buffer[pointer++] = shape.getTransformY();
        buffer[pointer++] = shape.getType().ordinal();

        buffer[pointer++] = fill.getRed() / 255f;
        buffer[pointer++] = fill.getGreen() / 255f;
        buffer[pointer++] = fill.getBlue() / 255f;
        buffer[pointer++] = fill.getAlpha() / 255f;
        buffer[pointer++] = border.getThickness();

        buffer[pointer++] = stroke.getRed() / 255f;
        buffer[pointer++] = stroke.getGreen() / 255f;
        buffer[pointer++] = stroke.getBlue() / 255f;
        buffer[pointer++] = stroke.getAlpha() / 255f;

        /*
         * We're going to start adding the additional variables here:
         */
        switch (shape.getType()) {
            case Line -> {
                Line line = (Line) shape;

                buffer[pointer++] = line.getX2() - line.getX();
                buffer[pointer++] = line.getY2() - line.getY();
                buffer[pointer++] = line.getThickness();
                buffer[pointer]   = 0.0f; // unused padding to fill vec4
            }

            case Rectangle -> {
                Rectangle r = (Rectangle) shape;
                buffer[pointer++] = r.getBottomRightRadius();
                buffer[pointer++] = r.getTopRightRadius();
                buffer[pointer++] = r.getBottomLeftRadius();
                buffer[pointer]   = r.getTopLeftRadius();
            }

            case Circle -> {
                Circle c = (Circle) shape;
                buffer[pointer] = c.getRadius();
            }

            case Triangle -> {
                Triangle t = (Triangle) shape;

                float x1 = t.getX();
                float y1 = t.getY();
                float x2 = t.getX2();
                float y2 = t.getY2();
                float x3 = t.getX3();
                float y3 = t.getY3();

                // Centroid of the triangle
                float cx = (x1 + x2 + x3) / 3f;
                float cy = (y1 + y2 + y3) / 3f;

                int base = shape.getIndex() * FLOATS_PER_INSTANCE;
                buffer[base] = cx;
                buffer[base + 1] = cy;

                float vx2 = x2 - cx;
                float vy2 = y2 - cy;
                float vx3 = x3 - cx;
                float vy3 = y3 - cy;

                buffer[pointer++] = vx2;
                buffer[pointer++] = vy2;
                buffer[pointer++] = vx3;
                buffer[pointer]   = vy3;
            }
        }

        dirty = true;
    }

    /**
     * Disables all vertex attributes and unbinds the VAO.
     * Call this if you need to clean up GL state manually.
     */
    public void dispose() {
        glBindVertexArray(vao);
        for (int i = 0; i <= 11; i++)
            glDisableVertexAttribArray(i);

        glUseProgram(0);
        glBindVertexArray(0);
    }
}
