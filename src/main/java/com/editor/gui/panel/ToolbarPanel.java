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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.DraggableCompositeShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.ShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ShapeDrawingButtonDecorator;
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.mediator.DragMediator;
import com.editor.memento.ToolbarMemento;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.state.StateChangeListener;
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
    // Mediator field in CustomPanel (superclass) is protected now
    // private DragMediator dragMediator;
    private ShapePrototypeRegistry prototypeRegistry;
    private CompositeShapePrototypeRegistry compositeRegistry;

    // State change listener for auto-save functionality
    private StateChangeListener stateChangeListener;

    // Button layout constants
    private static final int BUTTON_Y_START = 30;
    private static final int BUTTON_X_MARGIN = 10;
    private static final int BUTTON_Y_SPACING = 5;
    private int nextButtonY = BUTTON_Y_START;

    // Store mapping from button instance to its prototype key for memento
    // Use the 'buttons' list from CustomPanel and this map in parallel
    private Map<IButton, String> buttonToPrototypeKeyMap = new HashMap<>();

    // Colors for visual feedback
    private final Color normalColor = new Color(240, 240, 240);
    private final Color hoverColor = new Color(220, 240, 220);

    public ToolbarPanel() {
        super();
        setBackground(normalColor);
    }

    @Override
    public void paint(Graphics g) {
        setBackground(isShapeOverToolbar ? hoverColor : normalColor);
        super.paint(g); // Draws background and buttons from CustomPanel's list

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(TITLE_FONT);
        g2d.setColor(Color.DARK_GRAY);
        int titleWidth = g2d.getFontMetrics().stringWidth(PANEL_TITLE);
        int x = (getWidth() - titleWidth) / 2;
        g2d.drawString(PANEL_TITLE, x, 20);

        g2d.setColor(Color.GRAY);
        g2d.drawLine(10, 25, getWidth() - 10, 25);
    }

    public void setTargetWhiteBoard(WhiteBoard whiteBoard) {
        this.targetWhiteBoard = whiteBoard;
    }

    public void setPrototypeRegistry(ShapePrototypeRegistry registry) {
        this.prototypeRegistry = registry;
    }

    public void setCompositePrototypeRegistry(CompositeShapePrototypeRegistry registry) {
        this.compositeRegistry = registry;
        System.out.println("[ToolbarPanel] CompositeShapePrototypeRegistry set: " + (registry != null));
    }

    /**
     * Sets the state change listener for this toolbar panel.
     * The listener will be notified when significant state changes occur.
     *
     * @param listener The state change listener
     */
    public void setStateChangeListener(StateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    /**
     * Notifies the state change listener that a significant state change has
     * occurred.
     *
     * @param description A description of the change
     */
    private void notifyStateChanged(String description) {
        if (stateChangeListener != null) {
            stateChangeListener.onStateChanged(this, description);
        }
    }

    @Override
    public void setDragMediator(DragMediator mediator) {
        super.setDragMediator(mediator); // Registers as CustomPanel
        if (mediator != null) {
            mediator.registerToolbarPanel(this); // Registers specifically as ToolbarPanel
            System.out.println("[ToolbarPanel] Registered with mediator (as ToolbarPanel and CustomPanel).");
        } else {
            System.out.println("[ToolbarPanel] DragMediator set to null.");
        }
    }

    public void setShapeOverToolbar(boolean isOver) {
        if (this.isShapeOverToolbar != isOver) {
            this.isShapeOverToolbar = isOver;
            System.out.println("[ToolbarPanel] Shape is " + (isOver ? "over" : "not over") + " toolbar");
            repaint();
        }
    }

    public boolean isPointOverToolbar(Point screenPoint) {
        if (screenPoint == null)
            return false;
        try {
            Point panelLocation = getLocationOnScreen();
            return screenPoint.x >= panelLocation.x &&
                    screenPoint.x < panelLocation.x + getWidth() &&
                    screenPoint.y >= panelLocation.y &&
                    screenPoint.y < panelLocation.y + getHeight();
        } catch (Exception e) {
            System.err.println("[ToolbarPanel] Error checking if point is over toolbar: " + e.getMessage());
            return false;
        }
    }

    public boolean addSelectedShapesToToolbar() {
        if (targetWhiteBoard == null || prototypeRegistry == null || compositeRegistry == null) {
            System.err.println("[ToolbarPanel] Cannot add shapes: Whiteboard or Registries not set.");
            return false;
        }
        List<Shape> selectedShapes = targetWhiteBoard.getSelectedShapes();
        if (selectedShapes.isEmpty()) {
            System.out.println("[ToolbarPanel] No shapes selected to add.");
            return false;
        }

        System.out.println("[ToolbarPanel] Processing " + selectedShapes.size() + " selected shape(s) for toolbar.");
        boolean buttonAdded = false;
        IButton newButton = null;
        String newKey = null;

        if (selectedShapes.size() == 1 && !(selectedShapes.get(0) instanceof ShapeGroup)) {
            // Handle single shape
            Shape singleShape = selectedShapes.get(0);
            String className = singleShape.getClass().getSimpleName();
            String shapeTypeKey = className.equals("RegularPolygon") ? "Polygon" : className; // Basic mapping
            String iconName = shapeTypeKey.toLowerCase();
            String iconPath = "icons/" + iconName + ".png";

            if (prototypeRegistry.hasPrototype(shapeTypeKey)) {
                System.out.println("[ToolbarPanel] Creating single shape button for type: " + shapeTypeKey);

                // Clone the selected shape to preserve its properties (color, rotation, etc.)
                Shape clonedShape = singleShape.clone();

                // Create a unique key for this specific shape with its properties
                String uniqueShapeKey = shapeTypeKey + "_" + UUID.randomUUID().toString();

                // Register this specific shape as a prototype
                prototypeRegistry.registerPrototype(uniqueShapeKey, clonedShape);

                // Create a button that will use this specific shape prototype
                newButton = createDraggableShapeButton(BUTTON_X_MARGIN, nextButtonY, iconPath,
                        "Create a " + shapeTypeKey, uniqueShapeKey, clonedShape);
                newKey = uniqueShapeKey;
                buttonAdded = (newButton != null);
            } else {
                System.err.println(
                        "[ToolbarPanel] Warning: No prototype found for single shape type key: " + shapeTypeKey);
            }
        } else {
            // Handle group or multiple shapes
            System.out.println("[ToolbarPanel] Creating composite shape button.");
            List<Shape> shapesToGroup = new ArrayList<>();
            for (Shape s : selectedShapes) {
                shapesToGroup.add(s.clone()); // Clone shapes for the prototype
            }
            if (!shapesToGroup.isEmpty()) {
                ShapeGroup groupPrototype = new ShapeGroup(shapesToGroup);
                String groupKey = "composite_" + UUID.randomUUID().toString();
                compositeRegistry.registerPrototype(groupKey, groupPrototype);
                String iconPath = "icons/group.png"; // Use a generic group icon
                newButton = createCompositeButton(BUTTON_X_MARGIN, nextButtonY, iconPath,
                        "Create composite (" + shapesToGroup.size() + " shapes)", groupKey);
                newKey = groupKey;
                buttonAdded = (newButton != null);
            } else {
                System.out.println("[ToolbarPanel] No shapes to group after processing selection.");
            }
        }

        if (buttonAdded && newButton != null && newKey != null) {
            this.addButton(newButton, newKey); // Use the specific addButton to store mapping
            nextButtonY += newButton.getHeight() + BUTTON_Y_SPACING;
            System.out.println("[ToolbarPanel] Added button for key: " + newKey);
            targetWhiteBoard.clearSelection();
            targetWhiteBoard.repaint();
            repaint();

            // Explicitly notify state change listener for shapes dragged from whiteboard
            // This is in addition to the notification in addButton
            notifyStateChanged("Shapes dragged from whiteboard to toolbar");

            return true;
        }
        return false;
    }

    // Use this specific method to add buttons AND store their keys
    private void addButton(IButton button, String prototypeKey) {
        super.addButton(button); // Adds to CustomPanel's list and repaints
        buttonToPrototypeKeyMap.put(button, prototypeKey);
        System.out.println("[ToolbarPanel] Stored mapping for button with key: " + prototypeKey);

        // Notify state change listener
        notifyStateChanged("Button added to toolbar with key: " + prototypeKey);
    }

    @Override
    public boolean removeButton(IButton button) {
        boolean removed = super.removeButton(button); // Removes from CustomPanel's list and repaints
        if (removed) {
            String removedKey = buttonToPrototypeKeyMap.remove(button);
            System.out.println("[ToolbarPanel] Removed mapping for button with key: " + removedKey);
            recalculateButtonLayout(); // Adjust layout after removal

            // Notify state change listener
            notifyStateChanged("Button removed from toolbar with key: " + removedKey);
        }
        return removed;
    }

    // TODO: Implement a proper layout recalculation
    private void recalculateButtonLayout() {
        System.out.println("[ToolbarPanel] Recalculating button layout (simple implementation)...");
        nextButtonY = BUTTON_Y_START;
        List<IButton> currentButtons = new ArrayList<>(this.buttons); // Get current buttons from superclass
        this.buttons.clear(); // Clear superclass list
        Map<IButton, String> currentMap = new HashMap<>(this.buttonToPrototypeKeyMap); // Copy map
        this.buttonToPrototypeKeyMap.clear(); // Clear map

        for (IButton button : currentButtons) {
            String key = currentMap.get(button); // Find the key for this button
            if (key != null) {
                // Need to update Y position - requires ButtonDecorator to expose setY or
                // similar
                // For now, just re-add with potentially incorrect Y, but correct mapping
                System.out.println("[ToolbarPanel] Re-adding button for key: " + key + " (Layout adjustment needed)");
                this.addButton(button, key); // Re-add using the method that updates the map
                // Ideally: button.setY(nextButtonY);
                nextButtonY += button.getHeight() + BUTTON_Y_SPACING;
            } else {
                System.err.println("[ToolbarPanel] Warning: Could not find key for button during re-layout: " + button);
            }
        }
        repaint();
    }

    private IButton createDraggableShapeButton(int x, int y, String iconPath, String tooltipText, String shapeTypeKey) {
        return createDraggableShapeButton(x, y, iconPath, tooltipText, shapeTypeKey, null);
    }

    private IButton createDraggableShapeButton(int x, int y, String iconPath, String tooltipText, String shapeTypeKey,
            Shape customShapePrototype) {
        IButton button = new CustomButton(x, y, 40, 40, "");
        Shape shapePrototype = null;

        // If a custom shape prototype is provided, use it directly
        if (customShapePrototype != null) {
            shapePrototype = customShapePrototype;
        }
        // Otherwise, get the shape from the registry
        else if (prototypeRegistry != null && prototypeRegistry.hasPrototype(shapeTypeKey)) {
            try {
                shapePrototype = prototypeRegistry.createShape(shapeTypeKey, 0, 0);
            } catch (Exception e) {
                System.err.println("[ToolbarPanel] Error creating shape from registry: " + e.getMessage());
            }
        }

        if (shapePrototype != null) {
            button = new ShapeDrawingButtonDecorator(button, shapePrototype, 0.5, 4);
        } else {
            Image icon = ImageLoader.loadImage(iconPath);
            if (icon != null)
                button = new ImageDecorator(button, icon, 24, 24, 8, ImageDecorator.ImageMode.ICON_ONLY);
            else
                System.err.println("[ToolbarPanel] Warning: Icon not found: " + iconPath);
        }
        button = new TooltipDecorator(button, tooltipText);
        if (targetWhiteBoard != null && prototypeRegistry != null) {
            button = new ShapeCreationButtonDecorator(button, targetWhiteBoard, prototypeRegistry, shapeTypeKey);
        } else {
            return null;
        }
        // Mapping is done when calling addButton(button, key)
        return button;
    }

    private IButton createCompositeButton(int x, int y, String iconPath, String tooltipText, String groupKey) {
        System.out.println("[COMPOSITE DEBUG] createCompositeButton called for key: " + groupKey);
        IButton button = new CustomButton(x, y, 40, 40, "");
        ShapeGroup groupPrototype = null;
        if (compositeRegistry != null && compositeRegistry.hasPrototype(groupKey)) {
            try {
                groupPrototype = compositeRegistry.createGroup(groupKey, 0, 0);
                System.out.println(
                        "[COMPOSITE DEBUG] Successfully created group prototype from registry for key: " + groupKey);
            } catch (Exception e) {
                System.err.println("[COMPOSITE DEBUG] Error getting group prototype: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("[COMPOSITE DEBUG] Registry check failed: Registry is "
                    + (compositeRegistry == null ? "null" : "not null") + ", hasPrototype="
                    + (compositeRegistry != null && compositeRegistry.hasPrototype(groupKey)));
        }

        if (groupPrototype != null) {
            button = new ShapeDrawingButtonDecorator(button, groupPrototype, 0.5, 4);
        } else {
            Image icon = ImageLoader.loadImage(iconPath);
            if (icon != null)
                button = new ImageDecorator(button, icon, 24, 24, 8, ImageDecorator.ImageMode.ICON_ONLY);
            else
                System.err.println("[ToolbarPanel] Warning: Group icon not found: " + iconPath);
        }
        button = new TooltipDecorator(button, tooltipText);
        if (targetWhiteBoard != null && compositeRegistry != null) {
            DraggableCompositeShapeCreationButtonDecorator compositeBtn = new DraggableCompositeShapeCreationButtonDecorator(
                    button, targetWhiteBoard, compositeRegistry, groupKey);
            if (dragMediator != null)
                compositeBtn.setDragMediator(dragMediator);
            button = compositeBtn;
        } else {
            return null;
        }
        // Mapping is done when calling addButton(button, key)
        return button;
    }

    // --- Memento Pattern Implementation ---

    public ToolbarMemento createMemento() {
        // Get the values (String prototype keys) from the map.
        List<String> keys = new ArrayList<>(buttonToPrototypeKeyMap.values());
        System.out.println("[LOG] ToolbarPanel.createMemento() - Keys being saved: " + keys);
        return new ToolbarMemento(keys);
    }

    public void restoreFromMemento(ToolbarMemento memento) {
        if (memento == null) {
            System.err.println("[ToolbarPanel] Cannot restore from null memento.");
            return;
        }
        System.out.println("[LOG] ToolbarPanel.restoreFromMemento() - START");
        List<String> keysToRestore = memento.getButtonPrototypeKeys();
        System.out.println(
                "[LOG] ToolbarPanel.restoreFromMemento() - Keys received from ToolbarMemento: " + keysToRestore);

        // Clear existing dynamic buttons (using super.removeButton to avoid map issues
        // during clear)
        List<IButton> buttonsToRemove = new ArrayList<>(this.buttons); // Use list from superclass
        System.out.println("[ToolbarPanel] Removing " + buttonsToRemove.size() + " existing dynamic buttons.");
        for (IButton btn : buttonsToRemove) {
            super.removeButton(btn); // Remove from superclass list directly
        }
        buttonToPrototypeKeyMap.clear(); // Clear the map
        nextButtonY = BUTTON_Y_START; // Reset layout position

        // Log the state of the registries the ToolbarPanel instance knows about just
        // before checking
        System.out.println(
                "[LOG] ToolbarPanel.restoreFromMemento() - Checking internal registry references before loop:");
        System.out.println("[LOG]   this.prototypeRegistry is null: " + (this.prototypeRegistry == null));
        if (this.prototypeRegistry != null) {
            // Assuming ShapePrototypeRegistry has a method to get keys or check existence
            // For now, just re-check the specific keys we expect.
            System.out.println("[LOG]   this.prototypeRegistry.hasPrototype('Rectangle'): "
                    + this.prototypeRegistry.hasPrototype("Rectangle"));
            System.out.println("[LOG]   this.prototypeRegistry.hasPrototype('Polygon'): "
                    + this.prototypeRegistry.hasPrototype("Polygon"));
        }
        System.out.println("[LOG]   this.compositeRegistry is null: " + (this.compositeRegistry == null));
        if (this.compositeRegistry != null) {
            // Use getPrototypesMap() which we know exists
            System.out.println(
                    "[LOG]   this.compositeRegistry keys: " + this.compositeRegistry.getPrototypesMap().keySet());
        }

        // Recreate buttons from memento keys
        System.out.println("[LOG] ToolbarPanel.restoreFromMemento() - Recreating buttons from restored keys...");
        for (String key : keysToRestore) {
            IButton newButton = null;
            System.out.println("[LOG] ToolbarPanel.restoreFromMemento() - Processing key: " + key);
            if (key.startsWith("composite_")) {
                boolean hasKey = (compositeRegistry != null && compositeRegistry.hasPrototype(key));
                System.out.println("[LOG] ToolbarPanel.restoreFromMemento() - Is composite key. Registry has key '"
                        + key + "': " + hasKey);
                if (hasKey) {
                    newButton = createCompositeButton(BUTTON_X_MARGIN, nextButtonY, "icons/group.png",
                            "Restored Composite", key);
                } else {
                    System.err.println(
                            "[LOG] ToolbarPanel.restoreFromMemento() - Warning: Composite prototype not found for key: "
                                    + key);
                }
            } else {
                boolean hasKey = (prototypeRegistry != null && prototypeRegistry.hasPrototype(key));
                System.out.println("[LOG] ToolbarPanel.restoreFromMemento() - Is standard key. Registry has key '" + key
                        + "': " + hasKey);
                if (hasKey) {
                    String iconPath = "icons/" + key.toLowerCase() + ".png";
                    newButton = createDraggableShapeButton(BUTTON_X_MARGIN, nextButtonY, iconPath, "Restored " + key,
                            key);
                } else {
                    System.err.println(
                            "[LOG] ToolbarPanel.restoreFromMemento() - Warning: Standard prototype not found for key: "
                                    + key);
                }
            }

            if (newButton != null) {
                // Use the specific addButton overload that correctly populates the map
                this.addButton(newButton, key); // This method already logs the addition
                nextButtonY += newButton.getHeight() + BUTTON_Y_SPACING;
                System.out.println(
                        "[LOG] ToolbarPanel.restoreFromMemento() - Successfully recreated and added button for key: "
                                + key);
            } else {
                System.err
                        .println("[LOG] ToolbarPanel.restoreFromMemento() - Failed to recreate button for key: " + key);
            }
        }
        System.out.println(
                "[LOG] ToolbarPanel.restoreFromMemento() - END. Final button count: " + buttons.size());
        repaint();
    }
}
