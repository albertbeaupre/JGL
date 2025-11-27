package jgl.viewport;

public class FitViewport extends Viewport {
    public FitViewport(float worldWidth, float worldHeight) {
        super(worldWidth, worldHeight);
    }

    @Override
    public void update(int screenWidth, int screenHeight) {
        float worldAspect = worldWidth / worldHeight;
        float screenAspect = (float) screenWidth / (float) screenHeight;

        if (screenAspect > worldAspect) {
            this.screenHeight = screenHeight;
            this.screenWidth = (int) (screenHeight * worldAspect);
            this.screenX = (screenWidth - this.screenWidth) / 2;
            this.screenY = 0;
        } else {
            this.screenWidth = screenWidth;
            this.screenHeight = (int) (screenWidth / worldAspect);
            this.screenX = 0;
            this.screenY = (screenHeight - this.screenHeight) / 2;
        }

        projectionMatrix.identity().ortho(0, worldWidth, 0, worldHeight, -1f, 1f);
    }
}
