package com.editor.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import com.editor.shapes.Circle;
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;

public class AWTDrawing implements Drawer {
    private static final long serialVersionUID = 1L;
    private final transient Graphics2D graphics;

    public AWTDrawing(Graphics2D graphics) {
        this.graphics = graphics;
    }

    @Override
    public void drawRectangle(int x, int y, int width, int height) {
        graphics.drawRect(x, y, width, height);
    }

    @Override
    public void drawRectangle(Rectangle rectangle) {
        Color originalColor = graphics.getColor();
        java.awt.geom.AffineTransform oldTransform = graphics.getTransform();

        try {
            if (rectangle.getRotation() != 0) {
                int centerX = rectangle.getX() + rectangle.getWidth() / 2;
                int centerY = rectangle.getY() + rectangle.getHeight() / 2;
                graphics.rotate(Math.toRadians(rectangle.getRotation()), centerX, centerY);
            }

            int borderRadius = rectangle.getBorderRadius();
            int x = rectangle.getX();
            int y = rectangle.getY();
            int width = rectangle.getWidth();
            int height = rectangle.getHeight();

            if (borderRadius == 0) {
                graphics.setColor(rectangle.getFillColor());
                graphics.fillRect(x, y, width, height);

                graphics.setColor(rectangle.getBorderColor());
                graphics.drawRect(x, y, width, height);
            } else {
                graphics.setColor(rectangle.getFillColor());
                graphics.fillRoundRect(x, y, width, height, borderRadius, borderRadius);

                graphics.setColor(rectangle.getBorderColor());
                graphics.drawRoundRect(x, y, width, height, borderRadius, borderRadius);
            }
        } finally {
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
        Color originalColor = graphics.getColor();
        java.awt.geom.AffineTransform oldTransform = graphics.getTransform();

        try {
            int sides = regularPolygon.getNumberOfSides();
            int[] xPoints = new int[sides];
            int[] yPoints = new int[sides];
            double angleStep = 2 * Math.PI / sides;
            double rotationRadians = Math.toRadians(regularPolygon.getRotation());

            if (regularPolygon.getRotation() != 0) {
                int centerX = regularPolygon.getX();
                int centerY = regularPolygon.getY();
                graphics.rotate(rotationRadians, centerX, centerY);
            }

            for (int i = 0; i < sides; i++) {
                double angle = i * angleStep;
                xPoints[i] = (int) (regularPolygon.getX() + regularPolygon.getRadius() * Math.cos(angle));
                yPoints[i] = (int) (regularPolygon.getY() + regularPolygon.getRadius() * Math.sin(angle));
            }

            Polygon awtPolygon = new Polygon(xPoints, yPoints, sides);

            graphics.setColor(regularPolygon.getFillColor());
            graphics.fillPolygon(awtPolygon);

            graphics.setColor(regularPolygon.getBorderColor());
            graphics.drawPolygon(awtPolygon);
        } finally {
            graphics.setTransform(oldTransform);
            graphics.setColor(originalColor);
        }
    }

    @Override
    public void drawCircle(Circle circle) {
        Color originalColor = graphics.getColor();
        java.awt.geom.AffineTransform oldTransform = graphics.getTransform();

        try {
            int x = circle.getX() - circle.getRadius();
            int y = circle.getY() - circle.getRadius();
            int diameter = circle.getRadius() * 2;

            graphics.setColor(circle.getFillColor());
            graphics.fillOval(x, y, diameter, diameter);

            graphics.setColor(circle.getBorderColor());
            graphics.drawOval(x, y, diameter, diameter);

            if (circle.isSelected()) {
                graphics.setColor(Color.RED);
                int selectionMarkerSize = 6;

                graphics.fillRect(circle.getX() - selectionMarkerSize / 2, y - selectionMarkerSize / 2,
                        selectionMarkerSize, selectionMarkerSize);
                graphics.fillRect(circle.getX() - selectionMarkerSize / 2, y + diameter - selectionMarkerSize / 2,
                        selectionMarkerSize, selectionMarkerSize);
                graphics.fillRect(x - selectionMarkerSize / 2, circle.getY() - selectionMarkerSize / 2,
                        selectionMarkerSize, selectionMarkerSize);
                graphics.fillRect(x + diameter - selectionMarkerSize / 2, circle.getY() - selectionMarkerSize / 2,
                        selectionMarkerSize, selectionMarkerSize);
            }
        } finally {
            graphics.setTransform(oldTransform);
            graphics.setColor(originalColor);
        }
    }
}
