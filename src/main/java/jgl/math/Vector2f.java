package jgl.math;

/**
 * Represents a 2D vector with float components. Provides methods for common vector
 * operations such as addition, subtraction, scalar multiplication, and linear interpolation.
 */
public class Vector2f {
    /**
     * The x-coordinate of this vector
     */
    public float x;

    /**
     * The y-coordinate of this vector
     */
    public float y;

    /**
     * Creates a zero vector (0,0)
     */
    public Vector2f() {
        this(0, 0);
    }

    /**
     * Creates a vector with the given coordinates
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets this vector's coordinates to the specified values
     *
     * @param x The new x-coordinate
     * @param y The new y-coordinate
     * @return This vector for chaining
     */
    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Adds the specified offsets to this vector's coordinates
     *
     * @param dx The x offset to add
     * @param dy The y offset to add
     * @return This vector for chaining
     */
    public Vector2f add(float dx, float dy) {
        this.x += dx;
        this.y += dy;
        return this;
    }

    /**
     * Adds another vector to this vector
     *
     * @param v The vector to add
     * @return This vector for chaining
     */
    public Vector2f add(Vector2f v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    /**
     * Subtracts another vector from this vector
     *
     * @param v The vector to subtract
     * @return This vector for chaining
     */
    public Vector2f sub(Vector2f v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    /**
     * Multiplies this vector by a scalar value
     *
     * @param s The scalar to multiply by
     * @return This vector for chaining
     */
    public Vector2f mul(float s) {
        this.x *= s;
        this.y *= s;
        return this;
    }

    /**
     * Linearly interpolates this vector toward target vector by alpha amount
     *
     * @param t The target vector
     * @param a The interpolation factor (0-1)
     * @return This vector for chaining
     */
    public Vector2f lerp(Vector2f t, float a) {
        this.x += (t.x - this.x) * a;
        this.y += (t.y - this.y) * a;
        return this;
    }

    /**
     * Creates and returns a copy of this vector
     *
     * @return A new vector with the same coordinates
     */
    public Vector2f copy() {
        return new Vector2f(x, y);
    }

    /**
     * Returns a string representation of this vector
     *
     * @return String in format "(x, y)"
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}