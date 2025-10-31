package jgl.event.events;

import jgl.event.Event;

public class WindowResizeEvent extends Event {

    private final short oldWidth, oldHeight;
    private final short newWidth, newHeight;

    public WindowResizeEvent(short oldWidth, short oldHeight, short newWidth, short newHeight) {
        this.oldWidth = oldWidth;
        this.oldHeight = oldHeight;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
    }

    public int getOldWidth() {
        return oldWidth;
    }

    public int getOldHeight() {
        return oldHeight;
    }

    public int getNewWidth() {
        return newWidth;
    }

    public int getNewHeight() {
        return newHeight;
    }

}
