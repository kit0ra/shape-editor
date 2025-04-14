package com.editor.gui.button.decorators;

import java.awt.Graphics;

import com.editor.commands.CommandHistory;
import com.editor.commands.CreateGroupCommand;
import com.editor.gui.WhiteBoard; 
import com.editor.gui.button.IButton;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.ShapeGroup;

/**
 * Decorator that adds the functionality to create a pre-defined ShapeGroup
 * on the whiteboard when the button is clicked.
 */
public class CompositeShapeCreationButtonDecorator extends ButtonDecorator {

    protected final WhiteBoard whiteBoard;
    protected final CompositeShapePrototypeRegistry compositeRegistry;
    protected final String groupKey; 

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
        super.onClick(); 

        System.out.println("[CompositeButtonDecorator] Clicked - attempting to create group with key: " + groupKey);

        
        
        
        
        CommandHistory commandHistory = whiteBoard.getCommandHistory(); 
        if (commandHistory == null) {
            System.err.println("[CompositeButtonDecorator] Error: CommandHistory not available from WhiteBoard.");
            return;
        }

        
        int targetX = whiteBoard.getWidth() / 2;
        int targetY = whiteBoard.getHeight() / 2;

        try {
            
            ShapeGroup newGroup = compositeRegistry.createGroup(groupKey, targetX, targetY);

            
            CreateGroupCommand command = new CreateGroupCommand(whiteBoard.getShapesList(), newGroup); 
                                                                                                       
            commandHistory.executeCommand(command);

            
            whiteBoard.clearSelection(); 
            newGroup.setSelected(true);
            whiteBoard.addSelectedShape(newGroup); 

            whiteBoard.repaint();
            System.out.println("[CompositeButtonDecorator] Successfully created and added group: " + groupKey);

        } catch (IllegalArgumentException e) {
            System.err.println("[CompositeButtonDecorator] Error creating group: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[CompositeButtonDecorator] Unexpected error during group creation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        
        
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
