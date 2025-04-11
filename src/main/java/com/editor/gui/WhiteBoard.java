package com.editor.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import com.editor.commands.CommandHistory;
import com.editor.commands.CreateShapeCommand;
import com.editor.drawing.AWTDrawing;
import com.editor.drawing.Drawer;
import com.editor.shapes.Rectangle;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeFactory;

public class WhiteBoard extends Canvas {
    private double relX, relY, relW, relH;
    private Color backgroundColor = Color.WHITE;
    private List<Shape> shapes = new ArrayList<>();
    private CommandHistory commandHistory = new CommandHistory();
    private ShapeFactory currentShapeFactory = null;
    private Shape selectedShape;
    private Point dragStartPoint;

    public WhiteBoard(int width, int height, Color white) {
        this.setPreferredSize(new Dimension(width, height));
        this.backgroundColor = white;
        this.setBackground(white);

        setupMouseListeners();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Mouse pressed at: " + e.getX() + ", " + e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStartPoint = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        });
    }

    private void handleMouseDrag(MouseEvent e) {
        if (selectedShape != null && dragStartPoint != null) {
            int dx = e.getX() - dragStartPoint.x;
            int dy = e.getY() - dragStartPoint.y;
            selectedShape.move(dx, dy);
            dragStartPoint = e.getPoint();
            repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Clear the background
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw all shapes
        Drawer drawer = new AWTDrawing((Graphics2D) g);
        for (Shape shape : shapes) {
            shape.draw(drawer);
        }

        // Highlight selected shape
        if (selectedShape != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            try {
                g2d.setColor(new Color(0, 0, 255, 50));
                Rectangle bounds = selectedShape.getBounds();
                g2d.fillRect(bounds.getX(), bounds.getY(),
                        bounds.getWidth(), bounds.getHeight());
            } finally {
                g2d.dispose();
            }
        }
    }

    /**
     * Sets the current shape factory to use for creating shapes
     */
    public void setCurrentShapeFactory(ShapeFactory factory) {
        this.currentShapeFactory = factory;
    }

    /**
     * Creates a shape at the specified location using the current shape factory
     */
    public void createShapeAt(int x, int y) {
        if (currentShapeFactory != null) {
            Shape newShape = currentShapeFactory.createShape(x, y);
            commandHistory.executeCommand(
                    new CreateShapeCommand(shapes, newShape, x, y));
            selectedShape = newShape;
            selectedShape.setSelected(true);
            repaint();
        }
    }

    /**
     * Adds a shape to the center of the whiteboard using the current shape factory
     */
    public void addShapeToCenter() {
        if (currentShapeFactory != null) {
            // Calculate the center of the whiteboard
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            createShapeAt(centerX, centerY);
        }
    }

    public void undo() {
        commandHistory.undo();
        repaint();
    }

    public void redo() {
        commandHistory.redo();
        repaint();
    }

    public void setRelativeBounds(double xPercent, double yPercent, double widthPercent, double heightPercent) {
        this.relX = xPercent / 100.0;
        this.relY = yPercent / 100.0;
        this.relW = widthPercent / 100.0;
        this.relH = heightPercent / 100.0;
    }

    public void makeResponsiveTo(Frame frame) {
        ComponentAdapter resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBounds(frame);
            }
        };

        frame.addComponentListener(resizeListener);

        // Initial positioning
        updateBounds(frame);
    }

    private void updateBounds(Frame frame) {
        int w = frame.getWidth();
        int h = frame.getHeight();

        int x = (int) (w * relX);
        int y = (int) (h * relY);
        int width = (int) (w * relW);
        int height = (int) (h * relH);

        setBounds(x, y, width, height);
        repaint();
    }
}
