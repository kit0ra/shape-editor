package com.editor.gui.button.decorators;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.editor.commands.CommandHistory;
import com.editor.commands.CreateGroupCommand;
import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.button.IButton;
import com.editor.mediator.DragMediator;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.ShapeGroup;

/**
 * A decorator that adds drag-and-drop functionality to a
 * CompositeShapeCreationButtonDecorator.
 * This allows composite shape buttons to be dragged onto the whiteboard.
 */
public class DraggableCompositeShapeCreationButtonDecorator extends CompositeShapeCreationButtonDecorator
        implements Draggable {
    private boolean isDragging = false;
    private int dragX, dragY;
    private DragMediator dragMediator;

    /**
     * Creates a new DraggableCompositeShapeCreationButtonDecorator
     *
     * @param decoratedButton   The button to decorate
     * @param whiteBoard        The whiteboard where shapes will be created
     * @param compositeRegistry The registry containing composite shape prototypes
     * @param groupKey          The key to identify the group in the registry
     */
    public DraggableCompositeShapeCreationButtonDecorator(
            IButton decoratedButton,
            WhiteBoard whiteBoard,
            CompositeShapePrototypeRegistry compositeRegistry,
            String groupKey) {
        super(decoratedButton, whiteBoard, compositeRegistry, groupKey);
        System.out.println("[DraggableCompositeButton] Created for group: " + groupKey);
    }

    @Override
    public void draw(Graphics g) {
        
        super.draw(g);

        
        if (isDragging) {
            Graphics2D g2d = (Graphics2D) g.create();
            try {
                System.out.println("[DraggableCompositeButton] Drawing drag preview at (" + dragX + ", " + dragY + ")");

                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                
                Color lightPink = new Color(255, 182, 193, 128); 
                Color lightPinkBorder = new Color(255, 105, 180); 

                
                g2d.setColor(lightPink);
                g2d.fillRect(dragX - 30, dragY - 20, 60, 40);
                g2d.setColor(lightPinkBorder);
                g2d.drawRect(dragX - 30, dragY - 20, 60, 40);

                
                g2d.setColor(Color.BLACK);
                g2d.drawString("G", dragX - 5, dragY + 5);

                
                g2d.setColor(Color.DARK_GRAY);
                float[] dash = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                g2d.drawLine(getX() + getWidth() / 2, getY() + getHeight() / 2, dragX, dragY);
            } finally {
                g2d.dispose();
            }
        }
    }

    @Override
    public void startDrag(int x, int y) {
        System.out.println("[DraggableCompositeButton] Started dragging at (" + x + ", " + y + ")");
        isDragging = true;
        dragX = x;
        dragY = y;
        if (dragMediator != null) {
            dragMediator.startDrag(this, this, x, y);
        }
    }

    @Override
    public void drag(int x, int y) {
        System.out.println("[DraggableCompositeButton] Dragging to (" + x + ", " + y + ")");
        dragX = x;
        dragY = y;
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void endDrag(int x, int y) {
        System.out.println("[DraggableCompositeButton] Ended drag at (" + x + ", " + y + ")");
        isDragging = false;

        
        if (x >= 0 && y >= 0) {
            System.out.println("[DraggableCompositeButton] Attempting to create composite shape at drop location: (" + x
                    + ", " + y + ")");

            
            
            
            CommandHistory commandHistory = whiteBoard.getCommandHistory();
            if (commandHistory == null) {
                System.err.println("[DraggableCompositeButton] Error: CommandHistory not available from WhiteBoard.");
                return; 
            }

            try {
                
                ShapeGroup newGroup = compositeRegistry.createGroup(groupKey, x, y);
                System.out.println("[DraggableCompositeButton] Created group instance for key '" + groupKey + "' at ("
                        + x + ", " + y + ")");

                
                CreateGroupCommand command = new CreateGroupCommand(whiteBoard.getShapesList(), newGroup);
                commandHistory.executeCommand(command);
                System.out.println("[DraggableCompositeButton] Executed CreateGroupCommand.");

                
                whiteBoard.clearSelection();
                newGroup.setSelected(true);
                whiteBoard.addSelectedShape(newGroup);
                System.out.println("[DraggableCompositeButton] Selected the new group.");

                whiteBoard.repaint();
                System.out.println("[DraggableCompositeButton] Successfully created and added group '" + groupKey
                        + "' at (" + x + ", " + y + ")");

            } catch (IllegalArgumentException e) {
                System.err.println("[DraggableCompositeButton] Error creating group: " + e.getMessage());
            } catch (Exception e) {
                System.err.println(
                        "[DraggableCompositeButton] Unexpected error during group creation: " + e.getMessage());
                e.printStackTrace();
            }
            
        } else {
            System.out.println("[DraggableCompositeButton] Drag ended outside valid area or cancelled (" + x + ", " + y
                    + "). No shape created.");
        }
    }

    @Override
    public String getShapeType() {
        
        return "CompositeShape";
    }

    /**
     * Sets the drag mediator for this button
     *
     * @param mediator The mediator to use for drag operations
     */
    public void setDragMediator(DragMediator mediator) {
        System.out.println("[DraggableCompositeButton] Setting drag mediator");
        this.dragMediator = mediator;
    }

}
