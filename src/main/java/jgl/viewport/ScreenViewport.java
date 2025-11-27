package jgl.viewport;

public class ScreenViewport extends Viewport {

    /**
     * Creates a screen viewport where the world dimensions match screen pixels.
     */
    public ScreenViewport(int width, int height) {
        super(width, height);
    }

    /**
     * Creates a screen viewport with a custom world size.
     */
    public ScreenViewport(float worldWidth, float worldHeight) {
        super(worldWidth, worldHeight);
    }

    /**
     * Updates the size of the viewport and adjusts the world size to match.
     * NOTE: This does NOT overwrite screenX/screenY — only updates screenWidth/Height.
     */
    @Override
    public void update(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // If you want world size to match screen size
        this.worldWidth = screenWidth;
        this.worldHeight = screenHeight;

        projectionMatrix.identity().ortho(0, worldWidth, 0, worldHeight, -1f, 1f);
    }

    /**
     * Updates only the projection matrix (world → clip space), without touching screen bounds.
     */
    public void updateWorld() {
        if (camera != null) {
            camera.rebuild(worldWidth, worldHeight);
            camera.getProjection().get(mat);
        } else {
            projectionMatrix.identity().ortho(0, worldWidth, 0, worldHeight, -1f, 1f);
            projectionMatrix.get(mat);
        }
    }
}
