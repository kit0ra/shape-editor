package com.editor.gui.button.decorators;

import java.awt.Graphics;

import com.editor.commands.CommandHistory;
import com.editor.commands.CreateGroupCommand;
import com.editor.gui.WhiteBoard; // Assuming WhiteBoard provides access or we pass it
import com.editor.gui.button.IButton;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.ShapeGroup;

/**
 * Decorator that adds the functionality to create a pre-defined ShapeGroup
 * on the whiteboard when the button is clicked.
 */
public class CompositeShapeCreationButtonDecorator extends ButtonDecorator {

    private final WhiteBoard whiteBoard;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final String groupKey; // Key to identify the group in the registry

    public CompositeShapeCreationButtonDecorator(IButton decoratedButton, WhiteBoard whiteBoard,
            CompositeShapePrototypeRegistry compositeRegistry, String groupKey) {
        super(decoratedButton);
        this.whiteBoard = whiteBoard;
        this.compositeRegistry = compositeRegistry;
        this.groupKey = groupKey;

        if (whiteBoard == null || compositeRegistry == null || groupKey == null) {
            throw new IllegalArgumentException("WhiteBoard, CompositeRegistry, and GroupKey cannot be null.");
        }
    }

    @Override
    public void onClick() {
        super.onClick(); // Call underlying button's onClick if needed

        System.out.println("[CompositeButtonDecorator] Clicked - attempting to create group with key: " + groupKey);

        // Get the CommandHistory from the WhiteBoard (assuming a getter exists or is
        // added)
        // If no direct access, this might need adjustment (e.g., passing
        // CommandHistory)
        CommandHistory commandHistory = whiteBoard.getCommandHistory(); // Needs getter in WhiteBoard
        if (commandHistory == null) {
            System.err.println("[CompositeButtonDecorator] Error: CommandHistory not available from WhiteBoard.");
            return;
        }

        // Define where to place the new group (e.g., center of whiteboard)
        int targetX = whiteBoard.getWidth() / 2;
        int targetY = whiteBoard.getHeight() / 2;

        try {
            // Create the group instance from the registry
            ShapeGroup newGroup = compositeRegistry.createGroup(groupKey, targetX, targetY);

            // Create and execute the command
            CreateGroupCommand command = new CreateGroupCommand(whiteBoard.getShapesList(), newGroup); // Needs getter
                                                                                                       // in WhiteBoard
            commandHistory.executeCommand(command);

            // Optionally select the newly added group
            whiteBoard.clearSelection(); // Needs implementation or alternative
            newGroup.setSelected(true);
            whiteBoard.addSelectedShape(newGroup); // Needs implementation or alternative

            whiteBoard.repaint();
            System.out.println("[CompositeButtonDecorator] Successfully created and added group: " + groupKey);

        } catch (IllegalArgumentException e) {
            System.err.println("[CompositeButtonDecorator] Error creating group: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[CompositeButtonDecorator] Unexpected error during group creation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Delegate other IButton methods ---

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        // Note: The actual shape drawing is handled by the ShapeDrawingButtonDecorator
        // if it was applied before this decorator
    }

    @Override
    public boolean isMouseOver(int x, int y) {
        return super.isMouseOver(x, y);
    }

    @Override
    public void onMouseOver() {
        super.onMouseOver();
    }

    @Override
    public void onMouseOut() {
        super.onMouseOut();
    }

    @Override
    public boolean isCurrentlyHovered() {
        return super.isCurrentlyHovered();
    }

    @Override
    public int getX() {
        return super.getX();
    }

    @Override
    public int getY() {
        return super.getY();
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }
}
