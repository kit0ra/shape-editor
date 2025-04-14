package com.editor.gui.button.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.editor.gui.button.IButton;
import com.editor.state.StateChangeListener;

/**
 * Manages a collection of buttons, their positions, and their associated
 * prototype keys.
 * Handles button layout and positioning.
 */
public class ButtonManager {
    private final List<IButton> buttons = new ArrayList<>();
    private final Map<IButton, String> buttonToPrototypeKeyMap = new HashMap<>();

    
    private static final int BUTTON_Y_START = 30;
    private static final int BUTTON_Y_SPACING = 5;
    private int nextButtonY = BUTTON_Y_START;

    
    private StateChangeListener stateChangeListener;

    /**
     * Adds a button to the manager with its associated prototype key.
     *
     * @param button       The button to add
     * @param prototypeKey The prototype key associated with the button
     * @return true if the button was added, false otherwise
     */
    public boolean addButton(IButton button, String prototypeKey) {
        if (button == null || prototypeKey == null) {
            return false;
        }

        buttons.add(button);
        buttonToPrototypeKeyMap.put(button, prototypeKey);

        
        nextButtonY += button.getHeight() + BUTTON_Y_SPACING;

        
        notifyStateChanged("Button added with key: " + prototypeKey);

        return true;
    }

    /**
     * Removes a button from the manager.
     *
     * @param button The button to remove
     * @return true if the button was removed, false otherwise
     */
    public boolean removeButton(IButton button) {
        if (button == null) {
            return false;
        }

        boolean removed = buttons.remove(button);
        if (removed) {
            String removedKey = buttonToPrototypeKeyMap.remove(button);
            recalculateButtonPositions();

            
            notifyStateChanged("Button removed with key: " + removedKey);
        }

        return removed;
    }

    /**
     * Gets the next Y position for a new button.
     *
     * @return The next Y position
     */
    public int getNextYPosition() {
        return nextButtonY;
    }

    /**
     * Recalculates the positions of all buttons.
     */
    public void recalculateButtonPositions() {
        nextButtonY = BUTTON_Y_START;

        for (IButton button : buttons) {
            
            
            nextButtonY += button.getHeight() + BUTTON_Y_SPACING;
        }
    }

    /**
     * Gets all buttons managed by this manager.
     *
     * @return The list of buttons
     */
    public List<IButton> getButtons() {
        return new ArrayList<>(buttons);
    }

    /**
     * Gets the prototype key associated with a button.
     *
     * @param button The button
     * @return The prototype key, or null if the button is not managed by this
     *         manager
     */
    public String getPrototypeKey(IButton button) {
        return buttonToPrototypeKeyMap.get(button);
    }

    /**
     * Gets all prototype keys managed by this manager.
     *
     * @return The list of prototype keys
     */
    public List<String> getPrototypeKeys() {
        return new ArrayList<>(buttonToPrototypeKeyMap.values());
    }

    /**
     * Sets the state change listener for this manager.
     *
     * @param listener The state change listener
     */
    public void setStateChangeListener(StateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    /**
     * Notifies the state change listener that a significant state change has
     * occurred.
     *
     * @param description A description of the change
     */
    private void notifyStateChanged(String description) {
        if (stateChangeListener != null) {
            stateChangeListener.onStateChanged(this, description);
        }
    }
}
