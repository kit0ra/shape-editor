package com.editor.shapes;

import java.io.Serializable;

import com.editor.drawing.Drawer;

public interface Shape extends Cloneable, Serializable {
    void draw(Drawer drawer);

    void move(int dx, int dy); 

    void setPosition(int x, int y); 

    boolean isSelected(int x, int y); 

    void setSelected(boolean selected); 

    boolean isSelected(); 

    Rectangle getBounds(); 

    
    void setBorderColor(java.awt.Color color); 

    
    void setRotation(double degrees); 

    double getRotation(); 

    /**
     * Creates a deep clone of this shape.
     *
     * @return A new instance of the shape with the same properties
     */
    Shape clone();
}
