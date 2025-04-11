package com.editor.gui.button.decorators;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
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

                // Utiliser des couleurs différentes selon le type de forme
                if ("Rectangle".equals(shapeType)) {
                    g2d.setColor(new Color(0, 0, 255, 128)); // Bleu semi-transparent
                    g2d.fillRect(dragX - 30, dragY - 20, 60, 40);
                    g2d.setColor(Color.BLUE);
                    g2d.drawRect(dragX - 30, dragY - 20, 60, 40);
                } else if ("Polygon".equals(shapeType)) {
                    // Dessiner un hexagone pour le polygone
                    int radius = 30;
                    int sides = 6;
                    int[] xPoints = new int[sides];
                    int[] yPoints = new int[sides];

                    for (int i = 0; i < sides; i++) {
                        double angle = 2.0 * Math.PI * i / sides;
                        xPoints[i] = (int) (dragX + radius * Math.cos(angle));
                        yPoints[i] = (int) (dragY + radius * Math.sin(angle));
                    }

                    g2d.setColor(new Color(0, 255, 0, 128)); // Vert semi-transparent
                    g2d.fillPolygon(xPoints, yPoints, sides);
                    g2d.setColor(Color.GREEN);
                    g2d.drawPolygon(xPoints, yPoints, sides);
                }

                // Dessiner une ligne pointillée pour indiquer le glisser-déposer
                g2d.setColor(Color.DARK_GRAY);
                float[] dash = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                g2d.drawLine(getX() + getWidth() / 2, getY() + getHeight() / 2, dragX, dragY);
            } finally {
                g2d.dispose();
            }
        }
    }

    @Override
    public void onClick() {
        // When clicked, create a shape at the top-left corner of the whiteboard
        targetWhiteBoard.setPrototypeRegistry(prototypeRegistry);
        targetWhiteBoard.setCurrentShapeType(shapeType);
        targetWhiteBoard.addShapeToTopLeft(); // Use addShapeToTopLeft to place in the top-left corner

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

        // Vérifier si les coordonnées sont valides (pas -1, -1 qui indique une annulation)
        if (x >= 0 && y >= 0) {
            // Create the shape at the drop location
            targetWhiteBoard.createShapeAt(x, y);
        }

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
