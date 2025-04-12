package com.editor.gui.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList; // Added
import java.util.List; // Added
import java.util.UUID; // Added

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.DraggableCompositeShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.ShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ShapeDrawingButtonDecorator;
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.mediator.DragMediator;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;
import com.editor.shapes.ShapePrototypeRegistry; // Added
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
    private ShapePrototypeRegistry prototypeRegistry;
    private CompositeShapePrototypeRegistry compositeRegistry; // Added composite registry

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

        super.paint(g); // This handles drawing buttons via button.draw(g)

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
     * Sets the shape prototype registry for creating new shape buttons
     *
     * @param registry The prototype registry
     */
    public void setPrototypeRegistry(ShapePrototypeRegistry registry) {
        this.prototypeRegistry = registry;
    }

    /**
     * Sets the composite shape prototype registry for creating new group buttons.
     *
     * @param registry The composite prototype registry.
     */
    public void setCompositePrototypeRegistry(CompositeShapePrototypeRegistry registry) {
        this.compositeRegistry = registry;
    }

    /**
     * Sets the drag mediator for this panel
     *
     * @param mediator The mediator to use
     */
    @Override // Add Override annotation
    public void setDragMediator(DragMediator mediator) {
        super.setDragMediator(mediator); // Call the superclass method to register as a generic panel

        // Keep the local reference if needed for specific ToolbarPanel logic
        // this.dragMediator = mediator; // This line might be redundant if superclass
        // field is protected

        // Register with the mediator specifically as the ToolbarPanel
        if (mediator != null) {
            mediator.registerToolbarPanel(this);
            System.out.println("[ToolbarPanel] Registered with mediator (as ToolbarPanel and CustomPanel).");
        } else {
            System.out.println("[ToolbarPanel] DragMediator set to null.");
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
     * Adds the selected shapes from the whiteboard to the toolbar as buttons.
     * Handles single shapes, multiple shapes, and groups.
     *
     * @return true if shapes were added, false otherwise
     */
    public boolean addSelectedShapesToToolbar() {
        if (targetWhiteBoard == null) {
            System.out.println("[ToolbarPanel] Whiteboard not set.");
            return false;
        }
        // Check both registries are set
        if (prototypeRegistry == null || compositeRegistry == null) {
            System.out.println("[ToolbarPanel] Registries not set.");
            return false;
        }

        List<Shape> selectedShapes = targetWhiteBoard.getSelectedShapes();

        if (selectedShapes.isEmpty()) {
            System.out.println("[ToolbarPanel] No shapes selected.");
            return false;
        }

        System.out.println("[ToolbarPanel] Processing " + selectedShapes.size() + " selected shape(s) for toolbar.");
        boolean buttonAdded = false;

        // Case 1: Single, non-group shape selected
        if (selectedShapes.size() == 1 && !(selectedShapes.get(0) instanceof ShapeGroup)) {
            Shape singleShape = selectedShapes.get(0);
            String className = singleShape.getClass().getSimpleName();
            String shapeTypeKey;
            String iconName;

            // Map class name to registry key and icon name
            if (className.equals("RegularPolygon")) {
                shapeTypeKey = "Polygon";
                iconName = "polygon";
            } else if (className.equals("Rectangle")) {
                shapeTypeKey = "Rectangle";
                iconName = "rectangle";
            } else {
                shapeTypeKey = className;
                iconName = className.toLowerCase();
                System.out.println("[ToolbarPanel] Warning: No specific mapping for single shape class: " + className);
            }

            String iconPath = "icons/" + iconName + ".png";

            // Check standard registry
            if (prototypeRegistry.hasPrototype(shapeTypeKey)) {
                System.out.println("[ToolbarPanel] Creating single shape button for type: " + shapeTypeKey);
                IButton shapeButton = createDraggableShapeButton(
                        BUTTON_X_MARGIN, nextButtonY, iconPath, "Create a " + shapeTypeKey, shapeTypeKey);
                this.addButton(shapeButton);
                nextButtonY += shapeButton.getHeight() + BUTTON_Y_SPACING;
                System.out.println("[ToolbarPanel] Added button for single shape: " + shapeTypeKey);
                buttonAdded = true;
            } else {
                System.out.println(
                        "[ToolbarPanel] Warning: No prototype found for single shape type key: " + shapeTypeKey);
            }

            // Case 2: Multiple shapes selected OR a single ShapeGroup selected
        } else {
            System.out.println("[ToolbarPanel] Creating composite shape button.");
            List<Shape> shapesToGroup = new ArrayList<>();
            for (Shape s : selectedShapes) {
                // If the selected item is itself a group, add clones of its children
                if (s instanceof ShapeGroup) {
                    ShapeGroup group = (ShapeGroup) s;
                    // Use getShapes() to access children
                    for (Shape child : group.getShapes()) {
                        shapesToGroup.add(child.clone());
                    }
                } else {
                    // Otherwise, add a clone of the individual shape
                    shapesToGroup.add(s.clone());
                }
            }

            if (shapesToGroup.isEmpty()) {
                System.out.println("[ToolbarPanel] No shapes to group after processing selection.");
                return false;
            }

            // Create the group prototype
            ShapeGroup groupPrototype = new ShapeGroup(shapesToGroup);

            // Generate a unique key for the composite registry
            String groupKey = "composite_" + UUID.randomUUID().toString();

            // Register the new group prototype
            compositeRegistry.registerPrototype(groupKey, groupPrototype);

            // Create the composite button
            String iconPath = "icons/group.png"; // Use a generic group icon
            IButton compositeButton = createCompositeButton(
                    BUTTON_X_MARGIN, nextButtonY, iconPath, "Create composite (" + shapesToGroup.size() + " shapes)",
                    groupKey);
            this.addButton(compositeButton);
            nextButtonY += compositeButton.getHeight() + BUTTON_Y_SPACING;
            System.out.println("[ToolbarPanel] Added button for composite group: " + groupKey);
            buttonAdded = true;
        }

        if (buttonAdded) {
            // Deselect shapes on the whiteboard after adding them
            targetWhiteBoard.clearSelection(); // Use the new method
            targetWhiteBoard.repaint(); // Repaint whiteboard to show deselection
            repaint(); // Repaint the toolbar to show new buttons
            return true;
        } else {
            // If no button was added (e.g., prototype not found for single shape)
            return false;
        }
    }

    /**
     * Helper method to create a standard draggable shape button (for single
     * shapes).
     */
    private IButton createDraggableShapeButton(int x, int y, String iconPath, String tooltipText, String shapeTypeKey) {
        IButton button = new CustomButton(x, y, 40, 40, "");

        // Try to get the shape prototype to draw it on the button
        Shape shapePrototype = null;
        if (prototypeRegistry != null && prototypeRegistry.hasPrototype(shapeTypeKey)) {
            try {
                // Create a temporary instance to get the prototype
                // We'll position it at 0,0 since we'll scale it anyway
                shapePrototype = prototypeRegistry.createShape(shapeTypeKey, 0, 0);
            } catch (Exception e) {
                System.out.println("[ToolbarPanel] Error getting shape prototype: " + e.getMessage());
            }
        }

        // If we have a valid shape prototype, use it to draw on the button
        if (shapePrototype != null) {
            // Add shape drawing decorator to draw the shape on the button
            button = new ShapeDrawingButtonDecorator(button, shapePrototype, 0.5, 4);
            System.out.println("[ToolbarPanel] Using shape drawing for button: " + shapeTypeKey);
        } else {
            // Fallback to using an icon if we couldn't get the shape
            Image icon = ImageLoader.loadImage(iconPath);
            if (icon != null) {
                button = new ImageDecorator(button, icon, 24, 24, 8, ImageDecorator.ImageMode.ICON_ONLY);
            } else {
                System.out.println("[ToolbarPanel] Warning: Icon not found at path: " + iconPath);
            }
        }

        button = new TooltipDecorator(button, tooltipText);

        if (targetWhiteBoard != null && prototypeRegistry != null) {
            button = new ShapeCreationButtonDecorator(button, targetWhiteBoard, prototypeRegistry, shapeTypeKey);
        } else {
            System.out.println(
                    "[ToolbarPanel] Error: Whiteboard or PrototypeRegistry not set for ShapeCreationButtonDecorator.");
        }
        return button;
    }

    /**
     * Helper method to create a button that creates a composite shape (group).
     */
    private IButton createCompositeButton(int x, int y, String iconPath, String tooltipText, String groupKey) {
        IButton button = new CustomButton(x, y, 40, 40, ""); // Standard size

        // Get the shape group from the registry to draw it on the button
        ShapeGroup groupPrototype = null;
        if (compositeRegistry != null && compositeRegistry.hasPrototype(groupKey)) {
            try {
                // Create a temporary instance to get the prototype
                // We'll position it at 0,0 since we'll scale it anyway
                groupPrototype = compositeRegistry.createGroup(groupKey, 0, 0);
            } catch (Exception e) {
                System.out.println("[ToolbarPanel] Error getting group prototype: " + e.getMessage());
            }
        }

        // If we have a valid group prototype, use it to draw on the button
        if (groupPrototype != null) {
            // Add shape drawing decorator to draw the group on the button
            button = new ShapeDrawingButtonDecorator(button, groupPrototype, 0.5, 4);
            System.out.println("[ToolbarPanel] Using shape drawing for group button");
        } else {
            // Fallback to using an icon if we couldn't get the group
            Image icon = ImageLoader.loadImage(iconPath);
            if (icon != null) {
                button = new ImageDecorator(button, icon, 24, 24, 8, ImageDecorator.ImageMode.ICON_ONLY);
            } else {
                System.out.println("[ToolbarPanel] Warning: Group icon not found at path: " + iconPath);
            }
        }

        // Add tooltip
        button = new TooltipDecorator(button, tooltipText);

        // Use the DraggableCompositeShapeCreationButtonDecorator instead of
        // CompositeShapeCreationButtonDecorator
        if (targetWhiteBoard != null && compositeRegistry != null) {
            // Create a draggable composite button
            DraggableCompositeShapeCreationButtonDecorator compositeButton = new DraggableCompositeShapeCreationButtonDecorator(
                    button, targetWhiteBoard, compositeRegistry, groupKey);

            // Set the drag mediator on the button if available
            if (dragMediator != null) {
                compositeButton.setDragMediator(dragMediator);
                System.out.println("[ToolbarPanel] Set drag mediator on composite button: " + groupKey);
            }

            button = compositeButton;
        } else {
            System.out.println(
                    "[ToolbarPanel] Error: Whiteboard or CompositeRegistry not set for DraggableCompositeShapeCreationButtonDecorator.");
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
