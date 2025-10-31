package jgl;

import jgl.enums.SwapInterval;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowContentScaleCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowMaximizeCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_DOUBLEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowCloseCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowContentScaleCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIconifyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMaximizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Window {

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

    public static void init(String title, int width, int height) {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);

        address = glfwCreateWindow(width, height, title, NULL, NULL);

        if (address == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(address);
        glfwSwapInterval(swapInterval.getInterval());
        GL.createCapabilities();

        fbCallback = glfwSetFramebufferSizeCallback(address, (win, newW, newH) -> {

        });
        sizeCallback = glfwSetWindowSizeCallback(address, (win, newW, newH) -> {

        });
        posCallback = glfwSetWindowPosCallback(address, (win, newX, newY) -> {
        });
        focusCallback = glfwSetWindowFocusCallback(address, (win, focused) -> {

        });
        iconifyCallback = glfwSetWindowIconifyCallback(address, (win, iconified) -> {
        });
        maximizeCallback = glfwSetWindowMaximizeCallback(address, (win, maximized) -> {
        });
        scaleCallback = glfwSetWindowContentScaleCallback(address, (win, xs, ys) -> {
        });
        closeCallback = glfwSetWindowCloseCallback(address, (win) -> glfwSetWindowShouldClose(win, true));

        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vid != null) {
            int centerX = (vid.width() - width) / 2;
            int centerY = (vid.height() - height) / 2;
            glfwSetWindowPos(address, centerX, centerY);
        }

        glfwShowWindow(address);
        glViewport(0, 0, width, height);
    }

    static boolean shouldClose() {
        return glfwWindowShouldClose(address);
    }

    public static void setTitle(String newTitle) {
        glfwSetWindowTitle(address, newTitle);
    }

    public static void setFullscreen(boolean fullscreen) {
        if (address == NULL) return;

        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vid == null)
            throw new RuntimeException("Failed to get video mode");

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
        if (fbCallback != null) fbCallback.free();
        if (sizeCallback != null) sizeCallback.free();
        if (posCallback != null) posCallback.free();
        if (focusCallback != null) focusCallback.free();
        if (iconifyCallback != null) iconifyCallback.free();
        if (maximizeCallback != null) maximizeCallback.free();
        if (scaleCallback != null) scaleCallback.free();
        if (closeCallback != null) closeCallback.free();
        if (address != NULL) glfwDestroyWindow(address);
    }

    public static long getAddress() {
        return address;
    }
}
