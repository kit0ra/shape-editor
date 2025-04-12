package com.editor.shapes;

import java.awt.Color;
import java.awt.Point;

import com.editor.drawing.Drawer;

/**
 * Represents a circle shape.
 */
public class Circle implements Shape {
    private static final long serialVersionUID = 1L;

    private int x;
    private int y;
    private int radius;
    private Color fillColor = Color.GREEN; // Default fill color for circles
    private Color borderColor = Color.BLACK; // Default border color
    private double rotation = 0.0; // Rotation in degrees (not really used for circles but kept for consistency)
    private boolean selected = false; // Selection state

    /**
     * Creates a new circle with the specified position and radius.
     *
     * @param x      The x-coordinate of the center
     * @param y      The y-coordinate of the center
     * @param radius The radius of the circle
     */
    public Circle(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * Gets the x-coordinate of the circle's center.
     *
     * @return The x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of the circle's center.
     *
     * @param x The new x-coordinate
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gets the y-coordinate of the circle's center.
     *
     * @return The y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of the circle's center.
     *
     * @param y The new y-coordinate
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Gets the radius of the circle.
     *
     * @return The radius
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the circle.
     *
     * @param radius The new radius
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Gets the fill color of the circle.
     *
     * @return The fill color
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Sets the fill color of the circle.
     *
     * @param fillColor The new fill color
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Gets the border color of the circle.
     *
     * @return The border color
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the border color of the circle.
     *
     * @param borderColor The new border color
     */
    @Override
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Gets the rotation of the circle (not really used for circles).
     *
     * @return The rotation in degrees
     */
    @Override
    public double getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation of the circle (not really used for circles).
     *
     * @param rotation The new rotation in degrees
     */
    @Override
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    /**
     * Checks if a point is inside the circle.
     *
     * @param point The point to check
     * @return true if the point is inside the circle, false otherwise
     */
    public boolean contains(Point point) {
        // Calculate the distance from the point to the center of the circle
        double distance = Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));

        // The point is inside the circle if the distance is less than or equal to the
        // radius
        return distance <= radius;
    }

    /**
     * Moves the circle by the specified delta.
     *
     * @param dx The delta x
     * @param dy The delta y
     */
    @Override
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    @Override
    public void setPosition(int x, int y) {
        // Convert top-left coordinates to center coordinates
        // x and y are the coordinates of the top-left corner of the bounding rectangle
        this.x = x + radius;
        this.y = y + radius;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public void draw(Drawer drawer) {
        drawer.drawCircle(this);
    }

    /**
     * Creates a clone of this circle.
     *
     * @return A new Circle with the same properties
     */
    @Override
    public Shape clone() {
        // Create a new Circle with the same properties
        Circle clone = new Circle(x, y, radius);
        clone.setSelected(selected);
        clone.setFillColor(fillColor);
        clone.setBorderColor(borderColor);
        clone.setRotation(rotation);
        return clone;
    }

    /**
     * Checks if the circle is selected.
     *
     * @return true if the circle is selected, false otherwise
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets the selection state of the circle.
     *
     * @param selected The new selection state
     */
    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Checks if a point is inside the circle.
     *
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @return true if the point is inside the circle, false otherwise
     */
    @Override
    public boolean isSelected(int x, int y) {
        // Calculate the distance from the point to the center of the circle
        double distance = Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));

        // The point is inside the circle if the distance is less than or equal to the
        // radius
        return distance <= radius;
    }
}
