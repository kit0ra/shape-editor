package com.editor.gui.button;

/**
 * Marker interface to identify buttons that can be dragged.
 * Used primarily for shape creation buttons that need to be
 * dragged onto the whiteboard to create shapes.
 */
public interface Draggable {
    /**
     * Called when a drag operation starts
     * @param x The x-coordinate where the drag started
     * @param y The y-coordinate where the drag started
     */
    void startDrag(int x, int y);
    
    /**
     * Called when a drag operation is in progress
     * @param x The current x-coordinate of the drag
     * @param y The current y-coordinate of the drag
     */
    void drag(int x, int y);
    
    /**
     * Called when a drag operation ends
     * @param x The x-coordinate where the drag ended
     * @param y The y-coordinate where the drag ended
     */
    void endDrag(int x, int y);
    
    /**
     * @return The type of shape this draggable button creates
     */
    String getShapeType();
}
