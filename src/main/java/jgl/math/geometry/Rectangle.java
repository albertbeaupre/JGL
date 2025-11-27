package jgl.math.geometry;

public class Rectangle extends Shape {

    private byte bottomLeftRadius;
    private byte topLeftRadius;
    private byte topRightRadius;
    private byte bottomRightRadius;

    public Rectangle(float x, float y, float width, float height) {
        this.setPosition(x, y);
        this.setSize(width, height);
    }

    public void setRadius(int radius) {
        this.bottomLeftRadius = this.topLeftRadius = this.topRightRadius = this.bottomRightRadius = (byte) radius;
    }

    public byte getBottomLeftRadius() {
        return bottomLeftRadius;
    }

    public byte getTopLeftRadius() {
        return topLeftRadius;
    }

    public byte getTopRightRadius() {
        return topRightRadius;
    }

    public byte getBottomRightRadius() {
        return bottomRightRadius;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.Rectangle;
    }
}
