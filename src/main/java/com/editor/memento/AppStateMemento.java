package com.editor.memento;

import java.io.Serializable;

/**
 * Represents the overall application state to be saved or loaded.
 * It holds mementos for different parts of the application, like the
 * WhiteBoard, ToolbarPanel, and both prototype registries.
 */
public class AppStateMemento implements Serializable {

    private static final long serialVersionUID = 1L; 

    private final ShapeMemento whiteBoardState;
    private final ToolbarMemento toolbarState;
    private final CompositeRegistryMemento compositeRegistryState;
    private final PrototypeRegistryMemento prototypeRegistryState; 

    /**
     * Constructs an AppStateMemento.
     *
     * @param whiteBoardState        The memento for the WhiteBoard's state.
     * @param toolbarState           The memento for the ToolbarPanel's state.
     * @param compositeRegistryState The memento for the
     *                               CompositeShapePrototypeRegistry's state.
     * @param prototypeRegistryState The memento for the ShapePrototypeRegistry's
     *                               state.
     */
    public AppStateMemento(ShapeMemento whiteBoardState, ToolbarMemento toolbarState,
            CompositeRegistryMemento compositeRegistryState, PrototypeRegistryMemento prototypeRegistryState) { 
                                                                                                                
                                                                                                                
        this.whiteBoardState = whiteBoardState;
        this.toolbarState = toolbarState;
        this.compositeRegistryState = compositeRegistryState;
        this.prototypeRegistryState = prototypeRegistryState; 
        System.out.println(
                "[STATE DEBUG] AppStateMemento created with WhiteBoard, Toolbar, CompositeRegistry, and PrototypeRegistry states.");
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

    /**
     * Gets the saved ShapePrototypeRegistry state.
     *
     * @return The PrototypeRegistryMemento for the ShapePrototypeRegistry.
     */
    public PrototypeRegistryMemento getPrototypeRegistryState() { 
        return prototypeRegistryState;
    }
}
