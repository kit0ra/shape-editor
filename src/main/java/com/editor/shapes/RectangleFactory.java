package com.editor.shapes;

/**
 * Factory for creating rectangle shapes.
 */
public class RectangleFactory implements ShapeFactory {
    @Override
    public Shape createShape(int x, int y) {
        // Create a rectangle centered at (x, y)
        int width = 80;
        int height = 60;
        return new Rectangle(x - width/2, y - height/2, width, height);
    }
}
