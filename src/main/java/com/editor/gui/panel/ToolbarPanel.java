package com.editor.gui.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.ShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.mediator.DragMediator;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.utils.ImageLoader;

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
    private ShapePrototypeRegistry prototypeRegistry; // Added registry

    // Button layout constants
    private static final int BUTTON_Y_START = 30; // Start Y position below title
    private static final int BUTTON_X_MARGIN = 10;
    private static final int BUTTON_Y_SPACING = 5;
    private int nextButtonY = BUTTON_Y_START; // Track next button position

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
        g2d.setColor(Color.GRAY);
        g2d.drawLine(10, 25, getWidth() - 10, 25);

        // No need to paint buttons here, super.paint(g) handles it via button.draw(g)
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
     * Sets the shape prototype registry for creating new shape buttons
     *
     * @param registry The prototype registry
     */
    public void setPrototypeRegistry(ShapePrototypeRegistry registry) {
        this.prototypeRegistry = registry;
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
                    String className = shape.getClass().getSimpleName();
                    String shapeTypeKey; // Key used for registry lookup and button creation
                    String iconName; // Name used for icon path

                    // Map class name to registry key and icon name
                    if (className.equals("RegularPolygon")) {
                        shapeTypeKey = "Polygon";
                        iconName = "polygon";
                    } else if (className.equals("Rectangle")) {
                        shapeTypeKey = "Rectangle";
                        iconName = "rectangle";
                    } else {
                        // Default to class name if no specific mapping
                        shapeTypeKey = className;
                        iconName = className.toLowerCase();
                        System.out.println("[ToolbarPanel] Warning: No specific mapping for class: " + className
                                + ". Using defaults.");
                    }

                    // Construct icon path
                    String iconPath = "icons/" + iconName + ".png";

                    // Check if a prototype exists for this shape type key using hasPrototype
                    if (prototypeRegistry != null && prototypeRegistry.hasPrototype(shapeTypeKey)) {
                        System.out.println("[ToolbarPanel] Creating button for shape type: " + shapeTypeKey
                                + " (from class: " + className + ") with icon: " + iconPath);

                        // Create the draggable shape button
                        IButton shapeButton = createDraggableShapeButton(
                                BUTTON_X_MARGIN, // Fixed X position for vertical layout
                                nextButtonY,
                                iconPath,
                                "Create a " + shapeTypeKey, // Tooltip text
                                shapeTypeKey); // Use the mapped key

                        // Add the button to this panel's list
                        this.addButton(shapeButton);

                        // Update the Y position for the next button
                        nextButtonY += shapeButton.getHeight() + BUTTON_Y_SPACING;

                        System.out.println("[ToolbarPanel] Added button for: " + shapeTypeKey);
                    } else {
                        System.out.println(
                                "[ToolbarPanel] Warning: No prototype found for shape type key: " + shapeTypeKey
                                        + " (derived from class: " + className + "). Cannot add button.");
                    }
                }

                // Deselect shapes on the whiteboard after adding them
                // (Manual deselection as clearSelection() doesn't exist)
                for (Shape s : selectedShapes) {
                    s.setSelected(false);
                }
                // Note: We cannot directly clear the whiteboard's selectedShapes list from
                // here.
                // The whiteboard should ideally handle its own selection state changes.
                // For now, we just visually deselect. The whiteboard's internal list remains.
                // A better approach would be a method on WhiteBoard like deselectAll().

                targetWhiteBoard.repaint(); // Repaint whiteboard to show deselection
                repaint(); // Repaint the toolbar to show new buttons
                return true;
            } else {
                System.out.println("[ToolbarPanel] No shapes selected.");
            }
        } else {
            System.out.println("[ToolbarPanel] Whiteboard not set.");
        }
        return false; // Return false if whiteboard is null or no shapes selected
    }

    /**
     * Helper method to create a draggable shape button with icon and tooltip
     * (Adapted from ShapeEditorFrame)
     */
    private IButton createDraggableShapeButton(int x, int y, String iconPath, String tooltipText, String shapeType) {
        // Load the icon
        Image icon = ImageLoader.loadImage(iconPath);

        // Create base button (using standard toolbar button size)
        IButton button = new CustomButton(x, y, 40, 40, ""); // Standard size

        // Add icon decoration
        if (icon != null) {
            button = new ImageDecorator(
                    button,
                    icon,
                    24, 24, // Icon dimensions
                    8, // Padding
                    ImageDecorator.ImageMode.ICON_ONLY // Icon-only mode
            );
        } else {
            System.out.println("[ToolbarPanel] Warning: Icon not found at path: " + iconPath);
        }

        // Add tooltip decoration
        button = new TooltipDecorator(button, tooltipText);

        // Add shape creation decorator to make it draggable
        // Ensure whiteboard and registry are available
        if (targetWhiteBoard != null && prototypeRegistry != null) {
            button = new ShapeCreationButtonDecorator(button, targetWhiteBoard, prototypeRegistry, shapeType);
        } else {
            System.out.println(
                    "[ToolbarPanel] Error: Whiteboard or PrototypeRegistry not set. Cannot make button draggable.");
        }

        return button;
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
