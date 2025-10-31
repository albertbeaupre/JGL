package jgl;

import jgl.event.impl.MouseMoveEvent;
import jgl.event.impl.MousePressEvent;
import jgl.event.impl.MouseReleaseEvent;
import jgl.event.impl.MouseScrollEvent;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

/**
 * The Mouse class provides functionality for handling mouse input in a GLFW-based application.
 * It manages mouse movement, button clicks, and scroll events.
 * This is a utility class with only static methods and fields, and cannot be instantiated.
 * <p>
 * Key features:
 * - Tracks the current mouse cursor position.
 * - Tracks the current state of mouse buttons.
 * - Tracks scroll wheel activity in both X and Y directions.
 * - Tracks modifier keys (Shift, Ctrl, Alt, Super) during mouse events.
 */
public final class Mouse {

    private static GLFWCursorPosCallback cursorPosCallback;
    private static GLFWMouseButtonCallback mouseButtonCallback;
    private static GLFWScrollCallback scrollCallback;

    private static short x;
    private static short y;

    private Mouse() {
        // Inaccessible
    }

    static void init() {
        cursorPosCallback = glfwSetCursorPosCallback(Window.getAddress(), (win, xpos, ypos) -> {
            short fromX = x;
            short fromY = y;
            x = (short) xpos;
            y = (short) ypos;
            JGL.publish(new MouseMoveEvent(fromX, fromY, x, y));
        });

        mouseButtonCallback = glfwSetMouseButtonCallback(Window.getAddress(), (win, button, action, mods) -> {
            if (button < 0 || button > GLFW_MOUSE_BUTTON_LAST)
                return;

            if (action != GLFW_RELEASE) {
                JGL.publish(new MouseReleaseEvent(x, y, button, mods));
            } else {
                JGL.publish(new MousePressEvent(x, y, button, mods));
            }
        });

        scrollCallback = glfwSetScrollCallback(Window.getAddress(), (win, xoff, yoff) -> {
            JGL.publish(new MouseScrollEvent((int) xoff, (int) yoff));
        });
    }

    static void dispose() {
        if (cursorPosCallback != null) cursorPosCallback.free();
        if (mouseButtonCallback != null) mouseButtonCallback.free();
        if (scrollCallback != null) scrollCallback.free();
    }

    public static short getX() {
        return x;
    }

    public static short getY() {
        return y;
    }

    /**
     * Left mouse button (button 0).
     */
    public static final int LEFT = 0;

    /**
     * Right mouse button (button 1).
     */
    public static final int RIGHT = 1;

    /**
     * Middle mouse button (button 2).
     */
    public static final int MIDDLE = 2;

    /**
     * Mouse button 3 (usually side / extra button 1).
     */
    public static final int BUTTON_3 = 3;

    /**
     * Mouse button 4 (usually side / extra button 2).
     */
    public static final int BUTTON_4 = 4;

    /**
     * Mouse button 5 (usually side / extra button 3).
     */
    public static final int BUTTON_5 = 5;

    /**
     * Mouse button 6 (usually side / extra button 4).
     */
    public static final int BUTTON_6 = 6;

    /**
     * Mouse button 7 (usually side / extra button 5).
     */
    public static final int BUTTON_7 = 7;
}
