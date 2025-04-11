package com.editor.mediator;

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
    private List<CustomPanel> panels = new ArrayList<>();
    private WhiteBoard whiteBoard;
    private Draggable currentDraggable;
    private CustomPanel sourcePanelForDrag;
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
    public void startDrag(CustomPanel panel, Draggable draggable, int x, int y) {
        if (whiteBoard == null) {
            debugLog("ERROR: Cannot start drag - WhiteBoard not registered");
            return;
        }

        currentDraggable = draggable;
        sourcePanelForDrag = panel;
        isDragging = true;

        // Set up the whiteboard for the drag operation
        whiteBoard.setPrototypeRegistry(whiteBoard.getPrototypeRegistry());
        whiteBoard.setCurrentShapeType(draggable.getShapeType());

        // Notify the draggable that dragging has started
        draggable.startDrag(x, y);

        debugLog("Started drag operation: " + draggable.getShapeType() + " at (" + x + ", " + y + ")");

        // Request repaint of the source panel
        panel.repaint();
    }

    @Override
    public void drag(int x, int y) {
        if (!isDragging || currentDraggable == null) {
            return;
        }

        // Update the draggable with the new position
        currentDraggable.drag(x, y);

        // Convert panel coordinates to screen coordinates for checking panels
        Point screenPoint = null;
        if (sourcePanelForDrag != null) {
            try {
                screenPoint = new Point(x, y);
                screenPoint.translate(sourcePanelForDrag.getLocationOnScreen().x,
                        sourcePanelForDrag.getLocationOnScreen().y);
            } catch (Exception e) {
                debugLog("Error converting coordinates: " + e.getMessage());
            }
        }

        // Check if the drag is over the trash panel
        if (trashPanel != null && screenPoint != null) {
            try {
                // Check if the point is over the trash panel
                boolean isOverTrash = trashPanel.isPointOverTrash(screenPoint);
                trashPanel.setShapeOverTrash(isOverTrash);
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
            } catch (Exception e) {
                debugLog("Error checking if shape is over toolbar: " + e.getMessage());
            }
        }

        debugLog("Dragging: " + currentDraggable.getShapeType() + " at (" + x + ", " + y + ")");

        // Request repaint of the source panel
        if (sourcePanelForDrag != null) {
            sourcePanelForDrag.repaint();
        }
    }

    @Override
    public void endDrag(int x, int y) {
        if (!isDragging || currentDraggable == null || sourcePanelForDrag == null) {
            return;
        }

        // Convert panel coordinates to screen coordinates for checking panels
        Point screenPoint = null;
        if (sourcePanelForDrag != null) {
            try {
                screenPoint = new Point(x, y);
                screenPoint.translate(sourcePanelForDrag.getLocationOnScreen().x,
                        sourcePanelForDrag.getLocationOnScreen().y);
            } catch (Exception e) {
                debugLog("Error converting coordinates: " + e.getMessage());
            }
        }

        // Check if the drag ended over the trash panel
        boolean deletedShape = false;
        if (trashPanel != null && screenPoint != null) {
            try {
                // Check if the point is over the trash panel
                if (trashPanel.isPointOverTrash(screenPoint)) {
                    debugLog("Ending drag over trash panel - deleting shape(s)");
                    deletedShape = trashPanel.deleteSelectedShapes();
                    trashPanel.setShapeOverTrash(false); // Reset the trash panel visual state
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over trash: " + e.getMessage());
            }
        }

        // Check if the drag ended over the toolbar panel
        boolean addedToToolbar = false;
        if (toolbarPanel != null && screenPoint != null && !deletedShape) {
            try {
                // Check if the point is over the toolbar panel
                if (toolbarPanel.isPointOverToolbar(screenPoint)) {
                    debugLog("Ending drag over toolbar panel - adding shape(s) to toolbar");
                    addedToToolbar = toolbarPanel.addSelectedShapesToToolbar();
                    toolbarPanel.setShapeOverToolbar(false); // Reset the toolbar panel visual state
                }
            } catch (Exception e) {
                debugLog("Error checking if shape is over toolbar: " + e.getMessage());
            }
        }

        // If the shape wasn't deleted or added to toolbar, handle normal drag end
        if (!deletedShape && !addedToToolbar) {
            // Convert panel coordinates to whiteboard coordinates if needed
            Point whiteboardPoint = null;
            if (x >= 0 && y >= 0) {
                whiteboardPoint = convertToWhiteboardCoordinates(sourcePanelForDrag, new Point(x, y));
            }

            if (whiteboardPoint != null) {
                debugLog("Ending drag on whiteboard at (" + whiteboardPoint.x + ", " + whiteboardPoint.y + ")");
                // End the drag on the whiteboard
                currentDraggable.endDrag(whiteboardPoint.x, whiteboardPoint.y);
            } else {
                debugLog("Cancelling drag operation (not over whiteboard)");
                // Cancel the drag
                currentDraggable.endDrag(-1, -1);
            }
        }

        // Reset the whiteboard's current shape type
        whiteBoard.setCurrentShapeType(null);

        // Reset drag state
        isDragging = false;
        currentDraggable = null;

        // Always reset the trash panel visual state at the end of a drag operation
        resetTrashPanelState();

        // Request repaint of the source panel
        sourcePanelForDrag.repaint();
        sourcePanelForDrag = null;
    }

    @Override
    public Point convertToWhiteboardCoordinates(CustomPanel panel, Point panelPoint) {
        if (whiteBoard == null || panel == null) {
            debugLog("ERROR: Cannot convert coordinates - WhiteBoard or panel not registered");
            return null;
        }

        try {
            // Convert panel coordinates to screen coordinates
            Point screenPoint = new Point(panelPoint);
            screenPoint.translate(panel.getLocationOnScreen().x, panel.getLocationOnScreen().y);

            // Get whiteboard location on screen
            Point whiteboardLocation = whiteBoard.getLocationOnScreen();

            // Check if the point is within the whiteboard bounds with a small margin
            // This allows shapes to be dragged even when the cursor is slightly outside the
            // whiteboard
            int margin = 10; // 10-pixel margin around the whiteboard
            if (screenPoint.x >= whiteboardLocation.x - margin &&
                    screenPoint.x < whiteboardLocation.x + whiteBoard.getWidth() + margin &&
                    screenPoint.y >= whiteboardLocation.y - margin &&
                    screenPoint.y < whiteboardLocation.y + whiteBoard.getHeight() + margin) {

                // Convert to whiteboard coordinates
                Point whiteboardPoint = new Point(
                        screenPoint.x - whiteboardLocation.x,
                        screenPoint.y - whiteboardLocation.y);

                debugLog("Converted panel point (" + panelPoint.x + ", " + panelPoint.y +
                        ") to whiteboard point (" + whiteboardPoint.x + ", " + whiteboardPoint.y + ")");

                return whiteboardPoint;
            }

            debugLog("Point is outside whiteboard bounds");
            return null;
        } catch (Exception e) {
            debugLog("ERROR: Exception during coordinate conversion: " + e.getMessage());
            e.printStackTrace();
            return null;
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
        if (isOverTrash) {
            debugLog("Point is over trash panel");
            trashPanel.setShapeOverTrash(true);
        } else {
            trashPanel.setShapeOverTrash(false);
        }

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
        if (isOverToolbar) {
            debugLog("Point is over toolbar panel");
            toolbarPanel.setShapeOverToolbar(true);
        } else {
            toolbarPanel.setShapeOverToolbar(false);
        }

        return isOverToolbar;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
        debugLog("Debug " + (enabled ? "enabled" : "disabled"));
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
