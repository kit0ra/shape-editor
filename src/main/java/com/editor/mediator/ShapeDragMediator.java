package com.editor.mediator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.panel.CustomPanel;

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

        // Reset the whiteboard's current shape type
        whiteBoard.setCurrentShapeType(null);

        // Reset drag state
        isDragging = false;
        currentDraggable = null;

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
