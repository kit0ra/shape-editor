package com.editor.commands;

import com.editor.shapes.Shape;

/**
 * Command to move a shape on the whiteboard.
 * Implements the Command pattern to support undo/redo functionality.
 */
public class MoveShapeCommand implements Command {
    private final Shape shape;
    private final int startX, startY;
    private final int endX, endY;
    private final int deltaX, deltaY;

    /**
     * Creates a new MoveShapeCommand
     * 
     * @param shape The shape to move
     * @param startX The starting X position
     * @param startY The starting Y position
     * @param endX The ending X position
     * @param endY The ending Y position
     */
    public MoveShapeCommand(Shape shape, int startX, int startY, int endX, int endY) {
        this.shape = shape;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.deltaX = endX - startX;
        this.deltaY = endY - startY;
    }

    @Override
    public void execute() {
        // Move the shape to the end position
        shape.move(deltaX, deltaY);
    }

    @Override
    public void undo() {
        // Move the shape back to the start position
        shape.move(-deltaX, -deltaY);
    }
}
