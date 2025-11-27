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
        float maxX = MathUtils.max(x1, x2, x3);
        float minX = MathUtils.min(x1, x2, x3);
        float borderThickness = 0;

        if (this.getBorder() != null)
            borderThickness = this.getBorder().getThickness() * 6;

        return maxX - minX + borderThickness;
    }

    @Override
    public float getHeight() {
        float y1 = this.getY();
        float maxY = MathUtils.max(y1, y2, y3);
        float minY = MathUtils.min(y1, y2, y3);
        float borderThickness = 0;

        if (this.getBorder() != null)
            borderThickness = this.getBorder().getThickness() * 6;

        return maxY - minY + borderThickness;
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
