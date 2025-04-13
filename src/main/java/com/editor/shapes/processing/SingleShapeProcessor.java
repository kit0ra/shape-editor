package com.editor.shapes.processing;

import java.util.UUID;

import com.editor.gui.button.IButton;
import com.editor.gui.button.factory.ButtonFactory;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapePrototypeRegistry;

/**
 * Processor for single shapes.
 * Creates buttons for individual shapes.
 */
public class SingleShapeProcessor implements ShapeProcessor {
    private final ButtonFactory buttonFactory;
    private final ShapePrototypeRegistry registry;
    
    /**
     * Creates a new SingleShapeProcessor.
     *
     * @param buttonFactory The button factory to use for creating buttons
     * @param registry The shape prototype registry
     */
    public SingleShapeProcessor(ButtonFactory buttonFactory, ShapePrototypeRegistry registry) {
        this.buttonFactory = buttonFactory;
        this.registry = registry;
    }
    
    @Override
    public ProcessingResult processShape(Shape shape, int x, int y) {
        if (shape == null) {
            return null;
        }
        
        // Generate a unique key for the shape
        String uniqueShapeKey = generateUniqueKey(shape);
        
        // Clone the shape for the prototype
        Shape clonedShape = shape.clone();
        
        // Register the shape with the registry
        registry.registerPrototype(uniqueShapeKey, clonedShape);
        
        // Get the icon path for the shape
        String iconPath = getIconForShape(shape);
        
        // Create a button for the shape
        IButton button = buttonFactory.createButton(
                x, y, 
                iconPath, 
                "Create a " + shape.getClass().getSimpleName(), 
                uniqueShapeKey);
        
        return new ProcessingResult(button, uniqueShapeKey);
    }
    
    /**
     * Generates a unique key for a shape.
     *
     * @param shape The shape
     * @return A unique key
     */
    protected String generateUniqueKey(Shape shape) {
        String className = shape.getClass().getSimpleName();
        return className + "_" + UUID.randomUUID().toString();
    }
    
    /**
     * Gets the icon path for a shape.
     *
     * @param shape The shape
     * @return The icon path
     */
    protected String getIconForShape(Shape shape) {
        String className = shape.getClass().getSimpleName();
        String shapeTypeKey = className.equals("RegularPolygon") ? "Polygon" : className;
        return "icons/" + shapeTypeKey.toLowerCase() + ".png";
    }
}
