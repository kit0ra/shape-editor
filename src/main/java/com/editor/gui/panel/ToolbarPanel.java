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

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.DraggableCompositeShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.ShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ShapeDrawingButtonDecorator;
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.gui.button.factory.CompositeButtonFactory;
import com.editor.gui.button.factory.ShapeButtonFactory;
import com.editor.gui.button.manager.ButtonManager;
import com.editor.mediator.DragMediator;
import com.editor.memento.ToolbarMemento;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.shapes.processing.CompositeShapeProcessor;
import com.editor.shapes.processing.ProcessingResult;
import com.editor.shapes.processing.SingleShapeProcessor;
import com.editor.state.EditorStateManager;
import com.editor.state.StateChangeListener;
import com.editor.utils.ImageLoader;

/**
 * A panel that serves as a toolbar for additional tools and functionality.
 * This panel is positioned between the shapes panel and the trash panel.
 */
public class ToolbarPanel extends CustomPanel {

    private static final Color NORMALCOLOR = Color.decode("#F6E9D7");
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);

    private boolean isShapeOverToolbar = false;
    private WhiteBoard targetWhiteBoard;
    // Mediator field in CustomPanel (superclass) is protected now
    // private DragMediator dragMediator;
    private ShapePrototypeRegistry prototypeRegistry;
    private CompositeShapePrototypeRegistry compositeRegistry;

    // Button factories and processors
    private ShapeButtonFactory shapeButtonFactory;
    private CompositeButtonFactory compositeButtonFactory;
    private SingleShapeProcessor singleShapeProcessor;
    private CompositeShapeProcessor compositeShapeProcessor;
    private final ButtonManager buttonManager;

    // State management
    private final EditorStateManager stateManager;

    // Button layout constants
    private static final int BUTTON_Y_START = 30;
    private static final int BUTTON_X_MARGIN = 10;
    private static final int BUTTON_Y_SPACING = 5;
    private int nextButtonY = BUTTON_Y_START;

    // Store mapping from button instance to its prototype key for memento
    // Use the 'buttons' list from CustomPanel and this map in parallel
    private final Map<IButton, String> buttonToPrototypeKeyMap = new HashMap<>();

    // Colors for visual feedback
    private final Color hoverColor = new Color(220, 240, 220);

    public ToolbarPanel() {
        super();
        setBackground(NORMALCOLOR);

        // Initialize button manager
        buttonManager = new ButtonManager();

        // Initialize state manager
        stateManager = new EditorStateManager();
    }

    @Override
    public void paint(Graphics g) {
        setBackground(isShapeOverToolbar ? hoverColor : NORMALCOLOR);
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(TITLE_FONT);
        g2d.setColor(Color.decode("#F6E9D7"));
    }

    @Override
    public void setTargetWhiteBoard(WhiteBoard whiteBoard) {
        this.targetWhiteBoard = whiteBoard;

        // Initialize factories and processors when whiteboard is set
        if (whiteBoard != null) {
            if (prototypeRegistry != null) {
                shapeButtonFactory = new ShapeButtonFactory(whiteBoard, prototypeRegistry,
                        whiteBoard.getCommandHistory());
                singleShapeProcessor = new SingleShapeProcessor(shapeButtonFactory, prototypeRegistry);
            }

            if (compositeRegistry != null) {
                compositeButtonFactory = new CompositeButtonFactory(whiteBoard, compositeRegistry,
                        whiteBoard.getCommandHistory());
                compositeShapeProcessor = new CompositeShapeProcessor(compositeButtonFactory, compositeRegistry);
            }

            // Set drag mediator for factories if available
            if (this.dragMediator != null) {
                if (shapeButtonFactory != null) {
                    shapeButtonFactory.setDragMediator(this.dragMediator);
                }
                if (compositeButtonFactory != null) {
                    compositeButtonFactory.setDragMediator(this.dragMediator);
                }
            }
        }
    }

    public void setPrototypeRegistry(ShapePrototypeRegistry registry) {
        this.prototypeRegistry = registry;

        // Update factory and processor if whiteboard is already set
        if (targetWhiteBoard != null && registry != null) {
            shapeButtonFactory = new ShapeButtonFactory(targetWhiteBoard, registry,
                    targetWhiteBoard.getCommandHistory());
            singleShapeProcessor = new SingleShapeProcessor(shapeButtonFactory, registry);

            // Set drag mediator if available
            if (this.dragMediator != null && shapeButtonFactory != null) {
                shapeButtonFactory.setDragMediator(this.dragMediator);
            }
        }
    }

    public void setCompositePrototypeRegistry(CompositeShapePrototypeRegistry registry) {
        this.compositeRegistry = registry;
        System.out.println("[ToolbarPanel] CompositeShapePrototypeRegistry set: " + (registry != null));

        // Update factory and processor if whiteboard is already set
        if (targetWhiteBoard != null && registry != null) {
            compositeButtonFactory = new CompositeButtonFactory(targetWhiteBoard, registry,
                    targetWhiteBoard.getCommandHistory());
            compositeShapeProcessor = new CompositeShapeProcessor(compositeButtonFactory, registry);

            // Set drag mediator if available
            if (this.dragMediator != null && compositeButtonFactory != null) {
                compositeButtonFactory.setDragMediator(this.dragMediator);
            }
        }
    }

    /**
     * Sets the state change listener for this toolbar panel.
     * The listener will be notified when significant state changes occur.
     *
     * @param listener The state change listener
     */
    public void setStateChangeListener(StateChangeListener listener) {
        // Set the listener for the button manager
        buttonManager.setStateChangeListener(listener);

        // Add the listener to the state manager
        if (listener != null) {
            stateManager.addListener(listener);
        }
    }

    @Override
    public void setDragMediator(DragMediator mediator) {
        super.setDragMediator(mediator); // Registers as CustomPanel
        if (mediator != null) {
            mediator.registerToolbarPanel(this); // Registers specifically as ToolbarPanel
            System.out.println("[ToolbarPanel] Registered with mediator (as ToolbarPanel and CustomPanel).");

            // Set mediator for factories if available
            if (shapeButtonFactory != null) {
                shapeButtonFactory.setDragMediator(mediator);
            }
            if (compositeButtonFactory != null) {
                compositeButtonFactory.setDragMediator(mediator);
            }
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

        // Check if processors are initialized
        if (singleShapeProcessor == null || compositeShapeProcessor == null) {
            System.err.println("[ToolbarPanel] Cannot add shapes: Processors not initialized.");
            return false;
        }

        List<Shape> selectedShapes = targetWhiteBoard.getSelectedShapes();
        if (selectedShapes.isEmpty()) {
            System.out.println("[ToolbarPanel] No shapes selected to add.");
            return false;
        }

        System.out.println("[ToolbarPanel] Processing " + selectedShapes.size() + " selected shape(s) for toolbar.");
        ProcessingResult result;

        if (selectedShapes.size() == 1 && !(selectedShapes.get(0) instanceof ShapeGroup)) {
            // Handle single shape using SingleShapeProcessor
            Shape singleShape = selectedShapes.get(0);
            result = singleShapeProcessor.processShape(singleShape, BUTTON_X_MARGIN, nextButtonY);
        } else {
            // Handle group or multiple shapes using CompositeShapeProcessor
            result = compositeShapeProcessor.processShapes(selectedShapes, BUTTON_X_MARGIN, nextButtonY);
        }

        if (result != null && result.getButton() != null) {
            // Add the button using ButtonManager
            boolean added = buttonManager.addButton(result.getButton(), result.getPrototypeKey());
            if (added) {
                // Add the button to the panel
                super.addButton(result.getButton());

                // Store the mapping for memento
                buttonToPrototypeKeyMap.put(result.getButton(), result.getPrototypeKey());

                // Update next button position
                nextButtonY += result.getButton().getHeight() + BUTTON_Y_SPACING;

                System.out.println("[ToolbarPanel] Added button for key: " + result.getPrototypeKey());
                targetWhiteBoard.clearSelection();
                targetWhiteBoard.repaint();
                repaint();

                // Register state change with the state manager
                if (stateManager != null) {
                    stateManager.registerStateChange("ToolbarPanel", "AddShapes", result.getPrototypeKey());
                }

                return true;
            }
        }
        return false;
    }

    // Use this specific method to add buttons AND store their keys
    private void addButton(IButton button, String prototypeKey) {
        super.addButton(button); // Adds to CustomPanel's list and repaints
        buttonToPrototypeKeyMap.put(button, prototypeKey);
        System.out.println("[ToolbarPanel] Stored mapping for button with key: " + prototypeKey);

        // Register state change with the state manager
        if (stateManager != null) {
            stateManager.registerStateChange("ToolbarPanel", "AddButton", prototypeKey);
        }
    }

    @Override
    public boolean removeButton(IButton button) {
        boolean removed = super.removeButton(button); // Removes from CustomPanel's list and repaints
        if (removed) {
            String removedKey = buttonToPrototypeKeyMap.remove(button);
            System.out.println("[ToolbarPanel] Removed mapping for button with key: " + removedKey);

            // Remove from button manager
            if (buttonManager != null) {
                buttonManager.removeButton(button);
            }

            recalculateButtonLayout(); // Adjust layout after removal

            // Register state change with the state manager
            if (stateManager != null) {
                stateManager.registerStateChange("ToolbarPanel", "RemoveButton", removedKey);
            }
        }
        return removed;
    }

    // Implement a proper layout recalculation using ButtonManager
    private void recalculateButtonLayout() {
        System.out.println("[ToolbarPanel] Recalculating button layout...");

        // Use ButtonManager to recalculate positions
        if (buttonManager != null) {
            buttonManager.recalculateButtonPositions();
            nextButtonY = buttonManager.getNextYPosition();
        } else {
            // Fallback to old implementation if ButtonManager is not available
            nextButtonY = BUTTON_Y_START;
            List<IButton> currentButtons = new ArrayList<>(this.buttons); // Get current buttons from superclass
            this.buttons.clear(); // Clear superclass list
            Map<IButton, String> currentMap = new HashMap<>(this.buttonToPrototypeKeyMap); // Copy map
            this.buttonToPrototypeKeyMap.clear(); // Clear map

            for (IButton button : currentButtons) {
                String key = currentMap.get(button); // Find the key for this button
                if (key != null) {
                    System.out.println("[ToolbarPanel] Re-adding button for key: " + key);
                    this.addButton(button, key); // Re-add using the method that updates the map
                    nextButtonY += button.getHeight() + BUTTON_Y_SPACING;
                } else {
                    System.err.println(
                            "[ToolbarPanel] Warning: Could not find key for button during re-layout: " + button);
                }
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

    @SuppressWarnings("CallToPrintStackTrace")
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
                System.out.println("[LOG] ToolbarPanel.restoreFromMemento() - Processing key: '" + key
                        + "', Registry has key: " + hasKey);

                if (hasKey) {
                    // Extract the base shape type from the key (e.g., "Rectangle_UUID" ->
                    // "Rectangle")
                    String baseShapeType = key;
                    if (key.contains("_")) {
                        baseShapeType = key.substring(0, key.indexOf("_"));
                    }

                    // Get the icon path based on the base shape type
                    String iconPath = "icons/" + baseShapeType.toLowerCase() + ".png";
                    System.out.println("[LOG] ToolbarPanel.restoreFromMemento() - Using icon path: " + iconPath
                            + " for key: " + key);

                    // Create the button with the original key to preserve custom properties
                    newButton = createDraggableShapeButton(BUTTON_X_MARGIN, nextButtonY, iconPath,
                            "Restored " + baseShapeType, key);
                } else {
                    System.err.println(
                            "[LOG] ToolbarPanel.restoreFromMemento() - Warning: Prototype not found for key: "
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
