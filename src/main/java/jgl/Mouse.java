package jgl;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

public final class Mouse {

    private static GLFWCursorPosCallback cursorPosCallback;
    private static GLFWMouseButtonCallback mouseButtonCallback;
    private static GLFWScrollCallback scrollCallback;

    private static short x;
    private static short y;
    private static byte buttons;

    static void init() {
        cursorPosCallback = glfwSetCursorPosCallback(Window.getAddress(), (win, xpos, ypos) -> {
            x = (short) xpos;
            y = (short) ypos;
        });

        mouseButtonCallback = glfwSetMouseButtonCallback(Window.getAddress(), (win, button, action, mods) -> {
            if (button < 8) {
                byte mask = (byte) (1 << button);
                if (action != GLFW_RELEASE)
                    buttons |= mask;
                else buttons &= ~mask;
            }
        });

        scrollCallback = glfwSetScrollCallback(Window.getAddress(), (win, xoff, yoff) -> {
        });
    }

    static void update() {

    }

    static void dispose() {
        if (cursorPosCallback != null) cursorPosCallback.free();
        if (mouseButtonCallback != null) mouseButtonCallback.free();
        if (scrollCallback != null) scrollCallback.free();
    }

    public boolean isButtonDown(int button) {
        if (button < 0 || button >= 8) return false;
        return (buttons & (1 << button)) != 0;
    }

    public static short getX() {
        return x;
    }

    public static short getY() {
        return y;
    }
}
