package jgl.math.geometry;

public abstract class Shape {
    private ShapeRenderer renderer;
    private int index;
    private float x, y, rotation = 0, scaleX = 1, scaleY = 1, transformX, transformY;
    private float width, height;
    private Color color = new Color(0);
    private Border border;

    protected void setRenderer(ShapeRenderer renderer) {
        this.renderer = renderer;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScale(float sx, float sy) {
        this.scaleX = sx;
        this.scaleY = sy;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Border getBorder() {
        return border;
    }

    public void setBorder(Border border) {
        this.border = border;
    }

    public abstract ShapeType getType();

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getTransformX() {
        return transformX;
    }

    public float getTransformY() {
        return transformY;
    }

    public void setTransform(float x, float y) {
        this.transformX = x;
        this.transformY = y;
    }


    protected void setIndex(int index) {
        this.index = index;
    }

    protected int getIndex() {
        return index;
    }

}
