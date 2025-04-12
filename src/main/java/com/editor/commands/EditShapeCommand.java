package com.editor.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.editor.shapes.Circle;
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;
import com.editor.shapes.Shape;

/**
 * Command to edit shape properties (colors, rotation)
 */
public class EditShapeCommand implements Command {
    private List<Shape> shapes;
    private Map<Shape, ShapeState> oldStates = new HashMap<>();
    private Map<Shape, ShapeState> newStates = new HashMap<>();

    /**
     * Class to store shape state
     */
    private static class ShapeState {
        Color borderColor;
        Color fillColor;
        double rotation;
        int borderRadius;

        ShapeState(Color borderColor, Color fillColor, double rotation, int borderRadius) {
            this.borderColor = borderColor;
            this.fillColor = fillColor;
            this.rotation = rotation;
            this.borderRadius = borderRadius;
        }
    }

    /**
     * Create a new edit command
     *
     * @param shapes          The shapes to edit
     * @param newBorderColor  The new border color
     * @param newFillColor    The new fill color
     * @param newRotation     The new rotation angle in degrees
     * @param newBorderRadius The new border radius (for rectangles)
     */
    public EditShapeCommand(List<Shape> shapes, Color newBorderColor, Color newFillColor, double newRotation,
            int newBorderRadius) {
        this.shapes = new ArrayList<>(shapes);

        // Store old states
        for (Shape shape : shapes) {
            Color oldBorderColor = getBorderColor(shape);
            Color oldFillColor = getFillColor(shape);
            double oldRotation = getRotation(shape);
            int oldBorderRadius = getBorderRadius(shape);

            oldStates.put(shape, new ShapeState(oldBorderColor, oldFillColor, oldRotation, oldBorderRadius));
            newStates.put(shape, new ShapeState(newBorderColor, newFillColor, newRotation, newBorderRadius));
        }
    }

    /**
     * Create a new edit command (backward compatibility)
     *
     * @param shapes         The shapes to edit
     * @param newBorderColor The new border color
     * @param newFillColor   The new fill color
     * @param newRotation    The new rotation angle in degrees
     */
    public EditShapeCommand(List<Shape> shapes, Color newBorderColor, Color newFillColor, double newRotation) {
        this(shapes, newBorderColor, newFillColor, newRotation, 0);
    }

    @Override
    public void execute() {
        for (Shape shape : shapes) {
            ShapeState newState = newStates.get(shape);

            // Apply new properties
            shape.setBorderColor(newState.borderColor);
            setFillColor(shape, newState.fillColor);
            setRotation(shape, newState.rotation);
            setBorderRadius(shape, newState.borderRadius);
        }
    }

    @Override
    public void undo() {
        for (Shape shape : shapes) {
            ShapeState oldState = oldStates.get(shape);

            // Restore old properties
            shape.setBorderColor(oldState.borderColor);
            setFillColor(shape, oldState.fillColor);
            setRotation(shape, oldState.rotation);
            setBorderRadius(shape, oldState.borderRadius);
        }
    }

    // Helper methods to get/set properties based on shape type

    private Color getBorderColor(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getBorderColor();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getBorderColor();
        } else if (shape instanceof Circle) {
            return ((Circle) shape).getBorderColor();
        }
        return Color.BLACK; // Default
    }

    private Color getFillColor(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getFillColor();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getFillColor();
        } else if (shape instanceof Circle) {
            return ((Circle) shape).getFillColor();
        }
        return Color.WHITE; // Default
    }

    private double getRotation(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getRotation();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getRotation();
        } else if (shape instanceof Circle) {
            return ((Circle) shape).getRotation();
        }
        return 0.0; // Default
    }

    private void setFillColor(Shape shape, Color color) {
        if (shape instanceof Rectangle) {
            ((Rectangle) shape).setFillColor(color);
        } else if (shape instanceof RegularPolygon) {
            ((RegularPolygon) shape).setFillColor(color);
        } else if (shape instanceof Circle) {
            ((Circle) shape).setFillColor(color);
        }
    }

    private void setRotation(Shape shape, double rotation) {
        if (shape instanceof Rectangle) {
            ((Rectangle) shape).setRotation(rotation);
        } else if (shape instanceof RegularPolygon) {
            ((RegularPolygon) shape).setRotation(rotation);
        } else if (shape instanceof Circle) {
            ((Circle) shape).setRotation(rotation);
        }
    }

    private int getBorderRadius(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getBorderRadius();
        }
        return 0; // Default
    }

    private void setBorderRadius(Shape shape, int borderRadius) {
        if (shape instanceof Rectangle) {
            ((Rectangle) shape).setBorderRadius(borderRadius);
        }
        // Other shapes don't have border radius
    }
}
