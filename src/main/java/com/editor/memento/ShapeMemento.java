package com.editor.memento;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import com.editor.shapes.Shape;

/**
 * Memento class to store the state of the WhiteBoard.
 * It holds a list of clones of the shapes currently on the whiteboard.
 */
public class ShapeMemento implements Serializable {

    private static final long serialVersionUID = 1L; 

    
    private final List<Shape> shapesState;

    /**
     * Constructs a ShapeMemento by cloning the provided list of shapes.
     *
     * @param shapesToSave The list of shapes currently on the WhiteBoard.
     */
    public ShapeMemento(List<Shape> shapesToSave) {
        
        this.shapesState = shapesToSave.stream()
                .map(Shape::clone) 
                .collect(Collectors.toList());
        System.out.println("[ShapeMemento] Created with " + this.shapesState.size() + " shapes.");
    }

    /**
     * Gets the saved list of shape clones.
     *
     * @return A new list containing clones of the saved shapes.
     */
    public List<Shape> getShapesState() {
        
        return this.shapesState.stream()
                .map(Shape::clone)
                .collect(Collectors.toList());
    }
}
