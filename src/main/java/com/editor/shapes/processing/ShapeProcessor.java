package com.editor.shapes.processing;

import com.editor.shapes.Shape;

/**
 * Interface for processors that process shapes and create buttons for them.
 */
public interface ShapeProcessor {
    /**
     * Processes a shape and creates a button for it.
     *
     * @param shape The shape to process
     * @param x The x-coordinate for the button
     * @param y The y-coordinate for the button
     * @return A ProcessingResult containing the created button and prototype key
     */
    ProcessingResult processShape(Shape shape, int x, int y);
}
