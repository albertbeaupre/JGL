package jgl;

import java.nio.IntBuffer;
import jgl.enums.SwapInterval;
import jgl.event.events.WindowResizeEvent;
import jgl.event.listeners.WindowResizeListener;
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
import static org.lwjgl.opengl.GL11.glViewport;
import org.lwjgl.system.MemoryStack;
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
    private static short x, y;
    private static short width, height;

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
            short oldWidth = Window.width;
            short oldHeight = Window.height;
            Window.width = (short) newW;
            Window.height = (short) newH;

            JGL.publish(new WindowResizeEvent(oldWidth, oldHeight, Window.width, Window.height));
        });
        posCallback = glfwSetWindowPosCallback(address, (win, newX, newY) -> {
            Window.x = (short) newX;
            Window.y = (short) newY;
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

    public static void addWindowResizeListener(WindowResizeListener listener) {
        if (listener == null)
            throw new NullPointerException("A null WindowResizeListener cannot be added to the Window");
        JGL.registerEventListener(WindowResizeEvent.class, listener);
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


        if (fullscreen) {
            glfwSetWindowMonitor(address, glfwGetPrimaryMonitor(), 0, 0, vid.width(), vid.height(), vid.refreshRate());
        } else {
            glfwSetWindowMonitor(address, NULL, getX(), getY(), getWidth(), getHeight(), vid.refreshRate());
        }
    }

    public static void setSize(int width, int height) {
        glfwSetWindowSize(address, width, height);
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static int[] getFramebufferSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetFramebufferSize(address, w, h);
            return new int[]{w.get(0), h.get(0)};
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
