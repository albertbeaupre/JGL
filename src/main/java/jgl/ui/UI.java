package jgl.ui;

public class UI extends ElementContainer {

    private Element focused;

    public Element getFocused() {
        return focused;
    }

    @Override
    public Element getParent() {
        return this;
    }
}
