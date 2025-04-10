package com.editor.drawing;

public interface Drawer {
    void drawRectangle(int x, int y, int width, int height);

    void drawPolygon(int[] xPoints, int[] yPoints, int nPoints);
}
