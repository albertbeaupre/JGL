package jgl;

import static org.lwjgl.opengl.GL33.*;

/**
 * Demonstrates viewport clipping with a yellow minimap and a circle
 * drawn in its bottom-right corner that tries to bleed outside,
 * but is cleanly clipped by glScissor().
 *
 * @author Albert Beaupre
 * @since 2025-10-30
 */
public class Launcher implements Application {

    private int vaoRect;
    private int vboRect;
    private int vaoCircle;
    private int vboCircle;

    public static void main(String[] args) {
        JGL.init(new Launcher(), "Viewport Clipping Circle Demo", 800, 600);
    }

    @Override
    public void init() {
        glClearColor(0f, 0f, 0f, 1f); // background color

        // === Rectangle geometry (for minimap fill test) ===
        float[] rectVertices = {
                -1.2f, -1.2f,
                1.2f, -1.2f,
                1.2f,  1.2f,
                -1.2f,  1.2f
        };

        vaoRect = glGenVertexArrays();
        glBindVertexArray(vaoRect);

        vboRect = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboRect);
        glBufferData(GL_ARRAY_BUFFER, rectVertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // === Circle geometry ===
        int segments = 64;
        float radius = 1.2f; // intentionally large to bleed outside
        float[] circleVertices = new float[(segments + 2) * 2];
        circleVertices[0] = 0f;
        circleVertices[1] = 0f;
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            circleVertices[(i + 1) * 2] = (float) Math.cos(angle) * radius;
            circleVertices[(i + 1) * 2 + 1] = (float) Math.sin(angle) * radius;
        }

        vaoCircle = glGenVertexArrays();
        glBindVertexArray(vaoCircle);

        vboCircle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboCircle);
        glBufferData(GL_ARRAY_BUFFER, circleVertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        int windowWidth = 800;
        int windowHeight = 600;

        // === FULL WINDOW BACKGROUND ===
        glViewport(0, 0, windowWidth, windowHeight);
        glDisable(GL_SCISSOR_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        // === MINIMAP REGION ===
        int mapWidth = 200;
        int mapHeight = 200;
        int mapX = windowWidth - mapWidth - 20;
        int mapY = 20;

        glViewport(mapX, mapY, mapWidth, mapHeight);
        glEnable(GL_SCISSOR_TEST);
        glScissor(mapX, mapY, mapWidth, mapHeight);

        // Fill minimap yellow
        glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        // Draw a blue circle that tries to bleed out (bottom-right)
        glBindVertexArray(vaoCircle);
        glPushMatrix();
        glTranslatef(0.8f, -0.8f, 0f); // move to bottom-right corner (in minimap coords)
        glColor3f(0.0f, 0.3f, 1.0f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 66); // draw the circle
        glPopMatrix();

        glDisable(GL_SCISSOR_TEST);

        // Restore full window viewport
        glViewport(0, 0, windowWidth, windowHeight);
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void dispose() {
        glDeleteBuffers(vboRect);
        glDeleteVertexArrays(vaoRect);
        glDeleteBuffers(vboCircle);
        glDeleteVertexArrays(vaoCircle);
    }
}
