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

    
    private final Map<String, Shape> prototypesState;

    /**
     * Constructs a memento by cloning the prototypes from the registry map.
     *
     * @param prototypes The map of prototypes from the registry (String key ->
     *                   Shape value).
     */
    public PrototypeRegistryMemento(Map<String, Shape> prototypes) {
        
        this.prototypesState = prototypes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().clone() 
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
        
        
        return new HashMap<>(this.prototypesState);
    }
}
