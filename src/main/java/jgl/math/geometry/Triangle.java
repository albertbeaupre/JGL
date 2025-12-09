package jgl.math.geometry;

import jgl.math.MathUtils;

public class Triangle extends Shape {

    private float x2, y2;
    private float x3, y3;

    public Triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        setPosition(x1, y1);
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    @Override
    public float getWidth() {
        float x1 = this.getX();
        float dx2 = x2 - x1;
        float dx3 = x3 - x1;

        float maxDx = MathUtils.max(Math.abs(dx2), Math.abs(dx3));
        float borderThickness = 0;

        if (this.getBorder() != null)
            borderThickness = this.getBorder().getThickness() * 6;

        // Domain is symmetric around vertex 1 as origin: [-maxDx, +maxDx]
        return maxDx * 2.0f + borderThickness;
    }

    @Override
    public float getHeight() {
        float y1 = this.getY();
        float dy2 = y2 - y1;
        float dy3 = y3 - y1;

        float maxDy = MathUtils.max(Math.abs(dy2), Math.abs(dy3));
        float borderThickness = 0;

        if (this.getBorder() != null)
            borderThickness = this.getBorder().getThickness() * 6;

        // Domain is symmetric around vertex 1 as origin: [-maxDy, +maxDy]
        return maxDy * 2.0f + borderThickness;
    }

    public float getX2() {
        return x2;
    }

    public float getY2() {
        return y2;
    }

    public float getX3() {
        return x3;
    }

    public float getY3() {
        return y3;
    }

    public void setX2(float x) {
        this.x2 = x;
    }

    public void setY2(float y) {
        this.y2 = y;
    }

    public void setX3(float x) {
        this.x3 = x;
    }

    public void setY3(float y) {
        this.y3 = y;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.Triangle;
    }
}
