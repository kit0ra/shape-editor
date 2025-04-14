package com.editor.gui.button.decorators;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.IButton;

/**
 * Decorator that adds undo functionality to a button.
 */
public class UndoButtonDecorator extends ButtonDecorator {
    private final WhiteBoard whiteBoard;

    /**
     * Creates a new undo button decorator.
     * 
     * @param decoratedButton The button to decorate
     * @param whiteBoard The whiteboard to perform undo on
     */
    public UndoButtonDecorator(IButton decoratedButton, WhiteBoard whiteBoard) {
        super(decoratedButton);
        this.whiteBoard = whiteBoard;
    }

    @Override
    public void onClick() {
        
        whiteBoard.undo();
        
        
        super.onClick();
    }
}
