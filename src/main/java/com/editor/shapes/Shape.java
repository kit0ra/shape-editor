package com.editor.shapes;

import com.editor.drawing.Drawer;

public interface Shape extends Cloneable {
    void draw(Drawer drawer);

    void move(int dx, int dy); // Moves the shape by a delta

    void setPosition(int x, int y); // Sets the absolute position of the shape

    boolean isSelected(int x, int y); // Check if point (x,y) is within the shape

    void setSelected(boolean selected); // Set selection state

    boolean isSelected(); // Get current selection state

    Rectangle getBounds(); // Get the bounding rectangle of the shape

    /**
     * Creates a deep clone of this shape.
     *
     * @return A new instance of the shape with the same properties
     */
    Shape clone();
}
