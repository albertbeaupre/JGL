package jgl.event.events;

import jgl.event.Event;

public class MouseScrollEvent extends Event {

    private short xOffset, yOffset;

    public MouseScrollEvent(int xOffset, int yOffset) {
        this.xOffset = (short) xOffset;
        this.yOffset = (short) yOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = (short) xOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = (short) yOffset;
    }

    public int xOffset() {
        return xOffset;
    }

    public int yOffset() {
        return yOffset;
    }
}
