package com.editor.shapes;

/**
 * Factory for creating polygon shapes.
 */
public class PolygonFactory implements ShapeFactory {
    @Override
    public Shape createShape(int x, int y) {
        // Create a simple triangle centered at (x, y)
        int[] xPoints = {x, x - 40, x + 40};
        int[] yPoints = {y - 40, y + 40, y + 40};
        return new PolygonShape(xPoints, yPoints, 3);
    }
}
