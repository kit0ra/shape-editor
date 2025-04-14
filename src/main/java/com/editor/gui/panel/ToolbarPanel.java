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
    
    
    private ShapePrototypeRegistry prototypeRegistry;
    private CompositeShapePrototypeRegistry compositeRegistry;

    
    private ShapeButtonFactory shapeButtonFactory;
    private CompositeButtonFactory compositeButtonFactory;
    private SingleShapeProcessor singleShapeProcessor;
    private CompositeShapeProcessor compositeShapeProcessor;
    private final ButtonManager buttonManager;

    
    private final EditorStateManager stateManager;

    
    private static final int BUTTON_Y_START = 30;
    private static final int BUTTON_X_MARGIN = 10;
    private static final int BUTTON_Y_SPACING = 5;
    private int nextButtonY = BUTTON_Y_START;

    
    
    private final Map<IButton, String> buttonToPrototypeKeyMap = new HashMap<>();

    
    private final Color hoverColor = new Color(220, 240, 220);

    public ToolbarPanel() {
        super();
        setBackground(NORMALCOLOR);

        
        buttonManager = new ButtonManager();

        
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

        
        if (targetWhiteBoard != null && registry != null) {
            shapeButtonFactory = new ShapeButtonFactory(targetWhiteBoard, registry,
                    targetWhiteBoard.getCommandHistory());
            singleShapeProcessor = new SingleShapeProcessor(shapeButtonFactory, registry);

            
            if (this.dragMediator != null && shapeButtonFactory != null) {
                shapeButtonFactory.setDragMediator(this.dragMediator);
            }
        }
    }

    public void setCompositePrototypeRegistry(CompositeShapePrototypeRegistry registry) {
        this.compositeRegistry = registry;
        System.out.println("[ToolbarPanel] CompositeShapePrototypeRegistry set: " + (registry != null));

        
        if (targetWhiteBoard != null && registry != null) {
            compositeButtonFactory = new CompositeButtonFactory(targetWhiteBoard, registry,
                    targetWhiteBoard.getCommandHistory());
            compositeShapeProcessor = new CompositeShapeProcessor(compositeButtonFactory, registry);

            
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
        
        buttonManager.setStateChangeListener(listener);

        
        if (listener != null) {
            stateManager.addListener(listener);
        }
    }

    @Override
    public void setDragMediator(DragMediator mediator) {
        super.setDragMediator(mediator); 
        if (mediator != null) {
            mediator.registerToolbarPanel(this); 
            System.out.println("[ToolbarPanel] Registered with mediator (as ToolbarPanel and CustomPanel).");

            
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
            
            Shape singleShape = selectedShapes.get(0);
            result = singleShapeProcessor.processShape(singleShape, BUTTON_X_MARGIN, nextButtonY);
        } else {
            
            result = compositeShapeProcessor.processShapes(selectedShapes, BUTTON_X_MARGIN, nextButtonY);
        }

        if (result != null && result.getButton() != null) {
            
            boolean added = buttonManager.addButton(result.getButton(), result.getPrototypeKey());
            if (added) {
                
                super.addButton(result.getButton());

                
                buttonToPrototypeKeyMap.put(result.getButton(), result.getPrototypeKey());

                
                nextButtonY += result.getButton().getHeight() + BUTTON_Y_SPACING;

                System.out.println("[ToolbarPanel] Added button for key: " + result.getPrototypeKey());
                targetWhiteBoard.clearSelection();
                targetWhiteBoard.repaint();
                repaint();

                
                if (stateManager != null) {
                    stateManager.registerStateChange("ToolbarPanel", "AddShapes", result.getPrototypeKey());
                }

                return true;
            }
        }
        return false;
    }

    
    private void addButton(IButton button, String prototypeKey) {
        super.addButton(button); 
        buttonToPrototypeKeyMap.put(button, prototypeKey);
        System.out.println("[ToolbarPanel] Stored mapping for button with key: " + prototypeKey);

        
        if (stateManager != null) {
            stateManager.registerStateChange("ToolbarPanel", "AddButton", prototypeKey);
        }
    }

    @Override
    public boolean removeButton(IButton button) {
        boolean removed = super.removeButton(button); 
        if (removed) {
            String removedKey = buttonToPrototypeKeyMap.remove(button);
            System.out.println("[ToolbarPanel] Removed mapping for button with key: " + removedKey);

            
            if (buttonManager != null) {
                buttonManager.removeButton(button);
            }

            recalculateButtonLayout(); 

            
            if (stateManager != null) {
                stateManager.registerStateChange("ToolbarPanel", "RemoveButton", removedKey);
            }
        }
        return removed;
    }

    
    private void recalculateButtonLayout() {
        System.out.println("[ToolbarPanel] Recalculating button layout...");

        
        if (buttonManager != null) {
            buttonManager.recalculateButtonPositions();
            nextButtonY = buttonManager.getNextYPosition();
        } else {
            
            nextButtonY = BUTTON_Y_START;
            List<IButton> currentButtons = new ArrayList<>(this.buttons); 
            this.buttons.clear(); 
            Map<IButton, String> currentMap = new HashMap<>(this.buttonToPrototypeKeyMap); 
            this.buttonToPrototypeKeyMap.clear(); 

            for (IButton button : currentButtons) {
                String key = currentMap.get(button); 
                if (key != null) {
                    System.out.println("[ToolbarPanel] Re-adding button for key: " + key);
                    this.addButton(button, key); 
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

        
        if (customShapePrototype != null) {
            shapePrototype = customShapePrototype;
        }
        
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
        
        return button;
    }

    

    public ToolbarMemento createMemento() {
        
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

        
        
        List<IButton> buttonsToRemove = new ArrayList<>(this.buttons); 
        System.out.println("[ToolbarPanel] Removing " + buttonsToRemove.size() + " existing dynamic buttons.");
        for (IButton btn : buttonsToRemove) {
            super.removeButton(btn); 
        }
        buttonToPrototypeKeyMap.clear(); 
        nextButtonY = BUTTON_Y_START; 

        
        
        System.out.println(
                "[LOG] ToolbarPanel.restoreFromMemento() - Checking internal registry references before loop:");
        System.out.println("[LOG]   this.prototypeRegistry is null: " + (this.prototypeRegistry == null));
        if (this.prototypeRegistry != null) {
            
            
            System.out.println("[LOG]   this.prototypeRegistry.hasPrototype('Rectangle'): "
                    + this.prototypeRegistry.hasPrototype("Rectangle"));
            System.out.println("[LOG]   this.prototypeRegistry.hasPrototype('Polygon'): "
                    + this.prototypeRegistry.hasPrototype("Polygon"));
        }
        System.out.println("[LOG]   this.compositeRegistry is null: " + (this.compositeRegistry == null));
        if (this.compositeRegistry != null) {
            
            System.out.println(
                    "[LOG]   this.compositeRegistry keys: " + this.compositeRegistry.getPrototypesMap().keySet());
        }

        
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
                    
                    
                    String baseShapeType = key;
                    if (key.contains("_")) {
                        baseShapeType = key.substring(0, key.indexOf("_"));
                    }

                    
                    String iconPath = "icons/" + baseShapeType.toLowerCase() + ".png";
                    System.out.println("[LOG] ToolbarPanel.restoreFromMemento() - Using icon path: " + iconPath
                            + " for key: " + key);

                    
                    newButton = createDraggableShapeButton(BUTTON_X_MARGIN, nextButtonY, iconPath,
                            "Restored " + baseShapeType, key);
                } else {
                    System.err.println(
                            "[LOG] ToolbarPanel.restoreFromMemento() - Warning: Prototype not found for key: "
                                    + key);
                }
            }

            if (newButton != null) {
                
                this.addButton(newButton, key); 
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
