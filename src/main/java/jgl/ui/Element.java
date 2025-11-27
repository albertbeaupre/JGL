package jgl.ui;

public abstract class Element {

    private Element parent;
    private int index;

    public abstract void update(double delta);
    public abstract void render();

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

}
