package com.editor.commands;

import java.util.List;

import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;

/**
 * Command to add a pre-defined ShapeGroup to the whiteboard.
 */
public class CreateGroupCommand implements Command {
    private List<Shape> shapes;
    private ShapeGroup groupToAdd;

    /**
     * Constructor for CreateGroupCommand.
     *
     * @param shapes     The list of shapes on the whiteboard.
     * @param groupToAdd The ShapeGroup instance to add (already cloned and
     *                   positioned).
     */
    public CreateGroupCommand(List<Shape> shapes, ShapeGroup groupToAdd) {
        this.shapes = shapes;
        this.groupToAdd = groupToAdd;
    }

    @Override
    public void execute() {
        if (groupToAdd != null && !shapes.contains(groupToAdd)) {
            shapes.add(groupToAdd);
            System.out.println("[CreateGroupCommand] Executed: Added group " + groupToAdd);
        } else {
            System.out.println("[CreateGroupCommand] Execute failed: Group is null or already added.");
        }
    }

    @Override
    public void undo() {
        if (groupToAdd != null && shapes.contains(groupToAdd)) {
            shapes.remove(groupToAdd);
            System.out.println("[CreateGroupCommand] Undone: Removed group " + groupToAdd);
        } else {
            System.out.println("[CreateGroupCommand] Undo failed: Group is null or not found.");
        }
    }

    /**
     * Gets the group that was added by this command.
     * 
     * @return The ShapeGroup instance.
     */
    public ShapeGroup getAddedGroup() {
        return groupToAdd;
    }
}
