package jgl.event.events;

import jgl.event.Event;

import static org.lwjgl.glfw.GLFW.GLFW_MOD_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SUPER;

public class KeyEvent extends Event {

    private final short key;
    private final byte modifiers;

    public KeyEvent(int key, int modifiers) {
        this.key = (short) key;
        this.modifiers = (byte) modifiers;
    }

    public short getKey() {
        return key;
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
