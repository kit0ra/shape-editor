package com.editor.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        ShapeState(Color borderColor, Color fillColor, double rotation) {
            this.borderColor = borderColor;
            this.fillColor = fillColor;
            this.rotation = rotation;
        }
    }
    
    /**
     * Create a new edit command
     * 
     * @param shapes The shapes to edit
     * @param newBorderColor The new border color
     * @param newFillColor The new fill color
     * @param newRotation The new rotation angle in degrees
     */
    public EditShapeCommand(List<Shape> shapes, Color newBorderColor, Color newFillColor, double newRotation) {
        this.shapes = new ArrayList<>(shapes);
        
        // Store old states
        for (Shape shape : shapes) {
            Color oldBorderColor = getBorderColor(shape);
            Color oldFillColor = getFillColor(shape);
            double oldRotation = getRotation(shape);
            
            oldStates.put(shape, new ShapeState(oldBorderColor, oldFillColor, oldRotation));
            newStates.put(shape, new ShapeState(newBorderColor, newFillColor, newRotation));
        }
    }
    
    @Override
    public void execute() {
        for (Shape shape : shapes) {
            ShapeState newState = newStates.get(shape);
            
            // Apply new properties
            shape.setBorderColor(newState.borderColor);
            setFillColor(shape, newState.fillColor);
            setRotation(shape, newState.rotation);
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
        }
    }
    
    // Helper methods to get/set properties based on shape type
    
    private Color getBorderColor(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getBorderColor();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getBorderColor();
        }
        return Color.BLACK; // Default
    }
    
    private Color getFillColor(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getFillColor();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getFillColor();
        }
        return Color.WHITE; // Default
    }
    
    private double getRotation(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getRotation();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getRotation();
        }
        return 0.0; // Default
    }
    
    private void setFillColor(Shape shape, Color color) {
        if (shape instanceof Rectangle) {
            ((Rectangle) shape).setFillColor(color);
        } else if (shape instanceof RegularPolygon) {
            ((RegularPolygon) shape).setFillColor(color);
        }
    }
    
    private void setRotation(Shape shape, double rotation) {
        if (shape instanceof Rectangle) {
            ((Rectangle) shape).setRotation(rotation);
        } else if (shape instanceof RegularPolygon) {
            ((RegularPolygon) shape).setRotation(rotation);
        }
    }
}
