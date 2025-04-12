package com.editor.memento;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.editor.shapes.Shape;

/**
 * Memento for storing the state of the ShapePrototypeRegistry.
 * It holds a map of prototype keys to clones of the Shape prototypes.
 */
public class PrototypeRegistryMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    // Store clones of the prototypes to ensure immutability and capture state
    private final Map<String, Shape> prototypesState;

    /**
     * Constructs a memento by cloning the prototypes from the registry map.
     *
     * @param prototypes The map of prototypes from the registry (String key ->
     *                   Shape value).
     */
    public PrototypeRegistryMemento(Map<String, Shape> prototypes) {
        // Deep copy the map by cloning each Shape value
        this.prototypesState = prototypes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().clone() // Clone each Shape
                ));
        System.out.println("[LOG] PrototypeRegistryMemento(Map) - Constructor called. Storing keys: "
                + this.prototypesState.keySet());
    }

    /**
     * Gets the saved map of prototype keys to Shape clones.
     *
     * @return A new map containing the saved prototype state (clones made during
     *         memento creation).
     */
    public Map<String, Shape> getPrototypesState() {
        // Return a copy of the map held by the memento.
        // The values are already clones created when the memento was constructed.
        return new HashMap<>(this.prototypesState);
    }
}
