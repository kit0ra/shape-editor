package com.editor.commands;

import java.util.Stack;

public class CommandHistory {
    private Stack<Command> history = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (!history.isEmpty()) {
            Command command = history.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            history.push(command);
        }
    }

    /**
     * Clears both the undo and redo history stacks.
     * Useful when loading a new state.
     */
    public void clear() {
        history.clear();
        redoStack.clear();
        System.out.println("[CommandHistory] History cleared.");
    }
}
