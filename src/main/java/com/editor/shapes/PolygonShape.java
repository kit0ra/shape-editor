package com.editor.shapes;

import java.awt.Polygon;

import com.editor.drawing.Drawer;

public class PolygonShape implements Shape {
    private int[] xPoints, yPoints;
    private int nPoints;
    private boolean selected;

    public PolygonShape(int[] xPoints, int[] yPoints, int nPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.nPoints = nPoints;
    }

    @Override
    public void draw(Drawer drawer) {
        drawer.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void move(int x, int y) {
        for (int i = 0; i < nPoints; i++) {
            xPoints[i] += x;
            yPoints[i] += y;
        }
    }

    @Override
    public boolean isSelected(int x, int y) {
        Polygon poly = new Polygon();
        for (int i = 0; i < nPoints; i++) {
            poly.addPoint(xPoints[i], yPoints[i]);
        }
        return poly.contains(x, y);
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }
}
