package jgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public final class Window {

    private static final int STATE_RESIZED = 1 << 0;
    private static final int STATE_FOCUSED = 1 << 1;
    private static final int STATE_ICONIFIED = 1 << 2;
    private static final int STATE_MAXIMIZED = 1 << 3;
    private static final int STATE_VISIBLE = 1 << 4;
    private static final int STATE_CLOSE_REQ = 1 << 5;
    private static final int STATE_SCALE_CHANGED = 1 << 6;

    private static GLFWErrorCallback errorCallback;
    private static GLFWFramebufferSizeCallback fbCallback;
    private static GLFWWindowFocusCallback focusCallback;
    private static GLFWWindowIconifyCallback iconifyCallback;
    private static GLFWWindowMaximizeCallback maximizeCallback;
    private static GLFWWindowCloseCallback closeCallback;
    private static GLFWWindowPosCallback posCallback;
    private static GLFWWindowSizeCallback sizeCallback;
    private static GLFWWindowContentScaleCallback scaleCallback;

    private static long address;
    private static SwapInterval swapInterval = SwapInterval.OFF;
    private static byte state;

    public static void init(String title, int width, int height) {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);


        address = glfwCreateWindow(width, height, title, NULL, NULL);

        if (address == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(address);
        glfwSwapInterval(swapInterval.getInterval());
        GL.createCapabilities();

        fbCallback = glfwSetFramebufferSizeCallback(address, (win, newW, newH) -> state |= STATE_RESIZED);
        sizeCallback = glfwSetWindowSizeCallback(address, (win, newW, newH) -> state |= STATE_RESIZED);
        posCallback = glfwSetWindowPosCallback(address, (win, newX, newY) -> {});
        focusCallback = glfwSetWindowFocusCallback(address, (win, focused) -> state = focused ? (byte) (state | STATE_FOCUSED) : (byte) (state & ~STATE_FOCUSED));
        iconifyCallback = glfwSetWindowIconifyCallback(address, (win, iconified) -> state = iconified ? (byte) (state | STATE_ICONIFIED) : (byte) (state & ~STATE_ICONIFIED));
        maximizeCallback = glfwSetWindowMaximizeCallback(address, (win, maximized) -> state = maximized ? (byte) (state | STATE_MAXIMIZED) : (byte) (state & ~STATE_MAXIMIZED));
        scaleCallback = glfwSetWindowContentScaleCallback(address, (win, xs, ys) -> state |= STATE_SCALE_CHANGED);
        closeCallback = glfwSetWindowCloseCallback(address, (win) -> state |= STATE_CLOSE_REQ);

        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vid != null) {
            int centerX = (vid.width() - width) / 2;
            int centerY = (vid.height() - height) / 2;
            glfwSetWindowPos(address, centerX, centerY);
        }

        glfwShowWindow(address);
        state |= STATE_VISIBLE;

        glViewport(0, 0, width, height);
    }

    static void update() {
        if ((state & STATE_RESIZED) != 0) {
            int[] size = getFramebufferSize();
            glViewport(0, 0, size[0], size[1]);
            state &= ~STATE_RESIZED;
        }

        if ((state & STATE_CLOSE_REQ) != 0) glfwSetWindowShouldClose(address, true);
    }

    static boolean shouldClose() {
        return (state & STATE_CLOSE_REQ) != 0;
    }

    public static void setTitle(String newTitle) {
        glfwSetWindowTitle(address, newTitle);
    }

    public static void setFullscreen(boolean fullscreen) {
        if (address == NULL) return;

        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vid == null) throw new RuntimeException("Failed to get video mode");

        int[] size = getWindowSize();
        int[] pos = getWindowPos();

        if (fullscreen) {
            glfwSetWindowMonitor(address, glfwGetPrimaryMonitor(), 0, 0, vid.width(), vid.height(), vid.refreshRate());
        } else {
            glfwSetWindowMonitor(address, NULL, pos[0], pos[1], size[0], size[1], vid.refreshRate());
        }
    }

    public static void setSize(int width, int height) {
        glfwSetWindowSize(address, width, height);
    }

    public static int[] getWindowSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetWindowSize(address, w, h);
            return new int[]{w.get(0), h.get(0)};
        } catch (Exception e) {
            return null;
        }
    }

    public static int[] getFramebufferSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetFramebufferSize(address, w, h);
            return new int[]{w.get(0), h.get(0)};
        }
    }

    public static int[] getWindowPos() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            glfwGetWindowPos(address, x, y);
            return new int[]{x.get(0), y.get(0)};
        }
    }

    public static void setSwapInterval(SwapInterval type) {
        if (type == null) return;
        swapInterval = type;
        glfwSwapInterval(type.getInterval());
    }

    public static SwapInterval getSwapInterval() {
        return swapInterval;
    }

    protected static void dispose() {
        if (address != NULL) glfwDestroyWindow(address);
    }

    public static long getAddress() {
        return address;
    }
}
