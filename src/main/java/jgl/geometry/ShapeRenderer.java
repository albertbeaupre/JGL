package jgl.geometry;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.ARBBufferStorage.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.ARBBufferStorage.GL_MAP_PERSISTENT_BIT;
import static org.lwjgl.opengl.ARBBufferStorage.glBufferStorage;
import static org.lwjgl.opengl.GL33.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL33.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL33.GL_FALSE;
import static org.lwjgl.opengl.GL33.GL_FLOAT;
import static org.lwjgl.opengl.GL33.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL33.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL33.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL33.GL_TRIANGLES;
import static org.lwjgl.opengl.GL33.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL33.GL_VIEWPORT;
import static org.lwjgl.opengl.GL33.glAttachShader;
import static org.lwjgl.opengl.GL33.glBindBuffer;
import static org.lwjgl.opengl.GL33.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glCompileShader;
import static org.lwjgl.opengl.GL33.glCreateProgram;
import static org.lwjgl.opengl.GL33.glCreateShader;
import static org.lwjgl.opengl.GL33.glDeleteBuffers;
import static org.lwjgl.opengl.GL33.glDeleteProgram;
import static org.lwjgl.opengl.GL33.glDeleteShader;
import static org.lwjgl.opengl.GL33.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL33.glDrawArrays;
import static org.lwjgl.opengl.GL33.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL33.glGenBuffers;
import static org.lwjgl.opengl.GL33.glGenVertexArrays;
import static org.lwjgl.opengl.GL33.glGetIntegerv;
import static org.lwjgl.opengl.GL33.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL33.glGetProgrami;
import static org.lwjgl.opengl.GL33.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL33.glGetShaderi;
import static org.lwjgl.opengl.GL33.glGetUniformLocation;
import static org.lwjgl.opengl.GL33.glLinkProgram;
import static org.lwjgl.opengl.GL33.glMapBufferRange;
import static org.lwjgl.opengl.GL33.glShaderSource;
import static org.lwjgl.opengl.GL33.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL33.glUnmapBuffer;
import static org.lwjgl.opengl.GL33.glUseProgram;
import static org.lwjgl.opengl.GL33.glVertexAttribPointer;

public class ShapeRenderer {

    private static final int FLOATS_PER_VERTEX = 6;   // x, y, r, g, b, a
    private static final int BYTES_PER_FLOAT = 4;
    private static final int NUM_FRAMES = 2;
    private static final int DEFAULT_MAX_VERTICES = 100_000;

    private int maxVertices = DEFAULT_MAX_VERTICES;
    private int vertexCount;
    private int frameIndex;

    private int vao, vbo, shaderProgram, uProjectionLoc;
    private ByteBuffer mappedBuffer;
    private FloatBuffer mappedFloats;
    private long mappedSize, perFrameSize;

    // Current color state
    private float currR = 1f, currG = 1f, currB = 1f, currA = 1f;

    // Anti-alias config
    private boolean aaEnabled = true;
    private float feather = 1.25f; // pixels

    // Reusable viewport + projection
    private final int[] vp = new int[4];
    private final float[] ortho = new float[16]; // reused every frame

    // Reusable scratch buffers (grown as needed, never shrunk)
    private float[] pathBuf = new float[8];   // temporary path (x,y)*n
    private float[] normalsBuf = new float[8];   // temporary normals (nx,ny)*n
    private float[] lastPathBuf = new float[8];   // persisted outline for addBorder()
    private int lastPathCount = 0;

    public ShapeRenderer() {
        initGL();
    }

    // ---------- Color & AA control ----------
    public void setColor(float r, float g, float b, float a) {
        currR = r;
        currG = g;
        currB = b;
        currA = a;
    }

    public void setColor(float r, float g, float b) {
        setColor(r, g, b, 1f);
    }

    public void setAntialias(boolean enabled) {
        this.aaEnabled = enabled;
    }

    /**
     * Feather width in pixels for AA rings (default ~1.25).
     */
    public void setFeather(float pixels) {
        this.feather = Math.max(0f, pixels);
    }

    // ---------- GL setup ----------
    private void initGL() {
        int vert = createShader(GL_VERTEX_SHADER, """
                #version 330 core
                layout(location = 0) in vec2 aPos;
                layout(location = 1) in vec4 aColor;
                uniform mat4 uProjection;
                out vec4 vColor;
                void main() {
                    gl_Position = uProjection * vec4(aPos, 0.0, 1.0);
                    vColor = aColor;
                }
                """);

        int frag = createShader(GL_FRAGMENT_SHADER, """
                #version 330 core
                in vec4 vColor;
                out vec4 FragColor;
                void main() { FragColor = vColor; }
                """);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vert);
        glAttachShader(shaderProgram, frag);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException(glGetProgramInfoLog(shaderProgram));
        glDeleteShader(vert);
        glDeleteShader(frag);

        uProjectionLoc = glGetUniformLocation(shaderProgram, "uProjection");

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        mappedSize = (long) maxVertices * FLOATS_PER_VERTEX * BYTES_PER_FLOAT * NUM_FRAMES;
        perFrameSize = mappedSize / NUM_FRAMES;

        glBufferStorage(GL_ARRAY_BUFFER, mappedSize, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);

        mappedBuffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, mappedSize, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT, null);
        mappedFloats = mappedBuffer.asFloatBuffer();

        glVertexAttribPointer(0, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * BYTES_PER_FLOAT, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, FLOATS_PER_VERTEX * BYTES_PER_FLOAT, 2L * BYTES_PER_FLOAT);
        glEnableVertexAttribArray(1);
    }

    private static int createShader(int type, String src) {
        int s = glCreateShader(type);
        glShaderSource(s, src);
        glCompileShader(s);
        if (glGetShaderi(s, GL_COMPILE_STATUS) == GL_FALSE) throw new RuntimeException(glGetShaderInfoLog(s));
        return s;
    }

    // ---------- Frame control ----------
    public void begin() {
        frameIndex = (frameIndex + 1) % NUM_FRAMES;
        vertexCount = 0;

        int floatOffset = (int) (frameIndex * (perFrameSize / BYTES_PER_FLOAT));
        mappedFloats.position(floatOffset);

        glUseProgram(shaderProgram);
        glBindVertexArray(vao);
        updateProjectionFromViewport();

        // new frame: nothing selected yet for addBorder
        lastPathCount = 0;
    }

    public void end() {
        flush();
    }

    private void flush() {
        if (vertexCount == 0) return;
        int vertexOffset = (int) ((frameIndex * perFrameSize) / (FLOATS_PER_VERTEX * BYTES_PER_FLOAT));
        glDrawArrays(GL_TRIANGLES, vertexOffset, vertexCount);
        vertexCount = 0;
    }

    public void dispose() {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glUnmapBuffer(GL_ARRAY_BUFFER);      // unmap releases the mapping
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glDeleteProgram(shaderProgram);
        // DO NOT memFree(mappedBuffer) here; it's a mapped view, not memAlloc'd.
        mappedBuffer = null;
        mappedFloats = null;
    }

    // ---------- Projection ----------
    private void updateProjectionFromViewport() {
        glGetIntegerv(GL_VIEWPORT, vp);
        float w = Math.max(1, vp[2]);
        float h = Math.max(1, vp[3]);

        // Reuse 'ortho' array; write elements explicitly (bottom-left origin)
        float sx = 2f / w, sy = 2f / h;
        // [  sx,  0,  0, 0 ]
        // [   0, sy,  0, 0 ]
        // [   0,  0, -1, 0 ]
        // [  -1, -1,  0, 1 ]
        ortho[0] = sx;
        ortho[1] = 0;
        ortho[2] = 0;
        ortho[3] = 0;
        ortho[4] = 0;
        ortho[5] = sy;
        ortho[6] = 0;
        ortho[7] = 0;
        ortho[8] = 0;
        ortho[9] = 0;
        ortho[10] = -1;
        ortho[11] = 0;
        ortho[12] = -1;
        ortho[13] = -1;
        ortho[14] = 0;
        ortho[15] = 1;

        glUniformMatrix4fv(uProjectionLoc, false, ortho);
    }

    private void ensureCapacity(int extraVertices) {
        if (vertexCount + extraVertices > maxVertices) flush();
    }

    private void emit(float x, float y) {
        mappedFloats.put(x).put(y).put(currR).put(currG).put(currB).put(currA);
        vertexCount++;
    }

    private void emitColor(float x, float y, float r, float g, float b, float a) {
        mappedFloats.put(x).put(y).put(r).put(g).put(b).put(a);
        vertexCount++;
    }

    private void emitTri(float x1, float y1, float x2, float y2, float x3, float y3) {
        ensureCapacity(3);
        emit(x1, y1);
        emit(x2, y2);
        emit(x3, y3);
    }

    private void emitTriColor(float x1, float y1, float x2, float y2, float x3, float y3, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2, float r3, float g3, float b3, float a3) {
        ensureCapacity(3);
        emitColor(x1, y1, r1, g1, b1, a1);
        emitColor(x2, y2, r2, g2, b2, a2);
        emitColor(x3, y3, r3, g3, b3, a3);
    }

    private void emitQuad(float x, float y, float w, float h) {
        ensureCapacity(6);
        emit(x, y);
        emit(x + w, y);
        emit(x + w, y + h);
        emit(x, y);
        emit(x + w, y + h);
        emit(x, y + h);
    }

    // ---------- Scratch buffer management ----------
    private void ensureScratchCapacity(int points) {
        int floats = points * 2;
        if (pathBuf.length < floats) pathBuf = grow(pathBuf, floats);
        if (normalsBuf.length < floats) normalsBuf = grow(normalsBuf, floats);
    }

    private void ensureLastPathCapacity(int points) {
        int floats = points * 2;
        if (lastPathBuf.length < floats) lastPathBuf = grow(lastPathBuf, floats);
    }

    private static float[] grow(float[] a, int min) {
        int n = a.length;
        while (n < min) n = n < 1 ? 1 : n << 1;
        float[] b = new float[n];
        System.arraycopy(a, 0, b, 0, a.length);
        return b;
    }

    private void storeLastPath(float[] src, int points) {
        if (points <= 0) {
            lastPathCount = 0;
            return;
        }
        ensureLastPathCapacity(points);
        System.arraycopy(src, 0, lastPathBuf, 0, points * 2);
        lastPathCount = points;
    }

    /**
     * Computes per-vertex averaged (miter) normals for a closed path.
     * Writes into {@code normalsBuf} without allocating.
     */
    private void averagedNormals(float[] xy, int points) {
        // Compute edge normals and accumulate at vertices
        // normalsBuf used as accumulator (sum of edge normals)
        for (int i = 0; i < points * 2; i++) normalsBuf[i] = 0f;

        for (int i = 0; i < points; i++) {
            int j = (i + 1) % points;
            float x1 = xy[2 * i], y1 = xy[2 * i + 1];
            float x2 = xy[2 * j], y2 = xy[2 * j + 1];
            float dx = x2 - x1, dy = y2 - y1;
            float len = (float) sqrt(dx * dx + dy * dy);
            if (len == 0f) len = 1f;
            // left-hand normal for CCW path; OK for our AA rings (path is built clockwise/consistent)
            float nx = -dy / len, ny = dx / len;

            // accumulate at both ends
            normalsBuf[2 * i] += nx;
            normalsBuf[2 * i + 1] += ny;
            normalsBuf[2 * j] += nx;
            normalsBuf[2 * j + 1] += ny;
        }

        // Normalize
        for (int i = 0; i < points; i++) {
            float mx = normalsBuf[2 * i], my = normalsBuf[2 * i + 1];
            float ml = (float) sqrt(mx * mx + my * my);
            if (ml == 0f) {
                normalsBuf[2 * i] = 0f;
                normalsBuf[2 * i + 1] = 1f;
            } else {
                normalsBuf[2 * i] = mx / ml;
                normalsBuf[2 * i + 1] = my / ml;
            }
        }
    }

    // =====================================================================
    //                              FILL API (+ AA)
    // =====================================================================

    public void fillRect(float x, float y, float w, float h) {
        emitQuad(x, y, w, h);

        // Outline path (4 points) -> reuse buffers
        ensureScratchCapacity(4);
        pathBuf[0] = x;
        pathBuf[1] = y;
        pathBuf[2] = x + w;
        pathBuf[3] = y;
        pathBuf[4] = x + w;
        pathBuf[5] = y + h;
        pathBuf[6] = x;
        pathBuf[7] = y + h;

        storeLastPath(pathBuf, 4);
        if (aaEnabled && feather > 0f) featherRing(pathBuf, 4, feather);
    }

    public void fillCircle(float cx, float cy, float radius, int segments) {
        int n = Math.max(3, segments);
        ensureScratchCapacity(n);

        ensureCapacity(3 * n);
        float px = cx + radius, py = cy;
        for (int i = 1; i <= n; i++) {
            double t = 2.0 * Math.PI * i / n;
            float x = (float) (cx + radius * cos(t));
            float y = (float) (cy + radius * sin(t));
            emit(cx, cy);
            emit(px, py);
            emit(x, y);
            px = x;
            py = y;
        }

        // Build path directly into pathBuf
        for (int i = 0; i < n; i++) {
            double t = 2.0 * Math.PI * i / n;
            pathBuf[2 * i] = (float) (cx + radius * cos(t));
            pathBuf[2 * i + 1] = (float) (cy + radius * sin(t));
        }
        storeLastPath(pathBuf, n);
        if (aaEnabled && feather > 0f) featherRing(pathBuf, n, feather);
    }

    public void fillPolygonConvex(float[] xy) {
        int n = xy.length / 2;
        if (n < 3) return;
        ensureCapacity(3 * (n - 2));
        float x0 = xy[0], y0 = xy[1];
        for (int i = 2; i < n; i++) {
            float x1 = xy[2 * (i - 1)], y1 = xy[2 * (i - 1) + 1];
            float x2 = xy[2 * i], y2 = xy[2 * i + 1];
            emitTri(x0, y0, x1, y1, x2, y2);
        }
        // copy into lastPathBuf once (no new alloc each call)
        storeLastPath(xy, n);
        if (aaEnabled && feather > 0f) featherRing(xy, n, feather);
    }

    public void fillRoundedRect(float x, float y, float w, float h, float radius, int segments) {
        if (radius <= 0f) {
            fillRect(x, y, w, h);
            return;
        }
        float rad = Math.min(radius, Math.min(w, h) * 0.5f);
        float cx1 = x + rad, cy1 = y + rad;
        float cx2 = x + w - rad, cy2 = y + rad;
        float cx3 = x + w - rad, cy3 = y + h - rad;
        float cx4 = x + rad, cy4 = y + h - rad;

        // center + edges
        fillRect(cx1, cy1, w - 2 * rad, h - 2 * rad);
        emitQuad(cx1, y, w - 2 * rad, rad);
        emitQuad(cx1, y + h - rad, w - 2 * rad, rad);
        emitQuad(x, cy1, rad, h - 2 * rad);
        emitQuad(x + w - rad, cy1, rad, h - 2 * rad);

        // corners (solid)
        fillQuarterCircleAA(cx1, cy1, rad, 180f, 270f, segments, false);
        fillQuarterCircleAA(cx2, cy2, rad, 270f, 360f, segments, false);
        fillQuarterCircleAA(cx3, cy3, rad, 0f, 90f, segments, false);
        fillQuarterCircleAA(cx4, cy4, rad, 90f, 180f, segments, false);

        // Build outline path into reusable buffer
        int seg = Math.max(3, segments);
        int count = 4 * seg;
        ensureScratchCapacity(count);
        int k = 0;

        for (int i = 0; i < seg; i++) { // bottom-left 180..270
            double t = Math.toRadians(180 + (90.0 * i / seg));
            pathBuf[2 * k] = (float) (cx1 + rad * cos(t));
            pathBuf[2 * k + 1] = (float) (cy1 + rad * sin(t));
            k++;
        }
        for (int i = 0; i < seg; i++) { // bottom-right 270..360
            double t = Math.toRadians(270 + (90.0 * i / seg));
            pathBuf[2 * k] = (float) (cx2 + rad * cos(t));
            pathBuf[2 * k + 1] = (float) (cy2 + rad * sin(t));
            k++;
        }
        for (int i = 0; i < seg; i++) { // top-right 0..90
            double t = Math.toRadians(0 + (90.0 * i / seg));
            pathBuf[2 * k] = (float) (cx3 + rad * cos(t));
            pathBuf[2 * k + 1] = (float) (cy3 + rad * sin(t));
            k++;
        }
        for (int i = 0; i < seg; i++) { // top-left 90..180
            double t = Math.toRadians(90 + (90.0 * i / seg));
            pathBuf[2 * k] = (float) (cx4 + rad * cos(t));
            pathBuf[2 * k + 1] = (float) (cy4 + rad * sin(t));
            k++;
        }

        storeLastPath(pathBuf, count);
        if (aaEnabled && feather > 0f) featherRing(pathBuf, count, feather);
    }

    private void fillQuarterCircleAA(float cx, float cy, float radius, float startDeg, float endDeg, int segments, boolean alsoAA) {
        double start = Math.toRadians(startDeg);
        double end = Math.toRadians(endDeg);
        int seg = Math.max(1, segments);
        double step = (end - start) / seg;
        float px = (float) (cx + radius * cos(start));
        float py = (float) (cy + radius * sin(start));
        for (int i = 1; i <= seg; i++) {
            double t = start + i * step;
            float x = (float) (cx + radius * cos(t));
            float y = (float) (cy + radius * sin(t));
            emit(cx, cy);
            emit(px, py);
            emit(x, y);
            px = x;
            py = y;
        }
    }

    private void featherRing(float[] path, int n, float featherWidth) {
        if (n < 3) return;

        averagedNormals(path, n);
        float inA = currA, outA = 0f;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;

            float x1 = path[2 * i], y1 = path[2 * i + 1];
            float x2 = path[2 * j], y2 = path[2 * j + 1];
            float nx1 = normalsBuf[2 * i], ny1 = normalsBuf[2 * i + 1];
            float nx2 = normalsBuf[2 * j], ny2 = normalsBuf[2 * j + 1];

            float x1o = x1 + nx1 * featherWidth, y1o = y1 + ny1 * featherWidth;
            float x2o = x2 + nx2 * featherWidth, y2o = y2 + ny2 * featherWidth;

            emitTriColor(x1, y1, x2, y2, x2o, y2o, currR, currG, currB, inA, currR, currG, currB, inA, currR, currG, currB, outA);
            emitTriColor(x1, y1, x2o, y2o, x1o, y1o, currR, currG, currB, inA, currR, currG, currB, outA, currR, currG, currB, outA);
        }
    }

    // =====================================================================
    //                              STROKE API
    // =====================================================================

    public void drawLine(float x1, float y1, float x2, float y2, float thickness) {
        float dx = x2 - x1, dy = y2 - y1;
        float len = (float) sqrt(dx * dx + dy * dy);
        if (len == 0) return;
        float nx = -dy / len, ny = dx / len;
        float hx = nx * (thickness * 0.5f);
        float hy = ny * (thickness * 0.5f);

        float xA = x1 - hx, yA = y1 - hy;
        float xB = x1 + hx, yB = y1 + hy;
        float xC = x2 + hx, yC = y2 + hy;
        float xD = x2 - hx, yD = y2 - hy;

        emitTri(xA, yA, xB, yB, xC, yC);
        emitTri(xA, yA, xC, yC, xD, yD);
    }

    public void drawRect(float x, float y, float w, float h, float thickness) {
        emitQuad(x, y - thickness * 0.5f, w, thickness);
        emitQuad(x, y + h - thickness * 0.5f, w, thickness);
        emitQuad(x - thickness * 0.5f, y, thickness, h);
        emitQuad(x + w - thickness * 0.5f, y, thickness, h);
    }

    public void drawCircle(float cx, float cy, float radius, float thickness, int segments) {
        int n = Math.max(3, segments);
        if (thickness <= 0) return;

        float inner = Math.max(0f, radius - thickness * 0.5f);
        float outer = radius + thickness * 0.5f;

        double step = 2.0 * Math.PI / n;
        double ang = 0.0;

        float ix0 = (float) (cx + inner * cos(ang));
        float iy0 = (float) (cy + inner * sin(ang));
        float ox0 = (float) (cx + outer * cos(ang));
        float oy0 = (float) (cy + outer * sin(ang));

        for (int i = 1; i <= n; i++) {
            ang += step;
            float ix1 = (float) (cx + inner * cos(ang));
            float iy1 = (float) (cy + inner * sin(ang));
            float ox1 = (float) (cx + outer * cos(ang));
            float oy1 = (float) (cy + outer * sin(ang));
            emitTri(ix0, iy0, ox0, oy0, ox1, oy1);
            emitTri(ix0, iy0, ox1, oy1, ix1, iy1);
            ix0 = ix1;
            iy0 = iy1;
            ox0 = ox1;
            oy0 = oy1;
        }
    }

    public void drawRoundedRect(float x, float y, float w, float h, float radius, float thickness, int segments) {
        if (thickness <= 0) return;
        if (radius <= 0) {
            drawRect(x, y, w, h, thickness);
            return;
        }

        float innerR = Math.max(0f, radius - thickness * 0.5f);
        float outerR = radius + thickness * 0.5f;

        float[][] c = {{x + radius, y + radius, 180f, 270f}, {x + w - radius, y + radius, 270f, 360f}, {x + w - radius, y + h - radius, 0f, 90f}, {x + radius, y + h - radius, 90f, 180f}};

        for (float[] corner : c) {
            float cx = corner[0], cy = corner[1];
            double start = Math.toRadians(corner[2]);
            double end = Math.toRadians(corner[3]);
            double step = (end - start) / Math.max(1, segments);

            float ix0 = (float) (cx + innerR * cos(start));
            float iy0 = (float) (cy + innerR * sin(start));
            float ox0 = (float) (cx + outerR * cos(start));
            float oy0 = (float) (cy + outerR * sin(start));

            for (int i = 1; i <= segments; i++) {
                double t = start + i * step;
                float ix1 = (float) (cx + innerR * cos(t));
                float iy1 = (float) (cy + innerR * sin(t));
                float ox1 = (float) (cx + outerR * cos(t));
                float oy1 = (float) (cy + outerR * sin(t));

                emitTri(ix0, iy0, ox0, oy0, ox1, oy1);
                emitTri(ix0, iy0, ox1, oy1, ix1, iy1);

                ix0 = ix1;
                iy0 = iy1;
                ox0 = ox1;
                oy0 = oy1;
            }
        }

        emitQuad(x + radius, y - thickness * 0.5f, w - 2 * radius, thickness);
        emitQuad(x + radius, y + h - thickness * 0.5f, w - 2 * radius, thickness);
        emitQuad(x - thickness * 0.5f, y + radius, thickness, h - 2 * radius);
        emitQuad(x + w - thickness * 0.5f, y + radius, thickness, h - 2 * radius);
    }

    public void drawPolygon(float[] xy, float thickness) {
        int n = xy.length / 2;
        if (n < 2 || thickness <= 0) return;
        float half = thickness * 0.5f;

        ensureScratchCapacity(n);
        averagedNormals(xy, n);

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float x1 = xy[2 * i], y1 = xy[2 * i + 1];
            float x2 = xy[2 * j], y2 = xy[2 * j + 1];
            float nx1 = normalsBuf[2 * i], ny1 = normalsBuf[2 * i + 1];
            float nx2 = normalsBuf[2 * j], ny2 = normalsBuf[2 * j + 1];

            float x1o = x1 + nx1 * half, y1o = y1 + ny1 * half;
            float x2o = x2 + nx2 * half, y2o = y2 + ny2 * half;
            float x1i = x1 - nx1 * half, y1i = y1 - ny1 * half;
            float x2i = x2 - nx2 * half, y2i = y2 - ny2 * half;
            emitTri(x1o, y1o, x2o, y2o, x2i, y2i);
            emitTri(x1o, y1o, x2i, y2i, x1i, y1i);
        }
    }

    // =====================================================================
    //                        addBorder(thickness)
    // =====================================================================

    /**
     * Adds a border around the *last filled* shape using current color.
     */
    public void addBorder(float thickness) {
        if (lastPathCount < 3 || thickness <= 0f) return;

        int n = lastPathCount;
        float[] path = lastPathBuf;

        ensureScratchCapacity(n);
        averagedNormals(path, n);
        float half = thickness * 0.5f;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;

            float x1 = path[2 * i], y1 = path[2 * i + 1];
            float x2 = path[2 * j], y2 = path[2 * j + 1];
            float nx1 = normalsBuf[2 * i], ny1 = normalsBuf[2 * i + 1];
            float nx2 = normalsBuf[2 * j], ny2 = normalsBuf[2 * j + 1];

            float x1o = x1 + nx1 * half, y1o = y1 + ny1 * half;
            float x2o = x2 + nx2 * half, y2o = y2 + ny2 * half;
            float x1i = x1 - nx1 * half, y1i = y1 - ny1 * half;
            float x2i = x2 - nx2 * half, y2i = y2 - ny2 * half;

            emitTri(x1o, y1o, x2o, y2o, x2i, y2i);
            emitTri(x1o, y1o, x2i, y2i, x1i, y1i);
        }

        if (aaEnabled && feather > 0f) {
            float outW = half + feather;
            float inA = currA, outA = 0f;

            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;

                float x1 = path[2 * i], y1 = path[2 * i + 1];
                float x2 = path[2 * j], y2 = path[2 * j + 1];
                float nx1 = normalsBuf[2 * i], ny1 = normalsBuf[2 * i + 1];
                float nx2 = normalsBuf[2 * j], ny2 = normalsBuf[2 * j + 1];

                float x1o = x1 + nx1 * half, y1o = y1 + ny1 * half;
                float x2o = x2 + nx2 * half, y2o = y2 + ny2 * half;

                float x1f = x1 + nx1 * outW, y1f = y1 + ny1 * outW;
                float x2f = x2 + nx2 * outW, y2f = y2 + ny2 * outW;

                emitTriColor(x1o, y1o, x2o, y2o, x2f, y2f, currR, currG, currB, inA, currR, currG, currB, inA, currR, currG, currB, outA);
                emitTriColor(x1o, y1o, x2f, y2f, x1f, y1f, currR, currG, currB, inA, currR, currG, currB, outA, currR, currG, currB, outA);
            }
        }
    }
}
