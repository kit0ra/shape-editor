package com.editor.commands;

public interface Command {
    void execute();

    void undo();
}
