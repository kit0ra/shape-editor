package com.editor.mediator;

import java.awt.Component; 
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.button.IButton; 
import com.editor.gui.panel.CustomPanel;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.gui.panel.TrashPanel;

/**
 * Concrete implementation of the DragMediator interface.
 * This class mediates drag operations between panels and the whiteboard.
 */
public class ShapeDragMediator implements DragMediator {
    private final List<CustomPanel> panels = new ArrayList<>(); 
    private WhiteBoard whiteBoard;
    private Draggable currentDraggable;
    private Object sourceComponentForDrag; 
    private boolean isDragging = false;
    private boolean debugEnabled = false;
    private TrashPanel trashPanel = null;
    private ToolbarPanel toolbarPanel = null;

    @Override
    public void registerPanel(CustomPanel panel) {
        if (!panels.contains(panel)) {
            panels.add(panel);
            debugLog("Panel registered with mediator: " + panel.getClass().getSimpleName());
        }
    }

    @Override
    public void registerWhiteBoard(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
        debugLog("WhiteBoard registered with mediator");
    }

    @Override
    public void startDrag(Object sourceComponent, Draggable draggable, int x, int y) { 
        if (whiteBoard == null) {
            debugLog("ERROR: Cannot start drag - WhiteBoard not registered");
            return;
        }
        if (!(sourceComponent instanceof Component)) { 
            debugLog("ERROR: Cannot start drag - sourceComponent is not a valid Component");
            return;
        }

        currentDraggable = draggable;
        sourceComponentForDrag = sourceComponent; 
        isDragging = true;

        
        
        
        if (sourceComponent != whiteBoard) {
            whiteBoard.setPrototypeRegistry(whiteBoard.getPrototypeRegistry()); 
                                                                                
            whiteBoard.setCurrentShapeType(draggable.getShapeType());
        }

        
        draggable.startDrag(x, y); 

        debugLog("Started drag operation from " + sourceComponent.getClass().getSimpleName() + ": "
                + draggable.getShapeType() + " at (" + x + ", " + y + ")");

        
        ((Component) sourceComponent).repaint();
    }

    @Override
    public void drag(int x, int y) {
        if (!isDragging || currentDraggable == null) {
            return;
        }

        
        currentDraggable.drag(x, y);

        
        
        Point screenPoint = null;
        if (sourceComponentForDrag instanceof Component) {
            Component sourceComp = (Component) sourceComponentForDrag;
            try {
                screenPoint = new Point(x, y);
                
                Point sourceLocation = sourceComp.getLocationOnScreen();
                screenPoint.translate(sourceLocation.x, sourceLocation.y);
            } catch (IllegalComponentStateException e) {
                debugLog("Error converting coordinates (source component not showing?): " + e.getMessage());
                screenPoint = null; 
            } catch (Exception e) {
                debugLog("Unexpected error converting coordinates: " + e.getMessage());
                screenPoint = null; 
            }
        }

        
        if (trashPanel != null && screenPoint != null) {
            try {
                
                boolean isOverTrash = trashPanel.isPointOverTrash(screenPoint);
                trashPanel.setShapeOverTrash(isOverTrash);
                
                if (sourceComponentForDrag == whiteBoard) {
                    whiteBoard.setDraggingToTrash(isOverTrash);
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over trash: " + e.getMessage());
            }
        }

        
        if (toolbarPanel != null && screenPoint != null) {
            try {
                
                boolean isOverToolbar = toolbarPanel.isPointOverToolbar(screenPoint); 
                toolbarPanel.setShapeOverToolbar(isOverToolbar); 
                
                if (sourceComponentForDrag == whiteBoard) {
                    whiteBoard.setDraggingToToolbar(isOverToolbar);
                    
                    if (isOverToolbar)
                        whiteBoard.setDraggingToTrash(false);
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over toolbar: " + e.getMessage());
            }
        }

        debugLog("Dragging: " + currentDraggable.getShapeType() + " at source coords (" + x + ", " + y + ")");

        
        if (sourceComponentForDrag instanceof Component) {
            ((Component) sourceComponentForDrag).repaint();
        }
        
        if (sourceComponentForDrag == whiteBoard) {
            whiteBoard.repaint();
        }
    }

    @Override
    public void endDrag(int x, int y) {
        debugLog(">>> ShapeDragMediator.endDrag entered with source coords: (" + x + ", " + y + ")");
        if (!isDragging || currentDraggable == null || sourceComponentForDrag == null) {
            debugLog(">>> ShapeDragMediator.endDrag exiting early: isDragging=" + isDragging + ", currentDraggable="
                    + (currentDraggable != null) + ", sourceComponentForDrag=" + (sourceComponentForDrag != null));
            return;
        }

        
        
        Point screenPoint = null;
        if (sourceComponentForDrag instanceof Component) {
            Component sourceComp = (Component) sourceComponentForDrag;
            try {
                screenPoint = new Point(x, y);
                
                Point sourceLocation = sourceComp.getLocationOnScreen();
                screenPoint.translate(sourceLocation.x, sourceLocation.y);
                debugLog("Calculated screenPoint for endDrag: (" + screenPoint.x + ", " + screenPoint.y + ")");
            } catch (IllegalComponentStateException e) {
                debugLog("Error converting coordinates for endDrag (source component not showing?): " + e.getMessage());
                screenPoint = null;
            } catch (Exception e) {
                debugLog("Unexpected error converting coordinates for endDrag: " + e.getMessage());
                screenPoint = null;
            }
        }

        boolean deletedShape = false;
        boolean addedToToolbar = false;

        
        if (trashPanel != null && screenPoint != null) {
            try {
                if (trashPanel.isPointOverTrash(screenPoint)) {
                    debugLog("Ending drag over trash panel - deleting shape(s)");
                    
                    if (sourceComponentForDrag == whiteBoard) {
                        
                        deletedShape = true; 
                        debugLog("Drag from WhiteBoard ended over trash. WhiteBoard will handle deletion.");
                    } else if (sourceComponentForDrag instanceof CustomPanel && currentDraggable instanceof IButton) {
                        
                        debugLog("Drag from CustomPanel ended over trash. Attempting to remove button.");
                        CustomPanel sourcePanel = (CustomPanel) sourceComponentForDrag;
                        IButton draggedButton = (IButton) currentDraggable;
                        boolean removed = sourcePanel.removeButton(draggedButton);
                        if (removed) {
                            debugLog("Button removed successfully from source panel.");
                            deletedShape = true; 
                        } else {
                            debugLog("Failed to remove button from source panel.");
                            currentDraggable.endDrag(-1, -1); 
                            deletedShape = true; 
                        }
                    } else {
                        
                        deletedShape = true; 
                        currentDraggable.endDrag(-1, -1); 
                        debugLog("Drag from unknown source/type ended over trash. Cancelling drop.");
                    }
                    trashPanel.setShapeOverTrash(false); 
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over trash: " + e.getMessage());
            }
        }

        
        if (toolbarPanel != null && screenPoint != null && !deletedShape) {
            try {
                
                if (toolbarPanel.isPointOverToolbar(screenPoint)) { 
                    debugLog("Ending drag over toolbar panel - adding shape(s) to toolbar");
                    
                    if (sourceComponentForDrag == whiteBoard) {
                        
                        addedToToolbar = true; 
                    } else {
                        
                        addedToToolbar = true; 
                        currentDraggable.endDrag(-1, -1); 
                        debugLog("Drag from external source ended over toolbar. Cancelling drop.");
                    }
                    toolbarPanel.setShapeOverToolbar(false); 
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over toolbar: " + e.getMessage());
            }
        }

        
        if (!deletedShape && !addedToToolbar) {
            
            Point whiteboardPoint = null;
            if (x >= 0 && y >= 0 && sourceComponentForDrag instanceof Component) { 
                
                whiteboardPoint = convertToWhiteboardCoordinates(sourceComponentForDrag, new Point(x, y));
            } else {
                debugLog(
                        "Cannot convert to whiteboard coords: Invalid drop coords or sourceComponent is not a Component.");
            }

            if (whiteboardPoint != null) {
                
                if (isPointOverWhiteboard(screenPoint)) {
                    debugLog("Ending drag on whiteboard at (" + whiteboardPoint.x + ", " + whiteboardPoint.y + ")");
                    
                    currentDraggable.endDrag(whiteboardPoint.x, whiteboardPoint.y);
                } else {
                    debugLog("Drop point is not over whiteboard. Cancelling.");
                    currentDraggable.endDrag(-1, -1); 
                }

            } else {
                debugLog("Cancelling drag operation (not convertible to whiteboard coords or invalid drop)");
                
                currentDraggable.endDrag(-1, -1);
            }
        } else if (sourceComponentForDrag == whiteBoard) {
            
            
            debugLog("Calling WhiteBoard's endDrag after delete/toolbar add.");
            whiteBoard.endDrag(x, y); 
        }

        
        
        if (sourceComponentForDrag != whiteBoard) {
            whiteBoard.setCurrentShapeType(null);
        }

        
        isDragging = false;
        currentDraggable = null;

        
        resetTrashPanelState();
        
        if (toolbarPanel != null)
            toolbarPanel.setShapeOverToolbar(false);
        
        if (whiteBoard != null) {
            whiteBoard.setDraggingToTrash(false);
            whiteBoard.setDraggingToToolbar(false);
        }

        
        if (sourceComponentForDrag instanceof Component) {
            ((Component) sourceComponentForDrag).repaint();
        }
        
        if (sourceComponentForDrag != whiteBoard && whiteBoard != null) {
            whiteBoard.repaint();
        }

        sourceComponentForDrag = null; 
    }

    @Override
    
    @SuppressWarnings("CallToPrintStackTrace")
    public Point convertToWhiteboardCoordinates(Object sourceComponent, Point sourcePoint) {
        if (whiteBoard == null || sourceComponent == null || !(sourceComponent instanceof Component)) {
            debugLog("ERROR: Cannot convert coordinates - WhiteBoard or sourceComponent invalid/not registered");
            return null;
        }

        Component sourceComp = (Component) sourceComponent; 

        try {
            
            Point screenPoint = new Point(sourcePoint);
            Point sourceLocation = sourceComp.getLocationOnScreen(); 
            screenPoint.translate(sourceLocation.x, sourceLocation.y);

            debugLog("Input sourcePoint: (" + sourcePoint.x + ", " + sourcePoint.y + ") from "
                    + sourceComp.getClass().getSimpleName());
            debugLog("Calculated screenPoint: (" + screenPoint.x + ", " + screenPoint.y + ")");

            
            Point whiteboardLocation = whiteBoard.getLocationOnScreen();
            int wbX = whiteboardLocation.x;
            int wbY = whiteboardLocation.y;
            int wbWidth = whiteBoard.getWidth();
            int wbHeight = whiteBoard.getHeight();
            debugLog(
                    "Whiteboard screen location: (" + wbX + ", " + wbY + "), Size: (" + wbWidth + "x" + wbHeight + ")");

            
            
            
            int margin = 10; 
            boolean withinX = screenPoint.x >= wbX - margin && screenPoint.x < wbX + wbWidth + margin;
            boolean withinY = screenPoint.y >= wbY - margin && screenPoint.y < wbY + wbHeight + margin;
            debugLog("Bounds check: withinX=" + withinX + " (Range: " + (wbX - margin) + " to "
                    + (wbX + wbWidth + margin) + ")");
            debugLog("Bounds check: withinY=" + withinY + " (Range: " + (wbY - margin) + " to "
                    + (wbY + wbHeight + margin) + ")");

            if (withinX && withinY) {
                debugLog("Point IS within whiteboard bounds (including margin).");
                
                Point whiteboardPoint = new Point(
                        screenPoint.x - wbX,
                        screenPoint.y - wbY);

                debugLog("Converted source point (" + sourcePoint.x + ", " + sourcePoint.y +
                        ") to whiteboard point (" + whiteboardPoint.x + ", " + whiteboardPoint.y + ")");

                return whiteboardPoint;
            } else {
                debugLog("Point IS outside whiteboard bounds (including margin). Returning null.");
                return null;
            }

        } catch (IllegalComponentStateException e) {
            debugLog(
                    "ERROR: IllegalComponentStateException during coordinate conversion (Source component not showing?): "
                            + e.getMessage());
            return null; 
        } catch (Exception e) {
            debugLog("ERROR: Unexpected Exception during coordinate conversion: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to check if a point in screen coordinates is over the
     * whiteboard.
     *
     * @param screenPoint Point in screen coordinates.
     * @return true if the point is over the whiteboard, false otherwise.
     */
    private boolean isPointOverWhiteboard(Point screenPoint) {
        if (whiteBoard == null || screenPoint == null) {
            return false;
        }
        try {
            Point wbLocation = whiteBoard.getLocationOnScreen();
            int wbX = wbLocation.x;
            int wbY = wbLocation.y;
            int wbWidth = whiteBoard.getWidth();
            int wbHeight = whiteBoard.getHeight();

            return screenPoint.x >= wbX && screenPoint.x < wbX + wbWidth &&
                    screenPoint.y >= wbY && screenPoint.y < wbY + wbHeight;
        } catch (IllegalComponentStateException e) {
            debugLog("Error checking whiteboard bounds (not showing?): " + e.getMessage());
            return false;
        }
    }

    @Override
    public void registerTrashPanel(TrashPanel trashPanel) {
        this.trashPanel = trashPanel;
        debugLog("Trash panel registered with mediator");
    }

    @Override
    public boolean checkPointOverTrash(Point screenPoint) {
        if (trashPanel == null) {
            debugLog("ERROR: Cannot check if point is over trash - TrashPanel not registered");
            return false;
        }

        boolean isOverTrash = trashPanel.isPointOverTrash(screenPoint);
        
        
        
        
        
        
        
        return isOverTrash;
    }

    @Override
    public void resetTrashPanelState() {
        if (trashPanel != null) {
            debugLog("Resetting trash panel visual state");
            trashPanel.setShapeOverTrash(false);
        }
    }

    @Override
    public void registerToolbarPanel(ToolbarPanel toolbarPanel) {
        this.toolbarPanel = toolbarPanel;
        debugLog("Toolbar panel registered with mediator");
    }

    @Override
    public boolean checkPointOverToolbar(Point screenPoint) {
        if (toolbarPanel == null) {
            debugLog("ERROR: Cannot check if point is over toolbar - ToolbarPanel not registered");
            return false;
        }
        
        boolean isOverToolbar = toolbarPanel.isPointOverToolbar(screenPoint);
        
        
        
        
        
        
        
        return isOverToolbar;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
        debugLog("Debug " + (enabled ? "enabled" : "disabled"));
    }

    

    @Override
    public boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public Object getSourceComponentForDrag() {
        return this.sourceComponentForDrag;
    }

    /**
     * Log a debug message if debug is enabled
     *
     * @param message The message to log
     */
    private void debugLog(String message) {
        if (debugEnabled) {
            System.out.println("[DragMediator] " + message);
        }
    }
}
