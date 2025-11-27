package jgl.viewport;

public class StretchViewport extends Viewport {
    public StretchViewport(float worldWidth, float worldHeight) {
        super(worldWidth, worldHeight);
    }

    @Override
    public void update(int screenWidth, int screenHeight) {
        this.screenX = 0;
        this.screenY = 0;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        projectionMatrix.identity().ortho(0, worldWidth, 0, worldHeight, -1f, 1f);
    }
}
