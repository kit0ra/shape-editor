package com.editor.shapes;

import com.editor.drawing.Drawer;

public interface Shape {
    void draw(Drawer drawer);

    void move(int x, int y);

    boolean isSelected(int x, int y); // Check if point (x,y) is within the shape

    void setSelected(boolean selected); // Set selection state

    boolean isSelected(); // Get current selection state

}
