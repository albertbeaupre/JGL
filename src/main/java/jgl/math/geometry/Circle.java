package jgl.math.geometry;

public class Circle extends Shape {

    private float radius;

    public Circle(float x, float y, float radius) {
        this.setPosition(x, y);
        this.setSize(radius * 2, radius * 2);
        this.radius = radius;
    }

    public void setRadius(float r) {
        this.radius = r;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.Circle;
    }
}
