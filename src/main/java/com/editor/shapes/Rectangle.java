package com.editor.shapes;

import com.editor.drawing.Drawer;

public class Rectangle implements Shape {
    private int x, y, width, height;
    private boolean selected;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Drawer drawer) {
        drawer.drawRectangle(x, y, width, height);
    }

    @Override
    public void move(int x, int y) {
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
}
