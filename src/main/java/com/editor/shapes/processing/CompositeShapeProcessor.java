package com.editor.shapes.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.editor.gui.button.IButton;
import com.editor.gui.button.factory.ButtonFactory;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;

/**
 * Processor for composite shapes (groups of shapes).
 * Creates buttons for shape groups.
 */
public class CompositeShapeProcessor implements ShapeProcessor {
    private final ButtonFactory buttonFactory;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    
    /**
     * Creates a new CompositeShapeProcessor.
     *
     * @param buttonFactory The button factory to use for creating buttons
     * @param compositeRegistry The composite shape prototype registry
     */
    public CompositeShapeProcessor(ButtonFactory buttonFactory, CompositeShapePrototypeRegistry compositeRegistry) {
        this.buttonFactory = buttonFactory;
        this.compositeRegistry = compositeRegistry;
    }
    
    @Override
    public ProcessingResult processShape(Shape shape, int x, int y) {
        if (shape == null) {
            return null;
        }
        
        // Handle single shape as a group with one shape
        List<Shape> shapesToGroup = new ArrayList<>();
        
        if (shape instanceof ShapeGroup) {
            // If it's already a group, clone its shapes
            ShapeGroup group = (ShapeGroup) shape;
            for (Shape s : group.getShapes()) {
                shapesToGroup.add(s.clone());
            }
        } else {
            // If it's a single shape, just add it
            shapesToGroup.add(shape.clone());
        }
        
        if (shapesToGroup.isEmpty()) {
            return null;
        }
        
        // Create a group prototype
        ShapeGroup groupPrototype = new ShapeGroup(shapesToGroup);
        
        // Generate a unique key for the group
        String groupKey = generateUniqueKey(groupPrototype);
        
        // Register the group with the registry
        compositeRegistry.registerPrototype(groupKey, groupPrototype);
        
        // Get the icon path for the group
        String iconPath = getIconForShape(groupPrototype);
        
        // Create a button for the group
        IButton button = buttonFactory.createButton(
                x, y, 
                iconPath, 
                "Create composite (" + shapesToGroup.size() + " shapes)", 
                groupKey);
        
        return new ProcessingResult(button, groupKey);
    }
    
    /**
     * Processes multiple shapes and creates a button for them.
     *
     * @param shapes The shapes to process
     * @param x The x-coordinate for the button
     * @param y The y-coordinate for the button
     * @return A ProcessingResult containing the created button and prototype key
     */
    public ProcessingResult processShapes(List<Shape> shapes, int x, int y) {
        if (shapes == null || shapes.isEmpty()) {
            return null;
        }
        
        // Clone all shapes
        List<Shape> shapesToGroup = new ArrayList<>();
        for (Shape s : shapes) {
            shapesToGroup.add(s.clone());
        }
        
        // Create a group prototype
        ShapeGroup groupPrototype = new ShapeGroup(shapesToGroup);
        
        // Generate a unique key for the group
        String groupKey = generateUniqueKey(groupPrototype);
        
        // Register the group with the registry
        compositeRegistry.registerPrototype(groupKey, groupPrototype);
        
        // Get the icon path for the group
        String iconPath = getIconForShape(groupPrototype);
        
        // Create a button for the group
        IButton button = buttonFactory.createButton(
                x, y, 
                iconPath, 
                "Create composite (" + shapesToGroup.size() + " shapes)", 
                groupKey);
        
        return new ProcessingResult(button, groupKey);
    }
    
    /**
     * Generates a unique key for a shape group.
     *
     * @param group The shape group
     * @return A unique key
     */
    protected String generateUniqueKey(ShapeGroup group) {
        return "composite_" + UUID.randomUUID().toString();
    }
    
    /**
     * Gets the icon path for a shape group.
     *
     * @param group The shape group
     * @return The icon path
     */
    protected String getIconForShape(ShapeGroup group) {
        return "icons/group.png";
    }
}
