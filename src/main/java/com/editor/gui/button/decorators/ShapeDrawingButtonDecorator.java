package com.editor.gui.button.decorators;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.editor.drawing.AWTDrawing;
import com.editor.drawing.Drawer;
import com.editor.gui.button.IButton;
import com.editor.shapes.Circle;
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;

/**
 * A decorator that draws a shape on a button.
 * This is used to create buttons that visually represent shapes,
 * especially for composite shapes in the toolbar.
 */
public class ShapeDrawingButtonDecorator extends ButtonDecorator {
    private final Shape shape;
    private final double scaleRatio;
    private final int padding;
    private BufferedImage shapeImage;

    /**
     * Creates a new ShapeDrawingButtonDecorator.
     *
     * @param decoratedButton The button to decorate
     * @param shape           The shape to draw on the button
     * @param scaleRatio      The ratio to scale the shape (1.0 = original size)
     * @param padding         The padding around the shape in pixels
     */
    public ShapeDrawingButtonDecorator(
            IButton decoratedButton,
            Shape shape,
            double scaleRatio,
            int padding) {
        super(decoratedButton);
        this.shape = shape;
        this.scaleRatio = scaleRatio;
        this.padding = padding;
        createShapeImage();
    }

    /**
     * Creates a buffered image of the shape to improve performance.
     */
    private void createShapeImage() {
        if (shape == null)
            return;

        
        int width = decoratedButton.getWidth() - (padding * 2);
        int height = decoratedButton.getHeight() - (padding * 2);

        if (width <= 0 || height <= 0)
            return;

        shapeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = shapeImage.createGraphics();

        try {
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, width, height);

            
            Drawer drawer = new AWTDrawing(g2d);

            
            g2d.scale(scaleRatio, scaleRatio);

            
            Shape clonedShape = shape.clone();

            
            com.editor.shapes.Rectangle bounds = clonedShape.getBounds();
            int centerX = (int) (width / (2 * scaleRatio));
            int centerY = (int) (height / (2 * scaleRatio));
            int shapeX = centerX - (bounds.getWidth() / 2);
            int shapeY = centerY - (bounds.getHeight() / 2);

            clonedShape.setPosition(shapeX, shapeY);

            
            
            System.out.println("Drawing shape on button with fill color: " + getFillColorName(clonedShape));
            clonedShape.draw(drawer);

        } finally {
            g2d.dispose();
        }
    }

    /**
     * Gets a string representation of the fill color of a shape.
     *
     * @param shape The shape to get the fill color from
     * @return A string representation of the fill color
     */
    private String getFillColorName(Shape shape) {
        Color fillColor = null;

        if (shape instanceof Rectangle) {
            fillColor = ((Rectangle) shape).getFillColor();
        } else if (shape instanceof RegularPolygon) {
            fillColor = ((RegularPolygon) shape).getFillColor();
        } else if (shape instanceof Circle) {
            fillColor = ((Circle) shape).getFillColor();
        } else if (shape instanceof ShapeGroup) {
            
            fillColor = Color.PINK;
        }

        if (fillColor != null) {
            return String.format("RGB(%d,%d,%d)", fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue());
        } else {
            return "unknown";
        }
    }

    @Override
    public void draw(Graphics g) {
        
        super.draw(g);

        
        if (shapeImage != null) {
            g.drawImage(shapeImage,
                    getX() + padding,
                    getY() + padding,
                    null);
        }
    }
}
