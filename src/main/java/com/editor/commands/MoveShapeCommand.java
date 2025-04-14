package com.editor.commands;

import com.editor.shapes.Shape;
import java.awt.Point;


public class MoveShapeCommand implements Command {
    private final Shape shape;
    private final Point startPosition;
    private final Point endPosition;

    /**
     * Creates a command to move a shape.
     * 
     * @param shape  The shape to move.
     * @param startX The starting X coordinate.
     * @param startY The starting Y coordinate.
     * @param endX   The ending X coordinate.
     * @param endY   The ending Y coordinate.
     */
    public MoveShapeCommand(Shape shape, int startX, int startY, int endX, int endY) {
        this.shape = shape;
        this.startPosition = new Point(startX, startY);
        this.endPosition = new Point(endX, endY);
    }

    /**
     * Creates a command to move a shape using Point objects.
     * 
     * @param shape         The shape to move.
     * @param startPosition The starting position.
     * @param endPosition   The ending position.
     */
    public MoveShapeCommand(Shape shape, Point startPosition, Point endPosition) {
        this.shape = shape;
        this.startPosition = new Point(startPosition); 
        this.endPosition = new Point(endPosition); 
    }

    @Override
    public void execute() {
        
        
        shape.setPosition(endPosition.x, endPosition.y);
    }

    @Override
    public void undo() {
        
        shape.setPosition(startPosition.x, startPosition.y);
    }

    @Override
    public String toString() {
        return "MoveShapeCommand{" +
                "shape=" + shape.getClass().getSimpleName() + 
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                '}';
    }
}
