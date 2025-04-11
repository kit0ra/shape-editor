package com.editor.mediator;

import java.awt.Point;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.panel.CustomPanel;
import com.editor.gui.panel.TrashPanel;

/**
 * Interface for a mediator that handles drag operations between components.
 * This follows the Mediator pattern to centralize communication between
 * the WhiteBoard and panel components.
 */
public interface DragMediator {

    /**
     * Register a panel with the mediator
     *
     * @param panel The panel to register
     */
    void registerPanel(CustomPanel panel);

    /**
     * Register a whiteboard with the mediator
     *
     * @param whiteBoard The whiteboard to register
     */
    void registerWhiteBoard(WhiteBoard whiteBoard);

    /**
     * Start a drag operation from a panel
     *
     * @param panel     The panel where the drag started
     * @param draggable The draggable object being dragged
     * @param x         The x-coordinate where the drag started
     * @param y         The y-coordinate where the drag started
     */
    void startDrag(CustomPanel panel, Draggable draggable, int x, int y);

    /**
     * Update a drag operation
     *
     * @param x The current x-coordinate of the drag
     * @param y The current y-coordinate of the drag
     */
    void drag(int x, int y);

    /**
     * End a drag operation
     *
     * @param x The x-coordinate where the drag ended
     * @param y The y-coordinate where the drag ended
     */
    void endDrag(int x, int y);

    /**
     * Convert coordinates from a panel to whiteboard coordinates
     *
     * @param panel      The source panel
     * @param panelPoint The point in panel coordinates
     * @return The point in whiteboard coordinates, or null if outside the
     *         whiteboard
     */
    Point convertToWhiteboardCoordinates(CustomPanel panel, Point panelPoint);

    /**
     * Register a trash panel with the mediator
     *
     * @param trashPanel The trash panel to register
     */
    void registerTrashPanel(TrashPanel trashPanel);

    /**
     * Check if a point in screen coordinates is over the trash panel
     *
     * @param screenPoint The point in screen coordinates
     * @return true if the point is over the trash panel, false otherwise
     */
    boolean checkPointOverTrash(Point screenPoint);

    /**
     * Reset the trash panel's visual state
     */
    void resetTrashPanelState();

    /**
     * Enable or disable debug messages
     *
     * @param enabled Whether debug messages should be enabled
     */
    void setDebugEnabled(boolean enabled);
}
