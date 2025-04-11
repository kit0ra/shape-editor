package com.editor.gui.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import com.editor.gui.WhiteBoard;
import com.editor.mediator.DragMediator;
import com.editor.shapes.Shape;

/**
 * A panel that serves as a toolbar for additional tools and functionality.
 * This panel is positioned between the shapes panel and the trash panel.
 */
public class ToolbarPanel extends CustomPanel {

    private static final String PANEL_TITLE = "Toolbar";
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);

    private boolean isShapeOverToolbar = false;
    private WhiteBoard targetWhiteBoard;
    private DragMediator dragMediator;

    // Colors for visual feedback
    private final Color normalColor = new Color(240, 240, 240); // Light gray background
    private final Color hoverColor = new Color(220, 240, 220); // Light green when shape is over toolbar

    public ToolbarPanel() {
        super();
        setBackground(normalColor);
    }

    @Override
    public void paint(Graphics g) {
        // Set the background color based on whether a shape is over the toolbar
        setBackground(isShapeOverToolbar ? hoverColor : normalColor);

        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a title at the top of the panel
        g2d.setFont(TITLE_FONT);
        g2d.setColor(Color.DARK_GRAY);

        // Center the title
        int titleWidth = g2d.getFontMetrics().stringWidth(PANEL_TITLE);
        int x = (getWidth() - titleWidth) / 2;
        g2d.drawString(PANEL_TITLE, x, 20);

        // Draw a separator line below the title
        g2d.setColor(Color.GRAY);
        g2d.drawLine(10, 25, getWidth() - 10, 25);
    }

    /**
     * Sets the whiteboard that this toolbar panel will interact with
     * 
     * @param whiteBoard The target whiteboard
     */
    public void setTargetWhiteBoard(WhiteBoard whiteBoard) {
        this.targetWhiteBoard = whiteBoard;
    }

    /**
     * Sets the drag mediator for this panel
     * 
     * @param mediator The mediator to use
     */
    public void setDragMediator(DragMediator mediator) {
        this.dragMediator = mediator;

        // Register with the mediator as a special panel
        if (mediator != null) {
            mediator.registerToolbarPanel(this);
        }
    }

    /**
     * Called by the mediator when a shape is being dragged over this panel
     * 
     * @param isOver Whether a shape is currently over the toolbar panel
     */
    public void setShapeOverToolbar(boolean isOver) {
        if (this.isShapeOverToolbar != isOver) {
            this.isShapeOverToolbar = isOver;
            System.out.println("[ToolbarPanel] Shape is " + (isOver ? "over" : "not over") + " toolbar");
            repaint();
        }
    }

    /**
     * Adds the selected shapes from the whiteboard to the toolbar as buttons
     * 
     * @return true if shapes were added, false otherwise
     */
    public boolean addSelectedShapesToToolbar() {
        if (targetWhiteBoard != null) {
            java.util.List<Shape> selectedShapes = targetWhiteBoard.getSelectedShapes();
            if (!selectedShapes.isEmpty()) {
                System.out.println("[ToolbarPanel] Adding " + selectedShapes.size() + " shape(s) to toolbar");

                // Add each selected shape to the toolbar as a button
                for (Shape shape : selectedShapes) {
                    // Create a button from the shape
                    // This will be implemented in a future update
                    System.out.println("[ToolbarPanel] Added shape: " + shape.getClass().getSimpleName());
                }

                repaint();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a point in screen coordinates is over this toolbar panel
     * 
     * @param screenPoint The point in screen coordinates
     * @return true if the point is over this panel, false otherwise
     */
    public boolean isPointOverToolbar(Point screenPoint) {
        try {
            Point panelLocation = getLocationOnScreen();
            boolean isOver = screenPoint.x >= panelLocation.x &&
                    screenPoint.x < panelLocation.x + getWidth() &&
                    screenPoint.y >= panelLocation.y &&
                    screenPoint.y < panelLocation.y + getHeight();

            if (isOver) {
                System.out.println("[ToolbarPanel] Point is over toolbar panel");
            }

            return isOver;
        } catch (Exception e) {
            return false;
        }
    }
}
