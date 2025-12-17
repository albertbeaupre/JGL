package jgl.event.events;

public class MouseMoveEvent extends MouseEvent {

    private final short fromX, fromY;
    private final short toX, toY;

    public MouseMoveEvent(int button, int modifiers, int fromX, int fromY, int toX, int toY) {
        super(fromX, fromY, button, modifiers);
        this.fromX = (short) fromX;
        this.fromY = (short) fromY;
        this.toX = (short) toX;
        this.toY = (short) toY;
    }

    public int fromX() {
        return fromX;
    }

    public int fromY() {
        return fromY;
    }

    public int toX() {
        return toX;
    }

    public int toY() {
        return toY;
    }
}
