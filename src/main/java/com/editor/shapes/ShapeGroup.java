package com.editor.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.editor.drawing.Drawer;

/**
 * Represents a group of shapes that can be manipulated as a single entity.
 */
public class ShapeGroup implements Shape {
    private static final long serialVersionUID = 1L;
    private List<Shape> shapes = new ArrayList<>();
    private boolean selected;
    private Color borderColor = Color.BLACK;

    /**
     * Creates a new empty shape group.
     */
    public ShapeGroup() {
    }

    /**
     * Creates a new shape group with the given shapes.
     *
     * @param shapes The shapes to include in the group
     */
    public ShapeGroup(List<Shape> shapes) {
        this.shapes.addAll(shapes);
    }

    /**
     * Adds a shape to the group.
     *
     * @param shape The shape to add
     */
    public void addShape(Shape shape) {
        shapes.add(shape);
    }

    /**
     * Removes a shape from the group.
     *
     * @param shape The shape to remove
     * @return true if the shape was removed, false otherwise
     */
    public boolean removeShape(Shape shape) {
        return shapes.remove(shape);
    }

    /**
     * Gets the list of shapes in this group.
     *
     * @return The list of shapes
     */
    public List<Shape> getShapes() {
        return new ArrayList<>(shapes); // Return a copy to prevent external modification
    }

    @Override
    public void draw(Drawer drawer) {
        // Draw all shapes in the group
        for (Shape shape : shapes) {
            shape.draw(drawer);
        }
    }

    @Override
    public void move(int dx, int dy) {
        // Move all shapes in the group
        for (Shape shape : shapes) {
            shape.move(dx, dy);
        }
    }

    @Override
    public void setPosition(int x, int y) {
        // Calculate the offset from the current position
        Rectangle bounds = getBounds();
        int dx = x - bounds.getX();
        int dy = y - bounds.getY();

        // Move all shapes by this offset
        move(dx, dy);
    }

    @Override
    public boolean isSelected(int x, int y) {
        // Check if the point is within any shape in the group
        for (Shape shape : shapes) {
            if (shape.isSelected(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;

        // Set the selection state of all shapes in the group
        for (Shape shape : shapes) {
            shape.setSelected(selected);
        }
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public Rectangle getBounds() {
        if (shapes.isEmpty()) {
            return new Rectangle(0, 0, 0, 0);
        }

        // Start with the bounds of the first shape
        Rectangle bounds = shapes.get(0).getBounds();
        int minX = bounds.getX();
        int minY = bounds.getY();
        int maxX = minX + bounds.getWidth();
        int maxY = minY + bounds.getHeight();

        // Expand to include all other shapes
        for (int i = 1; i < shapes.size(); i++) {
            Rectangle shapeBounds = shapes.get(i).getBounds();
            minX = Math.min(minX, shapeBounds.getX());
            minY = Math.min(minY, shapeBounds.getY());
            maxX = Math.max(maxX, shapeBounds.getX() + shapeBounds.getWidth());
            maxY = Math.max(maxY, shapeBounds.getY() + shapeBounds.getHeight());
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public Shape clone() {
        try {
            // Call Object.clone() to create the initial shallow copy
            ShapeGroup clone = (ShapeGroup) super.clone();

            // Initialize a new list for the cloned shapes
            clone.shapes = new ArrayList<>();

            // Deep clone the shapes in the group
            for (Shape shape : shapes) {
                clone.addShape(shape.clone());
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new InternalError("Could not clone ShapeGroup", e);
        }
    }

    /**
     * Sets the border color for this group.
     *
     * @param color The new border color
     */
    public void setBorderColor(Color color) {
        this.borderColor = color;
    }

    /**
     * Gets the border color for this group.
     *
     * @return The current border color
     */
    public Color getBorderColor() {
        return borderColor;
    }
}
