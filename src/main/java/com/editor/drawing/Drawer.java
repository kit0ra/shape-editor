package com.editor.drawing;

import java.io.Serializable;

import com.editor.shapes.Circle;
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;

public interface Drawer extends Serializable {
    /**
     * Draws a rectangle with the specified parameters
     */
    void drawRectangle(int x, int y, int width, int height);

    /**
     * Draws a polygon with the specified parameters
     */
    void drawPolygon(int[] xPoints, int[] yPoints, int nPoints);

    /**
     * Draws a rectangle object with fill and border colors
     *
     * @param rectangle The rectangle to draw
     */
    void drawRectangle(Rectangle rectangle);

    /**
     * Draws a regular polygon object with fill and border colors
     *
     * @param regularPolygon The regular polygon to draw
     */
    void drawRegularPolygon(RegularPolygon regularPolygon);

    /**
     * Draws a circle object with fill and border colors
     *
     * @param circle The circle to draw
     */
    void drawCircle(Circle circle);
}
