package com.editor.shapes;

import java.awt.Graphics;
import java.awt.Polygon;

public class PolygonShape implements Shape {
    private int[] xPoints, yPoints;
    private int nPoints;

    public PolygonShape(int[] xPoints, int[] yPoints, int nPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.nPoints = nPoints;
    }

    @Override
    public void draw(Graphics g) {
        g.drawPolygon(new Polygon(xPoints, yPoints, nPoints));
    }

    @Override
    public boolean isSelected(int x, int y) {
        // Implement selection logic for the polygon shape
        return false;
    }

    @Override
    public void setSelected(boolean selected) {
        // Implement selection logic if needed
    }
}
