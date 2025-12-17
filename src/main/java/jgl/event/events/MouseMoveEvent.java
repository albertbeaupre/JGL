package jgl.event.events;

public class MouseMoveEvent extends MouseEvent {

    private short toX, toY;

    public MouseMoveEvent(int button, int modifiers, int fromX, int fromY, int toX, int toY) {
        super(fromX, fromY, button, modifiers);
        this.toX = (short) toX;
        this.toY = (short) toY;
    }

    public void setToX(short toX) {
        this.toX = toX;
    }

    public void setToY(short toY) {
        this.toY = toY;
    }

    public int toX() {
        return toX;
    }

    public int toY() {
        return toY;
    }
}
