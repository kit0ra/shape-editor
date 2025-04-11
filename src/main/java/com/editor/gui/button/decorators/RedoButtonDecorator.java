package com.editor.gui.button.decorators;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.IButton;

/**
 * Decorator that adds redo functionality to a button.
 */
public class RedoButtonDecorator extends ButtonDecorator {
    private final WhiteBoard whiteBoard;

    /**
     * Creates a new redo button decorator.
     * 
     * @param decoratedButton The button to decorate
     * @param whiteBoard The whiteboard to perform redo on
     */
    public RedoButtonDecorator(IButton decoratedButton, WhiteBoard whiteBoard) {
        super(decoratedButton);
        this.whiteBoard = whiteBoard;
    }

    @Override
    public void onClick() {
        // Call the redo method on the whiteboard
        whiteBoard.redo();
        
        // Call the original button's onClick method
        super.onClick();
    }
}
