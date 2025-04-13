package com.editor.shapes.processing;

import com.editor.gui.button.IButton;

/**
 * Represents the result of processing a shape.
 * Contains the created button and the prototype key.
 */
public class ProcessingResult {
    private final IButton button;
    private final String prototypeKey;
    
    /**
     * Creates a new ProcessingResult.
     *
     * @param button The button created during processing
     * @param prototypeKey The prototype key associated with the button
     */
    public ProcessingResult(IButton button, String prototypeKey) {
        this.button = button;
        this.prototypeKey = prototypeKey;
    }
    
    /**
     * Gets the button created during processing.
     *
     * @return The button
     */
    public IButton getButton() {
        return button;
    }
    
    /**
     * Gets the prototype key associated with the button.
     *
     * @return The prototype key
     */
    public String getPrototypeKey() {
        return prototypeKey;
    }
}
