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
     * @param registry      The shape prototype registry
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

        
        String uniqueShapeKey = generateUniqueKey(shape);

        
        Shape clonedShape = shape.clone();

        
        registry.registerPrototype(uniqueShapeKey, clonedShape);

        
        String iconPath = getIconForShape(shape);

        
        
        
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
