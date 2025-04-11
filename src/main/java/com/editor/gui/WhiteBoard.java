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
import com.editor.shapes.ShapePrototypeRegistry;

public class WhiteBoard extends Canvas {
    private double relX, relY, relW, relH;
    private Color backgroundColor = Color.WHITE;
    private List<Shape> shapes = new ArrayList<>();
    private CommandHistory commandHistory = new CommandHistory();
    private ShapePrototypeRegistry prototypeRegistry = null;
    private String currentShapeType = null;
    private Shape selectedShape;
    private Point dragStartPoint; // Where the mouse was initially pressed
    private Point originalShapePosition; // Top-left corner of the shape when drag started
    private Point dragOffset; // Difference between dragStartPoint and originalShapePosition
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
            // Store the currently selected shape
            Shape previouslySelected = selectedShape;

            // Reset drag state variables
            isDragging = false;
            originalShapePosition = null;
            dragStartPoint = null;
            dragOffset = null;

            // Try to select a shape under the mouse cursor
            boolean foundShape = false;
            for (Shape shape : shapes) {
                if (shape.isSelected(e.getX(), e.getY())) {
                    // Found a shape under the cursor
                    foundShape = true;
                    selectedShape = shape;
                    selectedShape.setSelected(true);

                    // Store the starting point for drag operation
                    dragStartPoint = e.getPoint();

                    // Store the original shape position for undo/redo and offset calculation
                    Rectangle bounds = selectedShape.getBounds();
                    originalShapePosition = new Point(bounds.getX(), bounds.getY());

                    // Calculate the offset between the click point and the shape's origin
                    dragOffset = new Point(
                            dragStartPoint.x - originalShapePosition.x,
                            dragStartPoint.y - originalShapePosition.y);

                    // Set dragging state
                    isDragging = true;
                    break;
                }
            }

            // If no shape was found under the cursor, deselect the current shape
            if (!foundShape) {
                // Only deselect if we're clicking on empty space
                if (previouslySelected != null) {
                    previouslySelected.setSelected(false);
                }
                selectedShape = null;
            } else if (previouslySelected != null && previouslySelected != selectedShape) {
                // If we selected a different shape, deselect the previous one
                previouslySelected.setSelected(false);
            }

            // Create new shape if none selected and a shape type is selected
            if (selectedShape == null && currentShapeType != null && prototypeRegistry != null) {
                createShapeAt(e.getX(), e.getY());
            }

            repaint();
        }
    }

    private void handleMouseDrag(MouseEvent e) {
        // Ensure dragging state, a shape is selected, and we have the offset
        if (isDragging && selectedShape != null && dragOffset != null) {
            // Calculate the new top-left position based on mouse position and initial
            // offset
            int newX = e.getX() - dragOffset.x;
            int newY = e.getY() - dragOffset.y;

            // Set the shape's position directly for smooth visual feedback
            selectedShape.setPosition(newX, newY);

            // Request a repaint to show the shape in its new position
            repaint();
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        // Check if a drag operation was in progress and completed
        if (isDragging && selectedShape != null && originalShapePosition != null) {
            // Get the final position of the shape after dragging
            Rectangle bounds = selectedShape.getBounds();
            Point finalPosition = new Point(bounds.getX(), bounds.getY());

            // Only create and execute a command if the position actually changed
            if (!originalShapePosition.equals(finalPosition)) {
                MoveShapeCommand moveCommand = new MoveShapeCommand(
                        selectedShape,
                        originalShapePosition,
                        finalPosition);

                commandHistory.executeCommand(moveCommand);
            }

            // Reset drag state variables regardless of whether a move occurred
            isDragging = false;
            dragStartPoint = null;
            originalShapePosition = null;
            dragOffset = null;

            // Repaint to potentially remove drag-specific highlighting
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
     * Sets the current shape type to use for creating shapes
     *
     * @param shapeType The type of shape to create (key in the prototype registry)
     */
    public void setCurrentShapeType(String shapeType) {
        this.currentShapeType = shapeType;
    }

    /**
     * Sets the prototype registry to use for creating shapes
     *
     * @param registry The shape prototype registry
     */
    public void setPrototypeRegistry(ShapePrototypeRegistry registry) {
        this.prototypeRegistry = registry;
    }

    /**
     * Creates a shape at the specified location using the current shape type
     */
    public void createShapeAt(int x, int y) {
        if (currentShapeType != null && prototypeRegistry != null) {
            Shape newShape = prototypeRegistry.createShape(currentShapeType, x, y);
            commandHistory.executeCommand(
                    new CreateShapeCommand(shapes, newShape, x, y));
            selectedShape = newShape;
            selectedShape.setSelected(true);
            repaint();
        }
    }

    /**
     * Adds a shape to the center of the whiteboard using the current shape type
     */
    public void addShapeToCenter() {
        if (currentShapeType != null && prototypeRegistry != null) {
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
