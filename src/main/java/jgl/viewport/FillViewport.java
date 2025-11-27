package jgl.viewport;

public class FillViewport extends Viewport {
    public FillViewport(float worldWidth, float worldHeight) {
        super(worldWidth, worldHeight);
    }

    @Override
    public void update(int screenWidth, int screenHeight) {
        float worldAspect = worldWidth / worldHeight;
        float screenAspect = (float) screenWidth / (float) screenHeight;

        if (screenAspect > worldAspect) {
            this.screenWidth = screenWidth;
            this.screenHeight = (int) (screenWidth / worldAspect);
            this.screenX = 0;
            this.screenY = (screenHeight - this.screenHeight) / 2;
        } else {
            this.screenHeight = screenHeight;
            this.screenWidth = (int) (screenHeight * worldAspect);
            this.screenY = 0;
            this.screenX = (screenWidth - this.screenWidth) / 2;
        }

        projectionMatrix.identity().ortho(0, worldWidth, 0, worldHeight, -1f, 1f);
    }
}
