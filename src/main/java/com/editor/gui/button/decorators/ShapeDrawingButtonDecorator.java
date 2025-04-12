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
import com.editor.shapes.Shape;

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
     * @param shape The shape to draw on the button
     * @param scaleRatio The ratio to scale the shape (1.0 = original size)
     * @param padding The padding around the shape in pixels
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
        if (shape == null) return;
        
        // Create a buffered image to draw the shape
        int width = decoratedButton.getWidth() - (padding * 2);
        int height = decoratedButton.getHeight() - (padding * 2);
        
        if (width <= 0 || height <= 0) return;
        
        shapeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = shapeImage.createGraphics();
        
        try {
            // Set up the graphics context
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
            // Clear the background (transparent)
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, width, height);
            
            // Create a drawer for the shape
            Drawer drawer = new AWTDrawing(g2d);
            
            // Scale the graphics context to fit the shape
            g2d.scale(scaleRatio, scaleRatio);
            
            // Draw the shape
            Shape clonedShape = shape.clone();
            
            // Center the shape in the button
            com.editor.shapes.Rectangle bounds = clonedShape.getBounds();
            int centerX = (int)(width / (2 * scaleRatio));
            int centerY = (int)(height / (2 * scaleRatio));
            int shapeX = centerX - (bounds.getWidth() / 2);
            int shapeY = centerY - (bounds.getHeight() / 2);
            
            clonedShape.setPosition(shapeX, shapeY);
            clonedShape.draw(drawer);
            
        } finally {
            g2d.dispose();
        }
    }

    @Override
    public void draw(Graphics g) {
        // Draw the base button
        super.draw(g);
        
        // Draw the shape image if available
        if (shapeImage != null) {
            g.drawImage(shapeImage, 
                    getX() + padding, 
                    getY() + padding, 
                    null);
        }
    }
}
