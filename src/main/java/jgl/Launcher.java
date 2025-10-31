package jgl;

import jgl.geometry.ShapeRenderer;
import static org.lwjgl.opengl.GL33.GL_BLEND;
import static org.lwjgl.opengl.GL33.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL33.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL33.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL33.glBlendFunc;
import static org.lwjgl.opengl.GL33.glClear;
import static org.lwjgl.opengl.GL33.glClearColor;
import static org.lwjgl.opengl.GL33.glEnable;

/**
 * Comprehensive ShapeRenderer test.
 *
 * Tests:
 *  - fillRect, drawRect
 *  - fillCircle, drawCircle
 *  - fillPolygonConvex, drawPolygon
 *  - fillRoundedRect, drawRoundedRect
 *  - addBorder(thickness)
 *  - AA toggling
 */
public class Launcher implements Application {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private ShapeRenderer renderer;
    private boolean aaEnabled = true;

    @Override
    public void init() {
        glClearColor(0.1f, 0.1f, 0.1f, 1f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        renderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);

        renderer.begin();

        // === Fill tests ===
        renderer.setAntialias(true);
        renderer.setFeather(1.5f);

        float x = 80, y = 500, gapX = 180, gapY = 160;

        // 1. Filled rectangle + border
        renderer.setColor(1f, 0.2f, 0.2f);
        renderer.fillRect(x, y, 120, 80);
        renderer.setColor(1f, 1f, 1f);
        renderer.addBorder(3f);

        // 2. Filled circle + border
        renderer.setColor(0.2f, 0.5f, 1f);
        renderer.fillCircle(x + gapX, y + 40, 40, 48);
        renderer.setColor(1f, 1f, 1f);
        renderer.addBorder(3f);

        // 3. Filled triangle + border
        renderer.setColor(0.3f, 1f, 0.4f);
        float[] tri = {x + 2 * gapX + 60, y, x + 2 * gapX + 120, y, x + 2 * gapX + 90, y + 90};
        renderer.fillPolygonConvex(tri);
        renderer.setColor(1f, 1f, 1f);
        renderer.addBorder(2.5f);

        // 4. Filled rounded rectangle + border
        renderer.setColor(1f, 0.6f, 0.15f);
        renderer.fillRoundedRect(x + 3 * gapX, y, 160, 90, 20, 20);
        renderer.setColor(1f, 1f, 1f);
        renderer.addBorder(3.5f);

        // === Outline tests ===
        float oy = y - gapY;

        renderer.setColor(1f, 0.4f, 0.4f);
        renderer.drawRect(x, oy, 120, 80, 3f);

        renderer.setColor(0.3f, 0.7f, 1f);
        renderer.drawCircle(x + gapX, oy + 40, 40, 3f, 48);

        renderer.setColor(1f, 1f, 0.4f);
        float[] tri2 = {x + 2 * gapX + 60, oy, x + 2 * gapX + 120, oy, x + 2 * gapX + 90, oy + 90};
        renderer.drawPolygon(tri2, 2.5f);

        renderer.setColor(1f, 0.8f, 0.3f);
        renderer.drawRoundedRect(x + 3 * gapX, oy, 160, 90, 20, 3f, 20);

        // === Line and polygon tests ===
        float lx = 100, ly = 250;
        renderer.setColor(1f, 1f, 0.2f);
        renderer.drawLine(lx, ly, lx + 140, ly + 60, 3f);
        renderer.setColor(0.3f, 1f, 1f);
        renderer.drawLine(lx, ly + 60, lx + 140, ly, 3f);

        // Hexagon fill + border test
        float[] hex = new float[12];
        float hx = 400, hy = 230, r = 50;
        for (int i = 0; i < 6; i++) {
            double t = Math.toRadians(i * 60);
            hex[2 * i] = (float) (hx + r * Math.cos(t));
            hex[2 * i + 1] = (float) (hy + r * Math.sin(t));
        }
        renderer.setColor(0.4f, 0.6f, 1f);
        renderer.fillPolygonConvex(hex);
        renderer.setColor(1f, 1f, 1f);
        renderer.addBorder(3f);

        // Rounded rectangle long test
        renderer.setColor(1f, 0.8f, 0.2f);
        renderer.fillRoundedRect(600, 230, 220, 90, 40, 24);
        renderer.setColor(0f, 0f, 0f);
        renderer.addBorder(2f);

        // === AA toggle display ===
        renderer.setColor(aaEnabled ? 0.2f : 0.6f, aaEnabled ? 1f : 0.2f, 0.3f);
        renderer.fillRect(50, 80, 100, 40);
        renderer.setColor(1f, 1f, 1f);
        renderer.addBorder(20f);

        renderer.end();

        Window.setTitle(String.format("ShapeRenderer Visual Test | FPS: %.0f | AA: %s",
                JGL.getFramesPerSecond(), aaEnabled ? "ON" : "OFF"));
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    public static void main(String[] args) {
        JGL.init(new Launcher(), "ShapeRenderer Visual Test", WIDTH, HEIGHT);
    }
}
