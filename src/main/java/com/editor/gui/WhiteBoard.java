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

import javax.swing.SwingUtilities;

import com.editor.commands.CommandHistory;
import com.editor.commands.CreateShapeCommand;
import com.editor.commands.MoveShapeCommand;
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
    private Point originalShapePosition;
    private boolean isDragging = false;

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
                handleMousePress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseRelease(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        });
    }

    private void handleMousePress(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            // Check for existing shape selection
            Shape previouslySelected = selectedShape;
            selectedShape = null;
            isDragging = false;
            originalShapePosition = null;

            // Try to select a shape under the mouse cursor
            for (Shape shape : shapes) {
                if (shape.isSelected(e.getX(), e.getY())) {
                    selectedShape = shape;
                    selectedShape.setSelected(true);

                    // Store the starting point for drag operation
                    dragStartPoint = e.getPoint();

                    // Store the original shape position for undo/redo
                    Rectangle bounds = selectedShape.getBounds();
                    originalShapePosition = new Point(bounds.getX(), bounds.getY());

                    // Set dragging state
                    isDragging = true;

                    break;
                }
            }

            // Deselect previous shape if a new one was selected or none was selected
            if (previouslySelected != null && previouslySelected != selectedShape) {
                previouslySelected.setSelected(false);
            }

            // Create new shape if none selected and factory is set
            if (selectedShape == null && currentShapeFactory != null) {
                createShapeAt(e.getX(), e.getY());
            }

            repaint();
        }
    }

    private void handleMouseDrag(MouseEvent e) {
        if (isDragging && selectedShape != null && dragStartPoint != null) {
            // Calculate the delta movement
            int dx = e.getX() - dragStartPoint.x;
            int dy = e.getY() - dragStartPoint.y;

            // Move the shape by the delta
            selectedShape.move(dx, dy);

            // Update the drag start point for the next drag event
            dragStartPoint = e.getPoint();

            // Request a repaint to show the shape in its new position
            repaint();
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        if (isDragging && selectedShape != null && originalShapePosition != null) {
            // Get the final position of the shape
            Rectangle bounds = selectedShape.getBounds();
            Point finalPosition = new Point(bounds.getX(), bounds.getY());

            // Create and execute a move command for undo/redo
            MoveShapeCommand moveCommand = new MoveShapeCommand(
                    selectedShape,
                    originalShapePosition.x, originalShapePosition.y,
                    finalPosition.x, finalPosition.y);

            commandHistory.executeCommand(moveCommand);

            // Reset drag state
            isDragging = false;
            dragStartPoint = null;
            originalShapePosition = null;

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
                Rectangle bounds = selectedShape.getBounds();

                if (isDragging) {
                    // Use a different highlight color and style for dragging
                    g2d.setColor(new Color(255, 165, 0, 80)); // Orange with transparency
                    g2d.fillRect(bounds.getX(), bounds.getY(),
                            bounds.getWidth(), bounds.getHeight());

                    // Draw a thicker border
                    g2d.setColor(new Color(255, 165, 0));
                    g2d.drawRect(bounds.getX() - 1, bounds.getY() - 1,
                            bounds.getWidth() + 2, bounds.getHeight() + 2);
                } else {
                    // Normal selection highlight
                    g2d.setColor(new Color(0, 0, 255, 50)); // Blue with transparency
                    g2d.fillRect(bounds.getX(), bounds.getY(),
                            bounds.getWidth(), bounds.getHeight());

                    // Draw a border
                    g2d.setColor(new Color(0, 0, 255));
                    g2d.drawRect(bounds.getX(), bounds.getY(),
                            bounds.getWidth(), bounds.getHeight());
                }
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
