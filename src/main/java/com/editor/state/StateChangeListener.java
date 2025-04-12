package com.editor.state;

/**
 * Interface for listening to state changes in the application.
 * Components that modify the application state should notify listeners
 * when significant changes occur.
 */
public interface StateChangeListener {
    /**
     * Called when a significant state change occurs.
     * 
     * @param source The object that triggered the state change
     * @param description A description of the change (for debugging)
     */
    void onStateChanged(Object source, String description);
}
