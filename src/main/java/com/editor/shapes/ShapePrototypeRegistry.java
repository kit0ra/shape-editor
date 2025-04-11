package com.editor.shapes;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for shape prototypes using the Prototype pattern.
 * Stores prototype instances and creates new shapes by cloning them.
 */
public class ShapePrototypeRegistry {
    private final Map<String, Shape> prototypes = new HashMap<>();
    
    /**
     * Registers a shape prototype with the given key.
     * 
     * @param key The key to identify the prototype
     * @param prototype The shape prototype to register
     */
    public void registerPrototype(String key, Shape prototype) {
        prototypes.put(key, prototype);
    }
    
    /**
     * Creates a new shape by cloning the prototype with the given key.
     * 
     * @param key The key of the prototype to clone
     * @param x The x-coordinate to position the new shape
     * @param y The y-coordinate to position the new shape
     * @return A new shape instance positioned at (x, y)
     * @throws IllegalArgumentException if the key is not registered
     */
    public Shape createShape(String key, int x, int y) {
        Shape prototype = prototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("No prototype registered with key: " + key);
        }
        
        // Clone the prototype
        Shape clone = prototype.clone();
        
        // Position the clone at the specified coordinates
        clone.setPosition(x, y);
        
        return clone;
    }
    
    /**
     * Checks if a prototype with the given key is registered.
     * 
     * @param key The key to check
     * @return true if a prototype with the key is registered, false otherwise
     */
    public boolean hasPrototype(String key) {
        return prototypes.containsKey(key);
    }
    
    /**
     * Gets all registered prototype keys.
     * 
     * @return An array of all registered prototype keys
     */
    public String[] getPrototypeKeys() {
        return prototypes.keySet().toArray(new String[0]);
    }
}
