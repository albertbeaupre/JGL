package jgl.event.events;

import jgl.event.Event;

public class WindowResizeEvent extends Event {

    private short oldWidth, oldHeight;
    private short newWidth, newHeight;

    public WindowResizeEvent(int oldWidth, int oldHeight, int newWidth, int newHeight) {
        this.oldWidth = (short) oldWidth;
        this.oldHeight = (short) oldHeight;
        this.newWidth = (short) newWidth;
        this.newHeight = (short) newHeight;
    }

    public void setOldWidth(short oldWidth) {
        this.oldWidth = oldWidth;
    }

    public void setOldHeight(short oldHeight) {
        this.oldHeight = oldHeight;
    }

    public void setNewWidth(short newWidth) {
        this.newWidth = newWidth;
    }

    public void setNewHeight(short newHeight) {
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
