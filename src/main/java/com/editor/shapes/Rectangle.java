package com.editor.shapes;

import java.awt.Color;

import com.editor.drawing.Drawer;

public class Rectangle implements Shape {
    private static final long serialVersionUID = 1L;
    private int x, y, width, height;
    private boolean selected;
    private Color fillColor = Color.BLUE;
    private Color borderColor = Color.BLACK;
    private double rotation = 0.0;
    private int borderRadius = 0;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Drawer drawer) {
        drawer.drawRectangle(this);
    }

    @Override
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean isSelected(int x, int y) {
        return x >= this.x && x <= this.x + width &&
                y >= this.y && y <= this.y + height;
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
        return new Rectangle(x, y, width, height);
    }

    @Override
    public Shape clone() {
        Rectangle clone = new Rectangle(x, y, width, height);
        clone.setSelected(selected);
        clone.setFillColor(fillColor);
        clone.setBorderColor(borderColor);
        clone.setRotation(rotation);
        clone.setBorderRadius(borderRadius);
        return clone;
    }

    /**
     * Sets the fill color for this rectangle
     *
     * @param color The new fill color
     */
    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    /**
     * Sets the border color for this rectangle
     *
     * @param color The new border color
     */
    public void setBorderColor(Color color) {
        this.borderColor = color;
    }

    /**
     * Gets the fill color for this rectangle
     *
     * @return The current fill color
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Gets the border color for this rectangle
     *
     * @return The current border color
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the rotation angle for this rectangle
     *
     * @param degrees The rotation angle in degrees
     */
    @Override
    public void setRotation(double degrees) {
        this.rotation = degrees;
    }

    /**
     * Gets the rotation angle for this rectangle
     *
     * @return The current rotation angle in degrees
     */
    @Override
    public double getRotation() {
        return rotation;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Gets the border radius for this rectangle
     *
     * @return The current border radius
     */
    public int getBorderRadius() {
        return borderRadius;
    }

    /**
     * Sets the border radius for this rectangle
     *
     * @param radius The new border radius
     */
    public void setBorderRadius(int radius) {
        this.borderRadius = radius;
    }
}
