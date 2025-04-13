package com.editor.shapes;

import java.awt.Color;
import java.awt.Polygon;

import com.editor.drawing.Drawer;

/**
 * Represents a regular polygon with a specified number of sides and radius.
 */
public class RegularPolygon implements Shape {
    private static final long serialVersionUID = 1L;
    private int x, y;
    private final int radius;
    private final int numberOfSides;
    private boolean selected;
    private Color fillColor = new Color(255, 182, 193); // Light pink default fill color
    private Color borderColor = Color.BLACK;
    private double rotation = 0.0;

    /**
     * Creates a new regular polygon.
     *
     * @param x             The x-coordinate of the center
     * @param y             The y-coordinate of the center
     * @param radius        The radius of the polygon
     * @param numberOfSides The number of sides
     */
    public RegularPolygon(int x, int y, int radius, int numberOfSides) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.numberOfSides = numberOfSides;
    }

    @Override
    public void draw(Drawer drawer) {
        drawer.drawRegularPolygon(this);
    }

    @Override
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x + radius;
        this.y = y + radius;
    }

    @Override
    public boolean isSelected(int testX, int testY) {
        int[] xPoints = new int[numberOfSides];
        int[] yPoints = new int[numberOfSides];
        double angleStep = 2 * Math.PI / numberOfSides;

        for (int i = 0; i < numberOfSides; i++) {
            xPoints[i] = (int) (x + radius * Math.cos(i * angleStep));
            yPoints[i] = (int) (y + radius * Math.sin(i * angleStep));
        }

        Polygon polygon = new Polygon(xPoints, yPoints, numberOfSides);
        return polygon.contains(testX, testY);
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public Rectangle getBounds() {
        int minX = x - radius;
        int minY = y - radius;
        int width = radius * 2;
        int height = radius * 2;

        return new Rectangle(minX, minY, width, height);
    }

    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public Shape clone() {
        try {
            RegularPolygon clone = (RegularPolygon) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            RegularPolygon clone = new RegularPolygon(x, y, radius, numberOfSides);
            clone.setSelected(selected);
            clone.setFillColor(fillColor);
            clone.setBorderColor(borderColor);
            clone.setRotation(rotation);
            return clone;
        }
    }

    /**
     * Gets the x-coordinate of the center.
     *
     * @return The x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the center.
     *
     * @return The y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the radius of the polygon.
     *
     * @return The radius
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Gets the number of sides of the polygon.
     *
     * @return The number of sides
     */
    public int getNumberOfSides() {
        return numberOfSides;
    }

    /**
     * Sets the fill color for this polygon.
     *
     * @param color The new fill color
     */
    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    /**
     * Sets the border color for this polygon.
     *
     * @param color The new border color
     */
    @Override
    public void setBorderColor(Color color) {
        this.borderColor = color;
    }

    /**
     * Gets the fill color for this polygon.
     *
     * @return The current fill color
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Gets the border color for this polygon.
     *
     * @return The current border color
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the rotation angle for this polygon
     *
     * @param degrees The rotation angle in degrees
     */
    @Override
    public void setRotation(double degrees) {
        this.rotation = degrees;
    }

    /**
     * Gets the rotation angle for this polygon
     *
     * @return The current rotation angle in degrees
     */
    @Override
    public double getRotation() {
        return rotation;
    }
}
