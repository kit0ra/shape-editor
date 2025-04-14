package com.editor.shapes;

import java.util.HashMap;
import java.util.Map;

import com.editor.memento.PrototypeRegistryMemento; 

/**
 * Registry for shape prototypes using the Prototype pattern.
 * Stores prototype instances and creates new shapes by cloning them.
 */
public class ShapePrototypeRegistry {
    private final Map<String, Shape> prototypes = new HashMap<>();

    /**
     * Registers a shape prototype with the given key.
     *
     * @param key       The key to identify the prototype
     * @param prototype The shape prototype to register
     */
    public void registerPrototype(String key, Shape prototype) {
        prototypes.put(key, prototype);
    }

    /**
     * Creates a new shape by cloning the prototype with the given key.
     *
     * @param key The key of the prototype to clone
     * @param x   The x-coordinate to position the new shape
     * @param y   The y-coordinate to position the new shape
     * @return A new shape instance positioned at (x, y)
     * @throws IllegalArgumentException if the key is not registered
     */
    public Shape createShape(String key, int x, int y) {
        Shape prototype = prototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("No prototype registered with key: " + key);
        }

        
        Shape clone = prototype.clone();

        
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
        return prototypes.keySet().stream().toArray(String[]::new);
    }

    /**
     * Gets a defensive copy of the internal prototype map.
     * Used for creating the memento.
     *
     * @return A new map containing the current prototypes.
     */
    public Map<String, Shape> getPrototypesMap() {
        
        return new HashMap<>(this.prototypes);
    }

    

    /**
     * Creates a memento containing the current state of the registry.
     *
     * @return A PrototypeRegistryMemento object.
     */
    public PrototypeRegistryMemento createMemento() {
        Map<String, Shape> currentPrototypes = new HashMap<>(this.prototypes);
        System.out.println("[LOG] ShapePrototypeRegistry.createMemento() - Creating memento with keys: "
                + currentPrototypes.keySet());
        
        return new PrototypeRegistryMemento(currentPrototypes);
    }

    /**
     * Restores the registry state from a memento.
     * Clears the current prototypes and replaces them with the ones from the
     * memento.
     *
     * @param memento The memento object containing the state to restore.
     */
    public void restoreFromMemento(PrototypeRegistryMemento memento) {
        if (memento == null) {
            System.err.println("[ShapePrototypeRegistry] Cannot restore from null memento.");
            return;
        }
        System.out.println("[ShapePrototypeRegistry] Restoring state from Memento...");
        
        Map<String, Shape> restoredPrototypes = memento.getPrototypesState();
        
        
        
        
        this.prototypes.putAll(restoredPrototypes);
        System.out.println(
                "[LOG] ShapePrototypeRegistry.restoreFromMemento() - Merged restore complete. Prototype count: "
                        + this.prototypes.size());
        
        System.out.println("[LOG] ShapePrototypeRegistry.restoreFromMemento() - Final keys in registry: "
                + this.prototypes.keySet());
    }
}
