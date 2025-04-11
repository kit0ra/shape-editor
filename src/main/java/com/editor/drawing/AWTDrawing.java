package com.editor.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;

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
    public void drawRectangle(Rectangle rectangle) {
        // Save the current color
        Color originalColor = graphics.getColor();

        // Fill the rectangle
        graphics.setColor(rectangle.getFillColor());
        graphics.fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

        // Draw the outline
        graphics.setColor(rectangle.getBorderColor());
        graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

        // Restore the original color
        graphics.setColor(originalColor);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        graphics.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawRegularPolygon(RegularPolygon regularPolygon) {
        // Save the current color
        Color originalColor = graphics.getColor();

        // Calculate the points of the regular polygon
        int sides = regularPolygon.getNumberOfSides();
        int[] xPoints = new int[sides];
        int[] yPoints = new int[sides];
        double angleStep = 2 * Math.PI / sides;

        for (int i = 0; i < sides; i++) {
            xPoints[i] = (int) (regularPolygon.getX() + regularPolygon.getRadius() * Math.cos(i * angleStep));
            yPoints[i] = (int) (regularPolygon.getY() + regularPolygon.getRadius() * Math.sin(i * angleStep));
        }

        // Create a Java AWT Polygon
        Polygon awtPolygon = new Polygon(xPoints, yPoints, sides);

        // Fill the polygon
        graphics.setColor(regularPolygon.getFillColor());
        graphics.fillPolygon(awtPolygon);

        // Draw the outline
        graphics.setColor(regularPolygon.getBorderColor());
        graphics.drawPolygon(awtPolygon);

        // Restore the original color
        graphics.setColor(originalColor);
    }
}
