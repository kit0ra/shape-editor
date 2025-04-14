package com.editor.memento;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.ShapeGroup;

/**
 * Memento for storing the state of the CompositeShapePrototypeRegistry.
 * It holds a map of prototype keys to clones of the ShapeGroup prototypes.
 */
public class CompositeRegistryMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private final Map<String, ShapeGroup> prototypesState;

    /**
     * Constructs a memento by cloning the prototypes from the registry.
     *
     * @param prototypes The map of prototypes from the registry (String key ->
     *                   ShapeGroup value).
     */
    public CompositeRegistryMemento(Map<String, ShapeGroup> prototypes) {
        
        this.prototypesState = prototypes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (ShapeGroup) entry.getValue().clone() 
                ));
        System.out.println("[LOG] CompositeRegistryMemento(Map) - Constructor called. Storing keys: "
                + this.prototypesState.keySet());
    }

    /**
     * Constructs a memento directly from a CompositeShapePrototypeRegistry.
     * Uses reflection to access the internal map of prototypes.
     *
     * @param registry The CompositeShapePrototypeRegistry to create a memento from.
     */
    @SuppressWarnings({ "CallToPrintStackTrace", "UseSpecificCatch" })
    public CompositeRegistryMemento(CompositeShapePrototypeRegistry registry) {
        Map<String, ShapeGroup> extractedMap = new HashMap<>();

        try {
            
            Field field = CompositeShapePrototypeRegistry.class.getDeclaredField("groupPrototypes");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, ShapeGroup> registryMap = (Map<String, ShapeGroup>) field.get(registry);

            
            extractedMap = registryMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> (ShapeGroup) entry.getValue().clone() 
                    ));

            System.out.println(
                    "[CompositeRegistryMemento] Created from registry with " + extractedMap.size() + " prototypes.");
        } catch (Exception e) {
            System.err.println("[CompositeRegistryMemento] Error accessing registry: " + e.getMessage());
            e.printStackTrace();
        }

        this.prototypesState = extractedMap;
    }

    /**
     * Gets the saved map of prototype keys to ShapeGroup clones.
     *
     * @return A new map containing the saved prototype state (clones made during
     *         memento creation).
     */
    public Map<String, ShapeGroup> getPrototypesState() {
        
        
        return new HashMap<>(this.prototypesState);
    }

    /**
     * Gets the registry state as a CompositeShapePrototypeRegistry.
     * Creates a new registry and populates it with the saved prototypes.
     *
     * @return A new CompositeShapePrototypeRegistry populated with the saved
     *         prototypes.
     */
    public CompositeShapePrototypeRegistry getRegistryState() {
        CompositeShapePrototypeRegistry registry = new CompositeShapePrototypeRegistry();

        
        for (Map.Entry<String, ShapeGroup> entry : prototypesState.entrySet()) {
            String key = entry.getKey();
            ShapeGroup group = (ShapeGroup) entry.getValue().clone(); 
            registry.registerPrototype(key, group);
        }

        System.out
                .println("[CompositeRegistryMemento] Created registry with " + prototypesState.size() + " prototypes.");
        return registry;
    }
}
