package com.editor.drawing;

import java.awt.Graphics2D;

public class AWTDrawing implements Drawer {
    private Graphics2D graphics;

    public AWTDrawing(Graphics2D graphics) {
        this.graphics = graphics;
    }

    @Override
    public void drawRectangle(int x, int y, int width, int height) {
        graphics.drawRect(x, y, width, height);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        graphics.drawPolygon(xPoints, yPoints, nPoints);
    }
}
