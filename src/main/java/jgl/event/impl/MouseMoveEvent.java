package jgl.event.impl;

import jgl.event.Event;

public class MouseMoveEvent extends Event {

    private final short fromX, fromY;
    private final short toX, toY;

    public MouseMoveEvent(int fromX, int fromY, int toX, int toY) {
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
