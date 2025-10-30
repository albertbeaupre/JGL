package jgl;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

public class JGL {

    private static double deltaTime;
    private static short framesPerSecond;

    public static void init(Application application, String title, int width, int height) {
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        Window.init(title, width, height);
        Mouse.init();
        Keyboard.init();

        double lastTime = glfwGetTime();
        double fpsTime = 0;

        application.init();
        while (!Window.shouldClose()) {
            double now = glfwGetTime();
            deltaTime = now - lastTime;
            lastTime = now;

            glfwPollEvents();
            glfwSwapBuffers(Window.getAddress());

            Window.update();
            Keyboard.update();
            Mouse.update();

            application.update(deltaTime);
            application.render();

            fpsTime += deltaTime;
            framesPerSecond++;

            if (fpsTime >= 1) {
                fpsTime = 0;
                System.out.println(framesPerSecond);
                framesPerSecond = 0;
            }
        }
        application.dispose();
        dispose();
    }

    public static double getFramesPerSecond() {
        return framesPerSecond;
    }

    public static double getDeltaTime() {
        return deltaTime;
    }

    public static void dispose() {
        Keyboard.dispose();
        Mouse.dispose();
        Window.dispose();

        glfwTerminate();
    }
}