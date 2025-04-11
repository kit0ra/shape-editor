package com.editor.commands;

import java.util.List;

import com.editor.shapes.Shape;

public class CreateShapeCommand implements Command {
    private final List<Shape> canvasShapes;
    private final Shape shape;
    private final int x, y;

    public CreateShapeCommand(List<Shape> canvasShapes, Shape shape, int x, int y) {
        this.canvasShapes = canvasShapes;
        this.shape = shape;
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute() {
        shape.move(x, y);
        canvasShapes.add(shape);
    }

    @Override
    public void undo() {
        canvasShapes.remove(shape);
    }
}
