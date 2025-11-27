package jgl.math.geometry;

public class Line extends Shape {

    private float x2, y2;
    private float thickness;

    public Line(float thickness, float x1, float y1, float x2, float y2) {
        this.setPosition(x1, y1);
        this.setSize(Math.abs(x1 - x2), Math.abs(y1 - y2));
        this.thickness = thickness;
        this.x2 = x2;
        this.y2 = y2;
    }

    public float getThickness() {
        return thickness;
    }

    public float getX2() {
        return x2;
    }

    public float getY2() {
        return y2;
    }

    public void setThickness(float t) {
        this.thickness = t;
    }

    public void setX2(float x) {
        this.x2 = x;
    }

    public void setY2(float y) {
        this.y2 = y;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.Line;
    }

}
