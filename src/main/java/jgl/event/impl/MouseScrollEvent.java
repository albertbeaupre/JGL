package jgl.event.impl;

import jgl.event.Event;

public class MouseScrollEvent extends Event {

    private final short xOffset, yOffset;

    public MouseScrollEvent(int xOffset, int yOffset) {
        this.xOffset = (short) xOffset;
        this.yOffset = (short) yOffset;
    }

    public int xOffset() {
        return xOffset;
    }

    public int yOffset() {
        return yOffset;
    }
}
