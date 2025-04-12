package com.editor.shapes;

import java.awt.Color;

import com.editor.drawing.Drawer;

public class Rectangle implements Shape {
    private static final long serialVersionUID = 1L;
    private int x, y, width, height;
    private boolean selected;
    private Color fillColor = Color.BLUE; // Default fill color for rectangles
    private Color borderColor = Color.BLACK; // Default border color

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
        // Create a new Rectangle with the same properties
        Rectangle clone = new Rectangle(x, y, width, height);
        clone.setSelected(selected);
        clone.setFillColor(fillColor);
        clone.setBorderColor(borderColor);
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
}
