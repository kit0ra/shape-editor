package com.editor.commands;

import java.util.List;

import com.editor.shapes.Shape;

public class DeleteShapeCommand implements Command {
    private final List<Shape> canvasShapes;
    private final Shape shape;
    private int index;

    public DeleteShapeCommand(List<Shape> canvasShapes, Shape shape) {
        this.canvasShapes = canvasShapes;
        this.shape = shape;
        this.index = canvasShapes.indexOf(shape);
    }

    @Override
    public void execute() {
        if (index != -1) {
            canvasShapes.remove(shape);
        }
    }

    @Override
    public void undo() {
        if (index != -1) {
            canvasShapes.add(index, shape);
        }
    }
}
