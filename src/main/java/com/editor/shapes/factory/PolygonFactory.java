package com.editor.shapes.factory;

import com.editor.shapes.PolygonShape;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeFactory;

public class PolygonFactory implements ShapeFactory {
    @Override
    public Shape createShape(int x, int y) {
        int[] xPoints = { x, x - 40, x + 40 };
        int[] yPoints = { y - 40, y + 40, y + 40 };
        return new PolygonShape(xPoints, yPoints, 3);
    }
}
