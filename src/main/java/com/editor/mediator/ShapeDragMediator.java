package com.editor.mediator;

import java.awt.Component; // Added
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.panel.CustomPanel;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.gui.panel.TrashPanel;

/**
 * Concrete implementation of the DragMediator interface.
 * This class mediates drag operations between panels and the whiteboard.
 */
public class ShapeDragMediator implements DragMediator {
    private List<CustomPanel> panels = new ArrayList<>(); // Keep track of panels specifically if needed
    private WhiteBoard whiteBoard;
    private Draggable currentDraggable;
    private Object sourceComponentForDrag; // Changed type to Object
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
    public void startDrag(Object sourceComponent, Draggable draggable, int x, int y) { // Changed param type
        if (whiteBoard == null) {
            debugLog("ERROR: Cannot start drag - WhiteBoard not registered");
            return;
        }
        if (!(sourceComponent instanceof Component)) { // Basic check
            debugLog("ERROR: Cannot start drag - sourceComponent is not a valid Component");
            return;
        }

        currentDraggable = draggable;
        sourceComponentForDrag = sourceComponent; // Store the source component
        isDragging = true;

        // Set up the whiteboard for the drag operation (if applicable)
        // This might need adjustment depending on whether the source is the whiteboard
        // itself
        if (sourceComponent != whiteBoard) {
            whiteBoard.setPrototypeRegistry(whiteBoard.getPrototypeRegistry()); // Assuming this is needed for external
                                                                                // drags
            whiteBoard.setCurrentShapeType(draggable.getShapeType());
        }

        // Notify the draggable that dragging has started
        draggable.startDrag(x, y); // Notify the draggable itself

        debugLog("Started drag operation from " + sourceComponent.getClass().getSimpleName() + ": "
                + draggable.getShapeType() + " at (" + x + ", " + y + ")");

        // Request repaint of the source component
        ((Component) sourceComponent).repaint();
    }

    @Override
    public void drag(int x, int y) {
        if (!isDragging || currentDraggable == null) {
            return;
        }

        // Update the draggable with the new position
        currentDraggable.drag(x, y);

        // Convert source component coordinates to screen coordinates for checking
        // panels
        Point screenPoint = null;
        if (sourceComponentForDrag instanceof Component) {
            Component sourceComp = (Component) sourceComponentForDrag;
            try {
                screenPoint = new Point(x, y);
                // Use the source component's location on screen
                Point sourceLocation = sourceComp.getLocationOnScreen();
                screenPoint.translate(sourceLocation.x, sourceLocation.y);
            } catch (IllegalComponentStateException e) {
                debugLog("Error converting coordinates (source component not showing?): " + e.getMessage());
                screenPoint = null; // Ensure screenPoint is null if conversion fails
            } catch (Exception e) {
                debugLog("Unexpected error converting coordinates: " + e.getMessage());
                screenPoint = null; // Ensure screenPoint is null if conversion fails
            }
        }

        // Check if the drag is over the trash panel
        if (trashPanel != null && screenPoint != null) {
            try {
                // Check if the point is over the trash panel
                boolean isOverTrash = trashPanel.isPointOverTrash(screenPoint);
                trashPanel.setShapeOverTrash(isOverTrash);
                // Update whiteboard state if dragging from whiteboard
                if (sourceComponentForDrag == whiteBoard) {
                    whiteBoard.setDraggingToTrash(isOverTrash);
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over trash: " + e.getMessage());
            }
        }

        // Check if the drag is over the toolbar panel
        if (toolbarPanel != null && screenPoint != null) {
            try {
                // Check if the point is over the toolbar panel
                boolean isOverToolbar = toolbarPanel.isPointOverToolbar(screenPoint);
                toolbarPanel.setShapeOverToolbar(isOverToolbar);
                // Update whiteboard state if dragging from whiteboard
                if (sourceComponentForDrag == whiteBoard) {
                    whiteBoard.setDraggingToToolbar(isOverToolbar);
                    // Ensure whiteboard doesn't think it's going to trash if it's going to toolbar
                    if (isOverToolbar)
                        whiteBoard.setDraggingToTrash(false);
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over toolbar: " + e.getMessage());
            }
        }

        debugLog("Dragging: " + currentDraggable.getShapeType() + " at source coords (" + x + ", " + y + ")");

        // Request repaint of the source component
        if (sourceComponentForDrag instanceof Component) {
            ((Component) sourceComponentForDrag).repaint();
        }
        // Also repaint whiteboard if dragging shapes on it
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

        // Convert source component coordinates to screen coordinates for checking
        // panels
        Point screenPoint = null;
        if (sourceComponentForDrag instanceof Component) {
            Component sourceComp = (Component) sourceComponentForDrag;
            try {
                screenPoint = new Point(x, y);
                // Use the source component's location on screen
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

        // Check if the drag ended over the trash panel
        if (trashPanel != null && screenPoint != null) {
            try {
                if (trashPanel.isPointOverTrash(screenPoint)) {
                    debugLog("Ending drag over trash panel - deleting shape(s)");
                    // If dragging from whiteboard, let whiteboard handle deletion via its endDrag
                    if (sourceComponentForDrag == whiteBoard) {
                        // Whiteboard's endDrag will call deleteSelectedShapes
                        deletedShape = true; // Mark as handled
                    } else {
                        // If dragging from elsewhere (e.g., toolbar button), just cancel
                        deletedShape = true; // Mark as handled, effectively cancelling drop
                        currentDraggable.endDrag(-1, -1); // Explicitly cancel button drop
                        debugLog("Drag from external source ended over trash. Cancelling drop.");
                    }
                    trashPanel.setShapeOverTrash(false); // Reset visual state
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over trash: " + e.getMessage());
            }
        }

        // Check if the drag ended over the toolbar panel
        if (toolbarPanel != null && screenPoint != null && !deletedShape) {
            try {
                if (toolbarPanel.isPointOverToolbar(screenPoint)) {
                    debugLog("Ending drag over toolbar panel - adding shape(s) to toolbar");
                    // If dragging from whiteboard, let whiteboard handle adding via its endDrag
                    if (sourceComponentForDrag == whiteBoard) {
                        // Whiteboard's endDrag will call toolbarPanel.addSelectedShapesToToolbar
                        addedToToolbar = true; // Mark as handled
                    } else {
                        // If dragging from elsewhere (e.g., toolbar button), just cancel
                        addedToToolbar = true; // Mark as handled, effectively cancelling drop
                        currentDraggable.endDrag(-1, -1); // Explicitly cancel button drop
                        debugLog("Drag from external source ended over toolbar. Cancelling drop.");
                    }
                    toolbarPanel.setShapeOverToolbar(false); // Reset visual state
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over toolbar: " + e.getMessage());
            }
        }

        // If the shape wasn't deleted or added to toolbar, handle normal drag end
        if (!deletedShape && !addedToToolbar) {
            // Convert source component coordinates to whiteboard coordinates if needed
            Point whiteboardPoint = null;
            if (x >= 0 && y >= 0 && sourceComponentForDrag instanceof Component) { // Check if source is a component
                // Pass the source component itself
                whiteboardPoint = convertToWhiteboardCoordinates(sourceComponentForDrag, new Point(x, y));
            } else {
                debugLog(
                        "Cannot convert to whiteboard coords: Invalid drop coords or sourceComponent is not a Component.");
            }

            if (whiteboardPoint != null) {
                // Check if the drop target is the whiteboard
                if (isPointOverWhiteboard(screenPoint)) {
                    debugLog("Ending drag on whiteboard at (" + whiteboardPoint.x + ", " + whiteboardPoint.y + ")");
                    // End the drag on the whiteboard - let the draggable handle it
                    currentDraggable.endDrag(whiteboardPoint.x, whiteboardPoint.y);
                } else {
                    debugLog("Drop point is not over whiteboard. Cancelling.");
                    currentDraggable.endDrag(-1, -1); // Cancel if not over whiteboard
                }

            } else {
                debugLog("Cancelling drag operation (not convertible to whiteboard coords or invalid drop)");
                // Cancel the drag
                currentDraggable.endDrag(-1, -1);
            }
        } else if (sourceComponentForDrag == whiteBoard) {
            // If dragging from whiteboard and it was deleted or added to toolbar,
            // still need to call whiteboard's endDrag to finalize its state
            debugLog("Calling WhiteBoard's endDrag after delete/toolbar add.");
            whiteBoard.endDrag(x, y); // Let whiteboard clean up its internal state
        }

        // Reset the whiteboard's current shape type if drag didn't start from
        // whiteboard
        if (sourceComponentForDrag != whiteBoard) {
            whiteBoard.setCurrentShapeType(null);
        }

        // Reset drag state
        isDragging = false;
        currentDraggable = null;

        // Always reset the trash panel visual state at the end of a drag operation
        resetTrashPanelState();
        // Always reset the toolbar panel visual state
        if (toolbarPanel != null)
            toolbarPanel.setShapeOverToolbar(false);
        // Reset whiteboard drag states
        if (whiteBoard != null) {
            whiteBoard.setDraggingToTrash(false);
            whiteBoard.setDraggingToToolbar(false);
        }

        // Request repaint of the source component
        if (sourceComponentForDrag instanceof Component) {
            ((Component) sourceComponentForDrag).repaint();
        }
        // Also repaint whiteboard if it wasn't the source
        if (sourceComponentForDrag != whiteBoard && whiteBoard != null) {
            whiteBoard.repaint();
        }

        sourceComponentForDrag = null; // Clear the source component reference
    }

    @Override
    // Changed signature to accept Object sourceComponent
    public Point convertToWhiteboardCoordinates(Object sourceComponent, Point sourcePoint) {
        if (whiteBoard == null || sourceComponent == null || !(sourceComponent instanceof Component)) {
            debugLog("ERROR: Cannot convert coordinates - WhiteBoard or sourceComponent invalid/not registered");
            return null;
        }

        Component sourceComp = (Component) sourceComponent; // Cast to Component

        try {
            // Convert source component coordinates to screen coordinates
            Point screenPoint = new Point(sourcePoint);
            Point sourceLocation = sourceComp.getLocationOnScreen(); // Use source component's location
            screenPoint.translate(sourceLocation.x, sourceLocation.y);

            debugLog("Input sourcePoint: (" + sourcePoint.x + ", " + sourcePoint.y + ") from "
                    + sourceComp.getClass().getSimpleName());
            debugLog("Calculated screenPoint: (" + screenPoint.x + ", " + screenPoint.y + ")");

            // Get whiteboard location on screen
            Point whiteboardLocation = whiteBoard.getLocationOnScreen();
            int wbX = whiteboardLocation.x;
            int wbY = whiteboardLocation.y;
            int wbWidth = whiteBoard.getWidth();
            int wbHeight = whiteBoard.getHeight();
            debugLog(
                    "Whiteboard screen location: (" + wbX + ", " + wbY + "), Size: (" + wbWidth + "x" + wbHeight + ")");

            // Check if the point is within the whiteboard bounds with a small margin
            // This allows shapes to be dragged even when the cursor is slightly outside the
            // whiteboard
            int margin = 10; // 10-pixel margin around the whiteboard
            boolean withinX = screenPoint.x >= wbX - margin && screenPoint.x < wbX + wbWidth + margin;
            boolean withinY = screenPoint.y >= wbY - margin && screenPoint.y < wbY + wbHeight + margin;
            debugLog("Bounds check: withinX=" + withinX + " (Range: " + (wbX - margin) + " to "
                    + (wbX + wbWidth + margin) + ")");
            debugLog("Bounds check: withinY=" + withinY + " (Range: " + (wbY - margin) + " to "
                    + (wbY + wbHeight + margin) + ")");

            if (withinX && withinY) {
                debugLog("Point IS within whiteboard bounds (including margin).");
                // Convert to whiteboard coordinates
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
            return null; // Return null if component isn't showing
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
        // Visual state update is handled within the trash panel itself now
        // if (isOverTrash) {
        // debugLog("Point is over trash panel");
        // trashPanel.setShapeOverTrash(true);
        // } else {
        // trashPanel.setShapeOverTrash(false);
        // }
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
        // Visual state update is handled within the toolbar panel itself now
        // if (isOverToolbar) {
        // debugLog("Point is over toolbar panel");
        // toolbarPanel.setShapeOverToolbar(true);
        // } else {
        // toolbarPanel.setShapeOverToolbar(false);
        // }
        return isOverToolbar;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
        debugLog("Debug " + (enabled ? "enabled" : "disabled"));
    }

    // --- Implementation of new interface methods ---

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
