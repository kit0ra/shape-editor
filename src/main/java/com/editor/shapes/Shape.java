package com.editor.shapes;

import java.io.Serializable;

import com.editor.drawing.Drawer;

public interface Shape extends Cloneable, Serializable {
    void draw(Drawer drawer);

    void move(int dx, int dy); // Moves the shape by a delta

    void setPosition(int x, int y); // Sets the absolute position of the shape

    boolean isSelected(int x, int y); // Check if point (x,y) is within the shape

    void setSelected(boolean selected); // Set selection state

    boolean isSelected(); // Get current selection state

    Rectangle getBounds(); // Get the bounding rectangle of the shape

    // Color methods
    void setBorderColor(java.awt.Color color); // Set the border color

    // Rotation methods
    void setRotation(double degrees); // Set rotation in degrees

    double getRotation(); // Get current rotation in degrees

    /**
     * Creates a deep clone of this shape.
     *
     * @return A new instance of the shape with the same properties
     */
    Shape clone();
}
