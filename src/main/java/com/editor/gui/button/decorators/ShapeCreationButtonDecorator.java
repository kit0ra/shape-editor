package com.editor.gui.button.decorators;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.button.IButton;
import com.editor.shapes.ShapePrototypeRegistry;

/**
 * A decorator that adds drag-and-drop shape creation functionality to a button.
 * This decorator implements the Draggable interface to allow buttons to be
 * dragged onto the whiteboard to create shapes.
 */
public class ShapeCreationButtonDecorator extends ButtonDecorator implements Draggable {
    private final WhiteBoard targetWhiteBoard;
    private final ShapePrototypeRegistry prototypeRegistry;
    private final String shapeType;
    private boolean isDragging = false;
    private int dragX, dragY;

    /**
     * Creates a new ShapeCreationButtonDecorator
     *
     * @param decoratedButton   The button to decorate
     * @param targetWhiteBoard  The whiteboard where shapes will be created
     * @param prototypeRegistry The registry containing shape prototypes
     * @param shapeType         A string identifier for the type of shape
     */
    public ShapeCreationButtonDecorator(
            IButton decoratedButton,
            WhiteBoard targetWhiteBoard,
            ShapePrototypeRegistry prototypeRegistry,
            String shapeType) {
        super(decoratedButton);
        this.targetWhiteBoard = targetWhiteBoard;
        this.prototypeRegistry = prototypeRegistry;
        this.shapeType = shapeType;
    }

    @Override
    public void draw(Graphics g) {
        // Draw the base button
        super.draw(g);

        // If dragging, draw a visual indicator
        if (isDragging) {
            Graphics2D g2d = (Graphics2D) g.create();
            try {
                // Draw a semi-transparent shape preview at the drag location
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.setColor(Color.BLUE);

                // Draw a simple shape indicator based on the shape type
                if ("Rectangle".equals(shapeType)) {
                    g2d.fillRect(dragX - 20, dragY - 20, 40, 40);
                } else if ("Polygon".equals(shapeType)) {
                    int[] xPoints = { dragX, dragX - 20, dragX + 20 };
                    int[] yPoints = { dragY - 20, dragY + 20, dragY + 20 };
                    g2d.fillPolygon(xPoints, yPoints, 3);
                }
            } finally {
                g2d.dispose();
            }
        }
    }

    @Override
    public void onClick() {
        // When clicked, create a shape at the center of the whiteboard
        targetWhiteBoard.setPrototypeRegistry(prototypeRegistry);
        targetWhiteBoard.setCurrentShapeType(shapeType);
        targetWhiteBoard.addShapeToCenter(); // Use addShapeToCenter to place in the middle

        // Reset the current shape type to null after adding the shape
        // This ensures that clicking on the whiteboard won't add another shape
        // but clicking the button again will
        targetWhiteBoard.setCurrentShapeType(null);
    }

    @Override
    public void startDrag(int x, int y) {
        isDragging = true;
        dragX = x;
        dragY = y;

        // Set the current shape type in the whiteboard
        targetWhiteBoard.setPrototypeRegistry(prototypeRegistry);
        targetWhiteBoard.setCurrentShapeType(shapeType);
    }

    @Override
    public void drag(int x, int y) {
        dragX = x;
        dragY = y;
    }

    @Override
    public void endDrag(int x, int y) {
        isDragging = false;

        // Create the shape at the drop location
        targetWhiteBoard.createShapeAt(x, y);

        // Reset the current shape type to null after adding the shape
        targetWhiteBoard.setCurrentShapeType(null);
    }

    @Override
    public String getShapeType() {
        return shapeType;
    }

    @Override
    public String getText() {
        return decoratedButton.getText();
    }

    @Override
    public int getX() {
        return decoratedButton.getX();
    }

    @Override
    public int getY() {
        return decoratedButton.getY();
    }

    @Override
    public int getWidth() {
        return decoratedButton.getWidth();
    }

    @Override
    public int getHeight() {
        return decoratedButton.getHeight();
    }
}
