package com.editor.mediator;

import java.awt.Point;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.panel.CustomPanel;
import com.editor.gui.panel.ToolbarPanel;
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
     * @param panel           The panel where the drag started
     * @param draggable       The draggable object being dragged
     * @param x               The x-coordinate where the drag started
     * @param sourceComponent The component where the drag started (e.g.,
     *                        CustomPanel or WhiteBoard)
     * @param draggable       The draggable object being dragged
     * @param x               The x-coordinate where the drag started (relative to
     *                        sourceComponent)
     * @param y               The y-coordinate where the drag started (relative to
     *                        sourceComponent)
     */
    void startDrag(Object sourceComponent, Draggable draggable, int x, int y);

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
     * Convert coordinates from a source component to whiteboard coordinates
     *
     * @param sourceComponent The source component (e.g., CustomPanel, WhiteBoard)
     * @param sourcePoint     The point in the source component's coordinates
     * @return The point in whiteboard coordinates, or null if outside the
     *         whiteboard or conversion fails
     */
    Point convertToWhiteboardCoordinates(Object sourceComponent, Point sourcePoint);

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
     * Register a toolbar panel with the mediator
     *
     * @param toolbarPanel The toolbar panel to register
     */
    void registerToolbarPanel(ToolbarPanel toolbarPanel);

    /**
     * Check if a point in screen coordinates is over the toolbar panel
     *
     * @param screenPoint The point in screen coordinates
     * @return true if the point is over the toolbar panel, false otherwise
     */
    boolean checkPointOverToolbar(Point screenPoint);

    /**
     * Enable or disable debug messages
     *
     * @param enabled Whether debug messages should be enabled
     */
    void setDebugEnabled(boolean enabled);

    /**
     * Checks if a drag operation is currently in progress.
     *
     * @return true if dragging, false otherwise.
     */
    boolean isDragging();

    /**
     * Gets the component from which the current drag operation originated.
     *
     * @return The source component (e.g., CustomPanel, WhiteBoard), or null if not
     *         dragging.
     */
    Object getSourceComponentForDrag();
}
