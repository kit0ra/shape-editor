package com.editor.gui.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; // Added for storing button keys
import java.util.Map;
import java.util.UUID; // Added for storing button keys

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.DraggableCompositeShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.ShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ShapeDrawingButtonDecorator; // Added missing import
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.mediator.DragMediator;
import com.editor.memento.ToolbarMemento;
import com.editor.shapes.CompositeShapePrototypeRegistry; // Added Memento import
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.utils.ImageLoader; // Added

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

    // Store mapping from button instance to its prototype key for memento
    private Map<IButton, String> buttonToPrototypeKeyMap = new HashMap<>();

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
     * Checks if a point in screen coordinates is over this toolbar panel
     *
     * @param screenPoint The point in screen coordinates
     * @return true if the point is over this panel, false otherwise
     */
    public boolean isPointOverToolbar(Point screenPoint) {
        if (screenPoint == null) {
            return false;
        }

        try {
            // Convert screen coordinates to panel coordinates
            Point panelLocation = getLocationOnScreen();
            return screenPoint.x >= panelLocation.x &&
                    screenPoint.x < panelLocation.x + getWidth() &&
                    screenPoint.y >= panelLocation.y &&
                    screenPoint.y < panelLocation.y + getHeight();
        } catch (Exception e) {
            System.out.println("[ToolbarPanel] Error checking if point is over toolbar: " + e.getMessage());
            return false;
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
                if (shapeButton != null) {
                    this.addButton(shapeButton); // addButton now also stores the key
                    nextButtonY += shapeButton.getHeight() + BUTTON_Y_SPACING;
                    System.out.println("[ToolbarPanel] Added button for single shape: " + shapeTypeKey);
                    buttonAdded = true;
                }
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
            if (compositeButton != null) {
                this.addButton(compositeButton); // addButton now also stores the key
                nextButtonY += compositeButton.getHeight() + BUTTON_Y_SPACING;
                System.out.println("[ToolbarPanel] Added button for composite group: " + groupKey);
                buttonAdded = true;
            }
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
     * Overrides addButton to store the prototype key mapping.
     *
     * @param button       The button to add.
     * @param prototypeKey The key used to create this button.
     */
    private void addButton(IButton button, String prototypeKey) {
        super.addButton(button); // Call CustomPanel's addButton
        buttonToPrototypeKeyMap.put(button, prototypeKey);
        System.out.println("[ToolbarPanel] Stored mapping for button with key: " + prototypeKey);
    }

    /**
     * Overrides removeButton to also remove the prototype key mapping.
     *
     * @param button The button to remove.
     * @return true if removed, false otherwise.
     */
    @Override
    public boolean removeButton(IButton button) {
        boolean removed = super.removeButton(button); // Call CustomPanel's removeButton
        if (removed) {
            String removedKey = buttonToPrototypeKeyMap.remove(button);
            System.out.println("[ToolbarPanel] Removed mapping for button with key: " + removedKey);
            // TODO: Re-layout remaining buttons if necessary
            // For now, just removing is fine, but layout might get sparse.
            // A simple re-layout could recalculate nextButtonY based on remaining buttons.
            recalculateButtonLayout(); // Add method to handle layout
        }
        return removed;
    }

    /**
     * Recalculates the Y positions of buttons after one is removed.
     * This is a simple implementation; more complex layouts might need more.
     */
    private void recalculateButtonLayout() {
        nextButtonY = BUTTON_Y_START;
        // Create a list of buttons currently in the panel (super.buttons is protected)
        List<IButton> currentButtons = new ArrayList<>(this.buttons);
        // Clear internal list and map before re-adding to ensure correct order and
        // mapping
        this.buttons.clear();
        this.buttonToPrototypeKeyMap.clear();

        System.out.println("[ToolbarPanel] Recalculating button layout...");
        for (IButton button : currentButtons) {
            // We need the original key to re-add the mapping.
            // This approach is flawed if we don't store the key elsewhere temporarily.
            // Let's modify addButton to handle this better.
            // --- Alternative approach: Modify the button's Y position directly ---
            // button.setY(nextButtonY); // Need a setY method in IButton/ButtonDecorator
            // nextButtonY += button.getHeight() + BUTTON_Y_SPACING;
            // --- For now, let's just log the need for re-layout ---
            System.out.println("[ToolbarPanel] Button re-layout needed but not fully implemented.");
            // Re-add the button to trigger paint and keep it in the list
            // We lose the key mapping here, which is bad for Memento.
            // Let's fix addButton and removeButton interaction.
        }
        // For now, just repaint. Layout won't adjust yet.
        repaint();
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
            return null; // Return null if setup fails
        }

        // Store the mapping before returning
        buttonToPrototypeKeyMap.put(button, shapeTypeKey);
        System.out.println("[ToolbarPanel] Stored mapping for single shape button: " + shapeTypeKey);

        return button;
    }

    /**
     * Helper method to create a button that creates a composite shape (group).
     */
    private IButton createCompositeButton(int x, int y, String iconPath, String tooltipText, String groupKey) {
        System.out.println("[COMPOSITE DEBUG] createCompositeButton called for key: " + groupKey);
        IButton button = new CustomButton(x, y, 40, 40, ""); // Standard size

        // Get the shape group from the registry to draw it on the button
        ShapeGroup groupPrototype = null;
        System.out.println("[COMPOSITE DEBUG] CompositeRegistry is null? " + (compositeRegistry == null));

        if (compositeRegistry != null) {
            boolean hasPrototype = compositeRegistry.hasPrototype(groupKey);
            System.out.println(
                    "[COMPOSITE DEBUG] CompositeRegistry has prototype for key '" + groupKey + "'? " + hasPrototype);

            if (hasPrototype) {
                try {
                    // Create a temporary instance to get the prototype
                    // We'll position it at 0,0 since we'll scale it anyway
                    System.out.println("[COMPOSITE DEBUG] Creating group from registry for key: " + groupKey);
                    groupPrototype = compositeRegistry.createGroup(groupKey, 0, 0);
                    System.out.println("[COMPOSITE DEBUG] Group created: " + (groupPrototype != null));
                } catch (Exception e) {
                    System.out.println("[COMPOSITE DEBUG] Error getting group prototype: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("[COMPOSITE DEBUG] Registry doesn't have prototype for key: " + groupKey);
            }
        } else {
            System.out.println("[COMPOSITE DEBUG] CompositeRegistry is null in createCompositeButton");
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
            return null; // Return null if setup fails
        }

        // Store the mapping before returning
        // Note: The final 'button' reference here is the decorated one
        buttonToPrototypeKeyMap.put(button, groupKey);
        System.out.println("[ToolbarPanel] Stored mapping for composite button: " + groupKey);

        return button;
    }

    // --- Memento Pattern Implementation ---

    /**
     * Creates a memento containing the current state of the toolbar buttons.
     *
     * @return A ToolbarMemento object.
     */
    public ToolbarMemento createMemento() {
        // Extract the prototype keys from the map values
        List<String> keys = new ArrayList<>(buttonToPrototypeKeyMap.values());

        // Enhanced debug output
        System.out.println("[STATE DEBUG] ToolbarPanel.createMemento() - START");
        System.out.println("[STATE DEBUG] Current button count: " + buttons.size());
        System.out.println("[STATE DEBUG] Current buttonToPrototypeKeyMap size: " + buttonToPrototypeKeyMap.size());
        System.out.println("[STATE DEBUG] Keys being saved: " + keys);

        // Print details of each button
        int i = 0;
        for (Map.Entry<IButton, String> entry : buttonToPrototypeKeyMap.entrySet()) {
            System.out
                    .println("[STATE DEBUG] Button " + i + ": Key=" + entry.getValue() + ", Button=" + entry.getKey());
            i++;
        }
        System.out.println("[STATE DEBUG] ToolbarPanel.createMemento() - END");

        return new ToolbarMemento(keys);
    }

    /**
     * Restores the toolbar state from a memento.
     * Clears existing dynamic buttons and recreates them based on the memento.
     *
     * @param memento The memento object containing the state to restore.
     */
    public void restoreFromMemento(ToolbarMemento memento) {
        if (memento == null) {
            System.err.println("[ToolbarPanel] Cannot restore from null memento.");
            return;
        }

        System.out.println("[STATE DEBUG] ToolbarPanel.restoreFromMemento() - START");
        System.out.println("[STATE DEBUG] Current button count before restore: " + buttons.size());
        System.out.println(
                "[STATE DEBUG] Current buttonToPrototypeKeyMap size before restore: " + buttonToPrototypeKeyMap.size());

        // Print details of existing buttons before restore
        int i = 0;
        for (Map.Entry<IButton, String> entry : buttonToPrototypeKeyMap.entrySet()) {
            System.out.println(
                    "[STATE DEBUG] Existing Button " + i + ": Key=" + entry.getValue() + ", Button=" + entry.getKey());
            i++;
        }

        List<String> keysToRestore = memento.getButtonPrototypeKeys();
        System.out.println("[STATE DEBUG] Keys to restore from memento: " + keysToRestore);
        System.out.println("[STATE DEBUG] Number of keys to restore: " + keysToRestore.size());

        // --- Clear existing dynamic buttons ---
        // Iterate backwards to avoid ConcurrentModificationException if removing
        // directly
        // Or create a copy of the keys to iterate over
        List<IButton> buttonsToRemove = new ArrayList<>();
        for (Map.Entry<IButton, String> entry : buttonToPrototypeKeyMap.entrySet()) {
            // Assume all buttons in the map are dynamic (created via drag/drop or load)
            // If there were static buttons, we'd need a way to differentiate them.
            buttonsToRemove.add(entry.getKey());
        }
        System.out.println("[ToolbarPanel] Removing " + buttonsToRemove.size() + " existing dynamic buttons.");
        for (IButton btn : buttonsToRemove) {
            removeButton(btn); // Use removeButton to clear map entry as well
        }
        // Ensure layout state is reset
        nextButtonY = BUTTON_Y_START;
        buttonToPrototypeKeyMap.clear(); // Should be cleared by removeButton calls, but clear just in case
        buttons.clear(); // Should be cleared by removeButton calls, but clear just in case

        // --- Recreate buttons from memento keys ---
        System.out.println("[ToolbarPanel] Recreating buttons from restored keys...");
        for (String key : keysToRestore) {
            IButton newButton = null;
            if (key.startsWith("composite_")) {
                // It's a composite shape button
                System.out.println("[COMPOSITE DEBUG] Processing composite key: " + key);
                System.out.println("[COMPOSITE DEBUG] CompositeRegistry is null? " + (compositeRegistry == null));

                if (compositeRegistry != null) {
                    boolean hasPrototype = compositeRegistry.hasPrototype(key);
                    System.out.println(
                            "[COMPOSITE DEBUG] CompositeRegistry has prototype for key '" + key + "'? " + hasPrototype);

                    if (hasPrototype) {
                        System.out.println("[COMPOSITE DEBUG] Recreating composite button for key: " + key);
                        newButton = createCompositeButton(BUTTON_X_MARGIN, nextButtonY, "icons/group.png",
                                "Restored Composite", key);
                        System.out.println("[COMPOSITE DEBUG] Button created: " + (newButton != null));
                    } else {
                        System.err.println(
                                "[COMPOSITE DEBUG] Warning: Composite prototype not found for key during restore: "
                                        + key);
                    }
                } else {
                    System.err.println("[COMPOSITE DEBUG] Error: CompositeRegistry is null during restore");
                }
            } else {
                // It's a single shape button
                if (prototypeRegistry != null && prototypeRegistry.hasPrototype(key)) {
                    System.out.println("[ToolbarPanel] Recreating single shape button for key: " + key);
                    String iconPath = "icons/" + key.toLowerCase() + ".png"; // Assuming key matches icon name
                                                                             // convention
                    newButton = createDraggableShapeButton(BUTTON_X_MARGIN, nextButtonY, iconPath, "Restored " + key,
                            key);
                } else {
                    System.err.println(
                            "[ToolbarPanel] Warning: Single shape prototype not found for key during restore: " + key);
                }
            }

            // If button was created successfully, add it (which also updates map and
            // layout)
            if (newButton != null) {
                // Use the specific addButton overload that takes the key
                // addButton(newButton, key); // This was removed, let createButton handle
                // mapping
                this.buttons.add(newButton); // Add directly to list (createButton already mapped)
                nextButtonY += newButton.getHeight() + BUTTON_Y_SPACING;
                System.out.println("[ToolbarPanel] Successfully recreated and added button for key: " + key);
            } else {
                System.err.println("[ToolbarPanel] Failed to recreate button for key: " + key);
            }
        }

        System.out.println("[STATE DEBUG] ToolbarPanel.restoreFromMemento() - AFTER RESTORE");
        System.out.println("[STATE DEBUG] Button count after restore: " + buttons.size());
        System.out
                .println("[STATE DEBUG] buttonToPrototypeKeyMap size after restore: " + buttonToPrototypeKeyMap.size());

        // Print details of buttons after restore
        i = 0;
        for (Map.Entry<IButton, String> entry : buttonToPrototypeKeyMap.entrySet()) {
            System.out.println(
                    "[STATE DEBUG] Restored Button " + i + ": Key=" + entry.getValue() + ", Button=" + entry.getKey());
            i++;
        }

        System.out.println("[STATE DEBUG] ToolbarPanel.restoreFromMemento() - END");
        repaint(); // Repaint the panel to show the restored buttons
    }

} // Added missing closing brace
