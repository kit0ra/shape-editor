package com.editor.commands;

import java.util.ArrayList;
import java.util.List;

import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;

/**
 * Command to group multiple shapes into a single ShapeGroup.
 */
public class GroupShapesCommand implements Command {
    private final List<Shape> canvasShapes;
    private final List<Shape> shapesToGroup;
    private final ShapeGroup group;
    private final List<Integer> originalIndices = new ArrayList<>();

    /**
     * Creates a command to group shapes.
     * 
     * @param canvasShapes The list of shapes on the canvas
     * @param shapesToGroup The shapes to group together
     */
    public GroupShapesCommand(List<Shape> canvasShapes, List<Shape> shapesToGroup) {
        this.canvasShapes = canvasShapes;
        this.shapesToGroup = new ArrayList<>(shapesToGroup);
        this.group = new ShapeGroup(shapesToGroup);
        
        
        for (Shape shape : shapesToGroup) {
            originalIndices.add(canvasShapes.indexOf(shape));
        }
    }

    @Override
    public void execute() {
        
        canvasShapes.removeAll(shapesToGroup);
        
        
        canvasShapes.add(group);
    }

    @Override
    public void undo() {
        
        canvasShapes.remove(group);
        
        
        for (int i = 0; i < shapesToGroup.size(); i++) {
            int index = originalIndices.get(i);
            if (index >= 0 && index <= canvasShapes.size()) {
                canvasShapes.add(index, shapesToGroup.get(i));
            } else {
                canvasShapes.add(shapesToGroup.get(i));
            }
        }
    }
    
    /**
     * Gets the created group.
     * 
     * @return The shape group
     */
    public ShapeGroup getGroup() {
        return group;
    }
}
