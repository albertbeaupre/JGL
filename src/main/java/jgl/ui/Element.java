package jgl.ui;

public abstract class Element {

    private static final byte HIDDEN = 1;
    private static final byte DISABLED = 1 << 1;
    private static final byte HOVERED = 1 << 2;
    private static final byte FOCUSED = 1 << 3;
    private static final byte PRESSED = 1 << 4;
    private static final byte FOCUSABLE = 1 << 5;

    private Element parent;
    private int index = -1;
    private byte flags;

    public abstract void update(float delta);

    public abstract void draw();

    protected void setIndex(int index) {
        this.index = index;
    }

    protected int getIndex() {
        return index;
    }

    protected void setParent(Element parent) {
        this.parent = parent;
    }

    public Element getParent() {
        return parent;
    }

    public boolean isFocusable() {
        return (flags & FOCUSABLE) != 0;
    }

    public void setFocusable(boolean focusable) {
        if (focusable) flags |= FOCUSABLE;
        else flags &= ~FOCUSABLE;
    }

    public boolean isHidden() {
        return (flags & HIDDEN) != 0;
    }

    public void setHidden(boolean value) {
        if (value) flags |= HIDDEN;
        else flags &= ~HIDDEN;
    }

    public boolean isDisabled() {
        return (flags & DISABLED) != 0;
    }

    public void setDisabled(boolean value) {
        if (value) flags |= DISABLED;
        else flags &= ~DISABLED;
    }

    public boolean isPressed() {
        return (flags & PRESSED) != 0;
    }

    public void setPressed(boolean value) {
        if (value) flags |= PRESSED;
        else flags &= ~PRESSED;
    }

    public boolean isHovered() {
        return (flags & HOVERED) != 0;
    }

    public void setHovered(boolean value) {
        if (value) flags |= HOVERED;
    }

    public boolean isFocused() {
        return (flags & FOCUSED) != 0;
    }

    public void setFocused(boolean value) {
        if (value) flags |= FOCUSED;
    }
}
