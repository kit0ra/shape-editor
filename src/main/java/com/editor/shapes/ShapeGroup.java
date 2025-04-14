package com.editor.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.editor.drawing.Drawer; 

/**
 * Represents a group of shapes that can be manipulated as a single entity.
 * Implements Serializable to allow saving/loading state.
 */
public class ShapeGroup implements Shape { 
    private static final long serialVersionUID = 1L; 
    private List<Shape> shapes = new ArrayList<>();
    private boolean selected;
    private Color borderColor = Color.BLACK;
    private double rotation = 0.0; 

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
        return new ArrayList<>(shapes); 
    }

    @Override
    public void draw(Drawer drawer) {
        
        for (Shape shape : shapes) {
            shape.draw(drawer);
        }
    }

    @Override
    public void move(int dx, int dy) {
        
        for (Shape shape : shapes) {
            shape.move(dx, dy);
        }
    }

    @Override
    public void setPosition(int x, int y) {
        
        Rectangle bounds = getBounds();
        int dx = x - bounds.getX();
        int dy = y - bounds.getY();

        
        move(dx, dy);
    }

    @Override
    public boolean isSelected(int x, int y) {
        
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

        
        Rectangle bounds = shapes.get(0).getBounds();
        int minX = bounds.getX();
        int minY = bounds.getY();
        int maxX = minX + bounds.getWidth();
        int maxY = minY + bounds.getHeight();

        
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
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public Shape clone() {
        try {
            
            ShapeGroup clone = (ShapeGroup) super.clone();

            
            clone.shapes = new ArrayList<>();

            
            for (Shape shape : shapes) {
                clone.addShape(shape.clone());
            }

            
            clone.rotation = this.rotation;

            return clone;
        } catch (CloneNotSupportedException e) {
            
            throw new InternalError("Could not clone ShapeGroup", e);
        }
    }

    /**
     * Sets the border color for this group.
     *
     * @param color The new border color
     */
    @Override
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

    /**
     * Sets the rotation angle for this group
     * This will rotate all shapes in the group around their own centers
     *
     * @param degrees The rotation angle in degrees
     */
    @Override
    public void setRotation(double degrees) {
        this.rotation = degrees;
        
        for (Shape shape : shapes) {
            shape.setRotation(degrees);
        }
    }

    /**
     * Gets the rotation angle for this group
     *
     * @return The current rotation angle in degrees
     */
    @Override
    public double getRotation() {
        return rotation;
    }
}
