package com.editor.memento;

import java.io.Serializable;

/**
 * Represents the overall application state to be saved or loaded.
 * It holds mementos for different parts of the application, like the
 * WhiteBoard and the ToolbarPanel.
 */
public class AppStateMemento implements Serializable {

    private static final long serialVersionUID = 1L; // For serialization

    private final ShapeMemento whiteBoardState;
    private final ToolbarMemento toolbarState;
    private final CompositeRegistryMemento compositeRegistryState; // Added for composite shapes

    /**
     * Constructs an AppStateMemento.
     *
     * @param whiteBoardState        The memento for the WhiteBoard's state.
     * @param toolbarState           The memento for the ToolbarPanel's state.
     * @param compositeRegistryState The memento for the
     *                               CompositeShapePrototypeRegistry's state.
     */
    public AppStateMemento(ShapeMemento whiteBoardState, ToolbarMemento toolbarState,
            CompositeRegistryMemento compositeRegistryState) {
        this.whiteBoardState = whiteBoardState;
        this.toolbarState = toolbarState;
        this.compositeRegistryState = compositeRegistryState;
        System.out.println(
                "[STATE DEBUG] AppStateMemento created with WhiteBoard, Toolbar, and CompositeRegistry states.");
    }

    /**
     * Gets the saved WhiteBoard state.
     *
     * @return The ShapeMemento for the WhiteBoard.
     */
    public ShapeMemento getWhiteBoardState() {
        return whiteBoardState;
    }

    /**
     * Gets the saved ToolbarPanel state.
     *
     * @return The ToolbarMemento for the ToolbarPanel.
     */
    public ToolbarMemento getToolbarState() {
        return toolbarState;
    }

    /**
     * Gets the saved CompositeShapePrototypeRegistry state.
     *
     * @return The CompositeRegistryMemento for the CompositeShapePrototypeRegistry.
     */
    public CompositeRegistryMemento getCompositeRegistryState() {
        return compositeRegistryState;
    }
}
