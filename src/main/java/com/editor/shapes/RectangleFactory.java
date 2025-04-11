package com.editor.shapes;

public class RectangleFactory implements ShapeFactory {
    @Override
    public Shape createShape(int x, int y) {
        return new Rectangle(x - 50, y - 30, 100, 60);
    }
}
