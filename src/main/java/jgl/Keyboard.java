package jgl;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Keyboard {

    private static GLFWKeyCallback keyCallback;
    private static final int[] keyBits = new int[(GLFW_KEY_LAST / Integer.SIZE) + 1];

    public static void init() {
        keyCallback = glfwSetKeyCallback(Window.getAddress(), (win, key, scancode, action, mods) -> {
            if (key >= 0 && key <= GLFW_KEY_LAST)
                setKeyState(key, action != GLFW_RELEASE);
        });
    }

    protected static void update() {

    }

    protected static void dispose() {
        if (keyCallback != null) keyCallback.free();
    }

    private static void setKeyState(int key, boolean down) {
        if (key < 0 || key > GLFW_KEY_LAST) return;
        int i = key >> 5, b = 1 << (key & (Integer.SIZE - 1));
        if (down) keyBits[i] |= b;
        else keyBits[i] &= ~b;
    }

    public static boolean isKeyDown(int key) {
        if (key < 0 || key > GLFW_KEY_LAST) return false;

        int i = key >> 5, b = 1 << (key & (Integer.SIZE - 1));
        return (keyBits[i] & b) != 0;
    }

}
