package com.editor.shapes;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for ShapeGroup prototypes.
 * Stores prototype instances (groups) and creates new groups by cloning them.
 */
public class CompositeShapePrototypeRegistry {
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
}
