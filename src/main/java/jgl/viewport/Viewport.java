package jgl.viewport;

import jgl.camera.Camera2D;
import jgl.math.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Abstract viewport class that handles viewport management in OpenGL.
 * Provides functionality for managing screen coordinates, world dimensions,
 * projection matrices and camera integration.
 */
public abstract class Viewport {

    /**
     * X coordinate of viewport on screen
     */
    public int screenX;

    /**
     * Y coordinate of viewport on screen
     */
    public int screenY;

    /**
     * Width of viewport in screen pixels
     */
    public int screenWidth;

    /**
     * Height of viewport in screen pixels
     */
    public int screenHeight;

    /**
     * Width of logical world units
     */
    protected float worldWidth;

    /**
     * Height of logical world units
     */
    protected float worldHeight;

    /**
     * Temporary array for matrix operations
     */
    float[] mat = new float[16];

    /**
     * The projection matrix used when no camera is set
     */
    protected final Matrix4f projectionMatrix = new Matrix4f();

    /**
     * Optional camera for controlling the view
     */
    protected Camera2D camera;

    /**
     * Creates a new viewport with the specified logical world dimensions.
     *
     * @param worldWidth  The width of the logical world
     * @param worldHeight The height of the logical world
     */
    public Viewport(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    /**
     * Sets the camera used by this viewport.
     *
     * @param camera The camera to use, or null for no camera
     */
    public void setCamera(Camera2D camera) {
        this.camera = camera;
        this.camera.setCenter(this.worldWidth / 2, this.worldHeight / 2);
    }

    /**
     * Updates the viewport dimensions. Must be implemented by subclasses.
     *
     * @param screenWidth  The new screen width
     * @param screenHeight The new screen height
     */
    public abstract void update(int screenWidth, int screenHeight);

    /**
     * Applies the viewport settings to the OpenGL context.
     * This method sets the OpenGL viewport to the screen coordinates and dimensions
     * defined by the viewport's properties and adjusts the projection matrix.
     * <p>
     * If a camera is set, its projection matrix is recalculated and applied.
     * Otherwise, a default projection matrix is used.
     * <p>
     * The method first updates the OpenGL viewport to match the dimensions of the
     * screen region defined by the viewport. Then, if a camera is set, it recalculates
     * the camera's projection matrix based on the configured logical world dimensions
     * and retrieves its values. In the absence of a camera, it applies the default
     * projection matrix.
     * <p>
     * Finally, the method configures the OpenGL projection matrix and resets the
     * model-view matrix for rendering operations.
     */
    public void apply() {
        glViewport(screenX, screenY, screenWidth, screenHeight);

        if (camera != null) {
            camera.rebuild(worldWidth, worldHeight);
            camera.getProjection().get(mat);
        } else {
            projectionMatrix.get(mat);
        }

        glMatrixMode(GL_PROJECTION);
        glLoadMatrixf(mat);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    /**
     * Applies viewport settings and executes the provided rendering code.
     *
     * @param renderer Runnable containing rendering commands
     */
    public void render(Runnable renderer) {
        apply();
        renderer.run();
    }

    /**
     * @return The logical world width
     */
    public float getWorldWidth() {
        return worldWidth;
    }

    /**
     * @return The logical world height
     */
    public float getWorldHeight() {
        return worldHeight;
    }

    public Camera2D getCamera() {
        return camera;
    }
}