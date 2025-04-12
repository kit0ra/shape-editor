package com.editor.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import com.editor.shapes.Circle;
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

            int borderRadius = rectangle.getBorderRadius();
            int x = rectangle.getX();
            int y = rectangle.getY();
            int width = rectangle.getWidth();
            int height = rectangle.getHeight();

            // If border radius is 0, draw a regular rectangle
            if (borderRadius == 0) {
                // Fill the rectangle
                graphics.setColor(rectangle.getFillColor());
                graphics.fillRect(x, y, width, height);

                // Draw the outline
                graphics.setColor(rectangle.getBorderColor());
                graphics.drawRect(x, y, width, height);
            } else {
                // Draw a rounded rectangle with the specified border radius
                // Fill the rounded rectangle
                graphics.setColor(rectangle.getFillColor());
                graphics.fillRoundRect(x, y, width, height, borderRadius, borderRadius);

                // Draw the outline
                graphics.setColor(rectangle.getBorderColor());
                graphics.drawRoundRect(x, y, width, height, borderRadius, borderRadius);
            }
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

    @Override
    public void drawCircle(Circle circle) {
        // Save the current color
        Color originalColor = graphics.getColor();
        java.awt.geom.AffineTransform oldTransform = graphics.getTransform();

        try {
            // Calculate the top-left corner of the bounding box
            int x = circle.getX() - circle.getRadius();
            int y = circle.getY() - circle.getRadius();
            int diameter = circle.getRadius() * 2;

            // Fill the circle
            graphics.setColor(circle.getFillColor());
            graphics.fillOval(x, y, diameter, diameter);

            // Draw the outline
            graphics.setColor(circle.getBorderColor());
            graphics.drawOval(x, y, diameter, diameter);

            // Draw selection indicator if selected
            if (circle.isSelected()) {
                graphics.setColor(Color.RED);
                int selectionMarkerSize = 6;

                // Draw selection markers at the cardinal points
                graphics.fillRect(circle.getX() - selectionMarkerSize / 2, y - selectionMarkerSize / 2,
                        selectionMarkerSize, selectionMarkerSize); // Top
                graphics.fillRect(circle.getX() - selectionMarkerSize / 2, y + diameter - selectionMarkerSize / 2,
                        selectionMarkerSize, selectionMarkerSize); // Bottom
                graphics.fillRect(x - selectionMarkerSize / 2, circle.getY() - selectionMarkerSize / 2,
                        selectionMarkerSize, selectionMarkerSize); // Left
                graphics.fillRect(x + diameter - selectionMarkerSize / 2, circle.getY() - selectionMarkerSize / 2,
                        selectionMarkerSize, selectionMarkerSize); // Right
            }
        } finally {
            // Restore the original transform and color
            graphics.setTransform(oldTransform);
            graphics.setColor(originalColor);
        }
    }
}
