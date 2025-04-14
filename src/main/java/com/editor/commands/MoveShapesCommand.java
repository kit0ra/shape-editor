package com.editor.commands;

import com.editor.shapes.Shape;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command to move multiple shapes at once.
 * Stores the original and final positions of each shape to support undo/redo.
 */
public class MoveShapesCommand implements Command {
    private final List<Shape> shapes;
    private final Map<Shape, Point> startPositions;
    private final Map<Shape, Point> endPositions;

    /**
     * Creates a command to move multiple shapes.
     * 
     * @param shapes The list of shapes to move
     * @param startPositions Map of shapes to their starting positions
     * @param endPositions Map of shapes to their ending positions
     */
    public MoveShapesCommand(List<Shape> shapes, Map<Shape, Point> startPositions, Map<Shape, Point> endPositions) {
        this.shapes = shapes;
        
        
        this.startPositions = new HashMap<>();
        for (Map.Entry<Shape, Point> entry : startPositions.entrySet()) {
            this.startPositions.put(entry.getKey(), new Point(entry.getValue()));
        }
        
        this.endPositions = new HashMap<>();
        for (Map.Entry<Shape, Point> entry : endPositions.entrySet()) {
            this.endPositions.put(entry.getKey(), new Point(entry.getValue()));
        }
    }

    @Override
    public void execute() {
        
        for (Shape shape : shapes) {
            Point endPos = endPositions.get(shape);
            if (endPos != null) {
                shape.setPosition(endPos.x, endPos.y);
            }
        }
    }

    @Override
    public void undo() {
        
        for (Shape shape : shapes) {
            Point startPos = startPositions.get(shape);
            if (startPos != null) {
                shape.setPosition(startPos.x, startPos.y);
            }
        }
    }

    @Override
    public String toString() {
        return "MoveShapesCommand{" +
                "shapes=" + shapes.size() + " shapes" +
                '}';
    }
}
