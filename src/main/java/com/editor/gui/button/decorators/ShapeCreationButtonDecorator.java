package com.editor.gui.button.decorators;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.button.IButton;
import com.editor.mediator.DragMediator;
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
    private DragMediator dragMediator;

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
        
        super.draw(g);

        
        if (isDragging) {
            Graphics2D g2d = (Graphics2D) g.create();
            try {
                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                
                Color lightPink = new Color(255, 182, 193, 128); 
                Color lightPinkBorder = new Color(255, 105, 180); 
                if (null != shapeType) 
                    switch (shapeType) {
                        case "Rectangle":
                            g2d.setColor(lightPink);
                            g2d.fillRect(dragX - 30, dragY - 20, 60, 40);
                            g2d.setColor(lightPinkBorder);
                            g2d.drawRect(dragX - 30, dragY - 20, 60, 40);
                            break;
                        case "Polygon": {
                            
                            int radius = 30;
                            int sides = 6;
                            int[] xPoints = new int[sides];
                            int[] yPoints = new int[sides];
                            for (int i = 0; i < sides; i++) {
                                double angle = 2.0 * Math.PI * i / sides;
                                xPoints[i] = (int) (dragX + radius * Math.cos(angle));
                                yPoints[i] = (int) (dragY + radius * Math.sin(angle));
                            }
                            g2d.setColor(lightPink);
                            g2d.fillPolygon(xPoints, yPoints, sides);
                            g2d.setColor(lightPinkBorder);
                            g2d.drawPolygon(xPoints, yPoints, sides);
                            break;
                        }
                        case "Circle": {
                            
                            int radius = 30;
                            g2d.setColor(lightPink);
                            g2d.fillOval(dragX - radius, dragY - radius, radius * 2, radius * 2);
                            g2d.setColor(lightPinkBorder);
                            g2d.drawOval(dragX - radius, dragY - radius, radius * 2, radius * 2);
                            break;
                        }
                        default:
                            break;
                    }

                
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
        
        targetWhiteBoard.setPrototypeRegistry(prototypeRegistry);
        targetWhiteBoard.setCurrentShapeType(shapeType);
        targetWhiteBoard.addShapeToTopLeft(); 

        
        
        
        targetWhiteBoard.setCurrentShapeType(null);
    }

    @Override
    public void startDrag(int x, int y) {
        isDragging = true;
        dragX = x;
        dragY = y;

        
        targetWhiteBoard.setPrototypeRegistry(prototypeRegistry);
        targetWhiteBoard.setCurrentShapeType(shapeType);

        
        if (dragMediator != null) {
            dragMediator.startDrag(this, this, x, y);
        }
    }

    @Override
    public void drag(int x, int y) {
        dragX = x;
        dragY = y;
    }

    @Override
    public void endDrag(int x, int y) {
        isDragging = false;

        
        
        if (x >= 0 && y >= 0) {
            
            targetWhiteBoard.createShapeAt(x, y);
        }

        
        targetWhiteBoard.setCurrentShapeType(null);
    }

    @Override
    public String getShapeType() {
        return shapeType;
    }

    /**
     * Sets the drag mediator for this button.
     *
     * @param mediator The drag mediator
     */
    public void setDragMediator(DragMediator mediator) {
        this.dragMediator = mediator;
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
