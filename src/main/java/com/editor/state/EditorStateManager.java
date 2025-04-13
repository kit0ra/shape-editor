package com.editor.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the state of the editor and notifies listeners of state changes.
 * This class follows the Observer pattern.
 */
public class EditorStateManager {
    private final List<StateChangeListener> listeners = new ArrayList<>();
    private final Map<String, Object> stateCache = new HashMap<>();
    
    /**
     * Registers a state change and notifies listeners.
     *
     * @param component The component that changed state
     * @param action The action that caused the state change
     * @param data Additional data about the state change
     */
    public void registerStateChange(String component, String action, Object data) {
        // Cache the state change
        String key = component + "." + action;
        stateCache.put(key, data);
        
        // Create a state change event
        StateChangeEvent event = new StateChangeEvent(component, action, data);
        
        // Notify listeners
        notifyListeners(event);
    }
    
    /**
     * Notifies all listeners of a state change.
     *
     * @param event The state change event
     */
    public void notifyListeners(StateChangeEvent event) {
        for (StateChangeListener listener : listeners) {
            listener.onStateChanged(event.getSource(), event.getDescription());
        }
    }
    
    /**
     * Adds a listener for state changes.
     *
     * @param listener The listener to add
     */
    public void addListener(StateChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener for state changes.
     *
     * @param listener The listener to remove
     */
    public void removeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Gets the cached state for a component and action.
     *
     * @param component The component
     * @param action The action
     * @return The cached state, or null if not found
     */
    public Object getCachedState(String component, String action) {
        String key = component + "." + action;
        return stateCache.get(key);
    }
    
    /**
     * Clears the state cache.
     */
    public void clearCache() {
        stateCache.clear();
    }
    
    /**
     * Represents a state change event.
     */
    public static class StateChangeEvent {
        private final Object source;
        private final String action;
        private final Object data;
        
        /**
         * Creates a new StateChangeEvent.
         *
         * @param source The source of the event
         * @param action The action that caused the event
         * @param data Additional data about the event
         */
        public StateChangeEvent(Object source, String action, Object data) {
            this.source = source;
            this.action = action;
            this.data = data;
        }
        
        /**
         * Gets the source of the event.
         *
         * @return The source
         */
        public Object getSource() {
            return source;
        }
        
        /**
         * Gets the action that caused the event.
         *
         * @return The action
         */
        public String getAction() {
            return action;
        }
        
        /**
         * Gets additional data about the event.
         *
         * @return The data
         */
        public Object getData() {
            return data;
        }
        
        /**
         * Gets a description of the event.
         *
         * @return The description
         */
        public String getDescription() {
            return action + " in " + source.getClass().getSimpleName();
        }
    }
}
