package jgl.math.geometry;

import jgl.graphics.Color;

public class Border {

    private Color color;
    private byte thickness;

    public Border(Color color, int thickness) {
        this.color = color;
        this.thickness = (byte) thickness;
    }

    public Color getColor() {
        return color;
    }

    public byte getThickness() {
        return thickness;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public void setThickness(byte t) {
        this.thickness = t;
    }
}
