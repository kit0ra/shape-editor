package com.editor.shapes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.editor.memento.CompositeRegistryMemento; // Added Memento import

/**
 * Registry for ShapeGroup prototypes.
 * Stores prototype instances (groups) and creates new groups by cloning them.
 */
public class CompositeShapePrototypeRegistry implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, ShapeGroup> groupPrototypes = new HashMap<>();

    /**
     * Registers a ShapeGroup prototype with the given key.
     *
     * @param key   The unique key to identify the group prototype.
     * @param group The ShapeGroup prototype to register. Should contain cloned
     *              shapes.
     */
    public void registerPrototype(String key, ShapeGroup group) {
        if (key == null || group == null) {
            throw new IllegalArgumentException("Key and group prototype cannot be null.");
        }
        groupPrototypes.put(key, group);
        System.out.println("[CompositeRegistry] Registered group prototype with key: " + key);
    }

    /**
     * Creates a new ShapeGroup by cloning the prototype with the given key.
     * The new group is positioned such that its bounding box's top-left corner is
     * at (x, y).
     *
     * @param key The key of the prototype to clone.
     * @param x   The target x-coordinate for the top-left corner of the new group's
     *            bounds.
     * @param y   The target y-coordinate for the top-left corner of the new group's
     *            bounds.
     * @return A new ShapeGroup instance positioned appropriately.
     * @throws IllegalArgumentException if the key is not registered.
     */
    public ShapeGroup createGroup(String key, int x, int y) {
        ShapeGroup prototype = groupPrototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("No group prototype registered with key: " + key);
        }

        // Clone the prototype group (deep clone is handled by ShapeGroup.clone())
        ShapeGroup clone = (ShapeGroup) prototype.clone();

        // Calculate the offset needed to move the clone to the target position (x, y)
        Rectangle bounds = clone.getBounds();
        int deltaX = x - bounds.getX();
        int deltaY = y - bounds.getY();

        // Move the cloned group by the calculated offset
        clone.move(deltaX, deltaY);

        System.out.println("[CompositeRegistry] Created group from key: " + key + " at (" + x + ", " + y + ")");
        return clone;
    }

    /**
     * Checks if a prototype with the given key is registered.
     *
     * @param key The key to check.
     * @return true if a prototype with the key is registered, false otherwise.
     */
    public boolean hasPrototype(String key) {
        return groupPrototypes.containsKey(key);
    }

    /**
     * Gets a defensive copy of the internal prototype map.
     * Used for creating the memento.
     *
     * @return A new map containing the current prototypes.
     */
    public Map<String, ShapeGroup> getPrototypesMap() { // Changed protected to public
        // Return a copy to prevent external modification of the internal map
        return new HashMap<>(this.groupPrototypes);
    }

    // --- Memento Pattern Implementation ---

    /**
     * Creates a memento containing the current state of the registry.
     *
     * @return A CompositeRegistryMemento object.
     */
    public CompositeRegistryMemento createMemento() {
        System.out.println("[CompositeRegistry] Creating Memento...");
        // Pass a copy of the current prototypes map
        return new CompositeRegistryMemento(new HashMap<>(this.groupPrototypes));
    }

    /**
     * Restores the registry state from a memento.
     * Clears the current prototypes and replaces them with the ones from the
     * memento.
     *
     * @param memento The memento object containing the state to restore.
     */
    public void restoreFromMemento(CompositeRegistryMemento memento) {
        if (memento == null) {
            System.err.println("[CompositeRegistry] Cannot restore from null memento.");
            return;
        }
        System.out.println("[CompositeRegistry] Restoring state from Memento...");
        // Get the map directly from the memento
        Map<String, ShapeGroup> restoredPrototypes = memento.getPrototypesState();
        // Clear current prototypes
        this.groupPrototypes.clear();
        // Add prototypes from the restored map
        this.groupPrototypes.putAll(restoredPrototypes);
        System.out.println(
                "[CompositeRegistry] Memento restore complete. Prototype count: " + this.groupPrototypes.size());
        // Log the keys restored for debugging
        System.out.println("[CompositeRegistry] Restored keys: " + this.groupPrototypes.keySet());
    }
}
