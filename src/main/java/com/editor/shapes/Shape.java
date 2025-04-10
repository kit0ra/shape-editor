package com.editor.shapes;

import java.awt.Graphics;

public interface Shape {
    void draw(Graphics g);

    boolean isSelected(int x, int y);

    void setSelected(boolean selected);

    // Other shape-related methods
}
