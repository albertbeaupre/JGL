package jgl.event.events;

import jgl.event.Event;

import static org.lwjgl.glfw.GLFW.GLFW_MOD_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SUPER;

public class MouseEvent extends Event {

    private final short x, y;
    private final byte button;
    private final byte modifiers;

    public MouseEvent(int x, int y, int button, int modifiers) {
        this.x = (short) x;
        this.y = (short) y;
        this.button = (byte) button;
        this.modifiers = (byte) modifiers;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getButton() {
        return button;
    }

    public boolean isShiftDown() {
        return (modifiers & GLFW_MOD_SHIFT) != 0;
    }

    public boolean isCtrlDown() {
        return (modifiers & GLFW_MOD_CONTROL) != 0;
    }

    public boolean isAltDown() {
        return (modifiers & GLFW_MOD_ALT) != 0;
    }

    public boolean isSuperDown() {
        return (modifiers & GLFW_MOD_SUPER) != 0;
    }

}
