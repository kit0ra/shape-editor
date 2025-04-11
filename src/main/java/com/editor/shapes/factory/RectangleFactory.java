package com.editor.shapes.factory;

import com.editor.shapes.Rectangle;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeFactory;

public class RectangleFactory implements ShapeFactory {
    @Override
    public Shape createShape(int x, int y) {
        return new Rectangle(x - 50, y - 30, 100, 60);
    }
}
