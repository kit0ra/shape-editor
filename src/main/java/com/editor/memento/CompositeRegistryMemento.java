package com.editor.memento;

import java.io.Serializable;

import com.editor.shapes.CompositeShapePrototypeRegistry;

/**
 * Memento class to store the state of the CompositeShapePrototypeRegistry.
 * This allows composite shape prototypes to be saved and restored.
 */
public class CompositeRegistryMemento implements Serializable {

    private static final long serialVersionUID = 1L; // For serialization

    // Store the registry directly since it's now serializable
    private final CompositeShapePrototypeRegistry registryState;

    /**
     * Constructs a CompositeRegistryMemento with the current registry state.
     *
     * @param registry The CompositeShapePrototypeRegistry to save.
     */
    public CompositeRegistryMemento(CompositeShapePrototypeRegistry registry) {
        // The registry is already serializable, so we can store it directly
        this.registryState = registry;
        
        System.out.println("[STATE DEBUG] CompositeRegistryMemento constructor - Created memento for composite registry");
    }

    /**
     * Gets the saved registry state.
     *
     * @return The saved CompositeShapePrototypeRegistry.
     */
    public CompositeShapePrototypeRegistry getRegistryState() {
        System.out.println("[STATE DEBUG] CompositeRegistryMemento.getRegistryState() - Returning registry state");
        return registryState;
    }
}
