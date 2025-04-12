package com.editor.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;

public class AWTDrawing implements Drawer {
    private static final long serialVersionUID = 1L;
    private transient Graphics2D graphics;

    public AWTDrawing(Graphics2D graphics) {
        this.graphics = graphics;
    }

    @Override
    public void drawRectangle(int x, int y, int width, int height) {
        graphics.drawRect(x, y, width, height);
    }

    @Override
    public void drawRectangle(Rectangle rectangle) {
        // Save the current color and transform
        Color originalColor = graphics.getColor();
        java.awt.geom.AffineTransform oldTransform = graphics.getTransform();

        try {
            // Apply rotation if needed
            if (rectangle.getRotation() != 0) {
                // Calculate the center of the rectangle
                int centerX = rectangle.getX() + rectangle.getWidth() / 2;
                int centerY = rectangle.getY() + rectangle.getHeight() / 2;

                // Rotate around the center
                graphics.rotate(Math.toRadians(rectangle.getRotation()), centerX, centerY);
            }

            // Fill the rectangle
            graphics.setColor(rectangle.getFillColor());
            graphics.fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

            // Draw the outline
            graphics.setColor(rectangle.getBorderColor());
            graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        } finally {
            // Restore the original transform and color
            graphics.setTransform(oldTransform);
            graphics.setColor(originalColor);
        }
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        graphics.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawRegularPolygon(RegularPolygon regularPolygon) {
        // Save the current color and transform
        Color originalColor = graphics.getColor();
        java.awt.geom.AffineTransform oldTransform = graphics.getTransform();

        try {
            // Calculate the points of the regular polygon
            int sides = regularPolygon.getNumberOfSides();
            int[] xPoints = new int[sides];
            int[] yPoints = new int[sides];
            double angleStep = 2 * Math.PI / sides;
            double rotationRadians = Math.toRadians(regularPolygon.getRotation());

            // Apply rotation if needed
            if (regularPolygon.getRotation() != 0) {
                // Calculate the center of the polygon
                int centerX = regularPolygon.getX();
                int centerY = regularPolygon.getY();

                // Rotate around the center
                graphics.rotate(rotationRadians, centerX, centerY);
            }

            for (int i = 0; i < sides; i++) {
                double angle = i * angleStep;
                xPoints[i] = (int) (regularPolygon.getX() + regularPolygon.getRadius() * Math.cos(angle));
                yPoints[i] = (int) (regularPolygon.getY() + regularPolygon.getRadius() * Math.sin(angle));
            }

            // Create a Java AWT Polygon
            Polygon awtPolygon = new Polygon(xPoints, yPoints, sides);

            // Fill the polygon
            graphics.setColor(regularPolygon.getFillColor());
            graphics.fillPolygon(awtPolygon);

            // Draw the outline
            graphics.setColor(regularPolygon.getBorderColor());
            graphics.drawPolygon(awtPolygon);
        } finally {
            // Restore the original transform and color
            graphics.setTransform(oldTransform);
            graphics.setColor(originalColor);
        }
    }
}
