package com.editor.commands;

import java.util.ArrayList;
import java.util.List;

import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;

/**
 * Command to ungroup a ShapeGroup back into its individual shapes.
 */
public class UngroupShapesCommand implements Command {
    private final List<Shape> canvasShapes;
    private final ShapeGroup group;
    private final List<Shape> ungroupedShapes;
    private final int groupIndex;

    /**
     * Creates a command to ungroup a shape group.
     * 
     * @param canvasShapes The list of shapes on the canvas
     * @param group The group to ungroup
     */
    public UngroupShapesCommand(List<Shape> canvasShapes, ShapeGroup group) {
        this.canvasShapes = canvasShapes;
        this.group = group;
        this.ungroupedShapes = new ArrayList<>(group.getShapes());
        this.groupIndex = canvasShapes.indexOf(group);
    }

    @Override
    public void execute() {
        
        canvasShapes.remove(group);
        
        
        canvasShapes.addAll(ungroupedShapes);
    }

    @Override
    public void undo() {
        
        canvasShapes.removeAll(ungroupedShapes);
        
        
        if (groupIndex >= 0 && groupIndex <= canvasShapes.size()) {
            canvasShapes.add(groupIndex, group);
        } else {
            canvasShapes.add(group);
        }
    }
    
    /**
     * Gets the list of ungrouped shapes.
     * 
     * @return The list of shapes that were in the group
     */
    public List<Shape> getUngroupedShapes() {
        return new ArrayList<>(ungroupedShapes);
    }
}
