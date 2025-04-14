package com.editor.commands;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.editor.gui.WhiteBoard;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.memento.AppStateMemento;
import com.editor.memento.CompositeRegistryMemento;
import com.editor.memento.PrototypeRegistryMemento; 
import com.editor.memento.ShapeMemento;
import com.editor.memento.ToolbarMemento;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.ShapePrototypeRegistry; 

/**
 * Command to save the application state (WhiteBoard and ToolbarPanel) to a
 * file.
 */
public class SaveStateCommand implements Command {

    
    private final WhiteBoard whiteBoard;
    private final ToolbarPanel toolbarPanel;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final ShapePrototypeRegistry prototypeRegistry; 
    private final String filePath;

    /**
     * Constructor that takes all components including the composite registry.
     *
     * @param whiteBoard        The whiteboard component
     * @param toolbarPanel      The toolbar panel component
     * @param compositeRegistry The composite shape prototype registry
     * @param prototypeRegistry The standard shape prototype registry
     * @param filePath          The path to save the state to
     */
    public SaveStateCommand(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel,
            CompositeShapePrototypeRegistry compositeRegistry, ShapePrototypeRegistry prototypeRegistry,
            String filePath) { 
        this.whiteBoard = whiteBoard;
        this.toolbarPanel = toolbarPanel;
        this.compositeRegistry = compositeRegistry;
        this.prototypeRegistry = prototypeRegistry; 
        this.filePath = filePath;
    }

    /**
     * Backward compatibility constructor that doesn't require a composite registry.
     * Creates an empty composite registry internally.
     *
     * @param whiteBoard   The whiteboard component
     * @param toolbarPanel The toolbar panel component
     * @param filePath     The path to save the state to
     */
    
    
    
    
    
    

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void execute() {
        System.out.println("[STATE DEBUG] SaveStateCommand.execute() - START");
        System.out.println("[STATE DEBUG] Saving application state to: " + filePath);
        try {
            
            System.out.println("[STATE DEBUG] Creating WhiteBoard memento...");
            ShapeMemento whiteboardMemento = whiteBoard.createMemento();

            System.out.println("[STATE DEBUG] Creating ToolbarPanel memento...");
            ToolbarMemento toolbarMemento = toolbarPanel.createMemento();

            System.out.println("[STATE DEBUG] Creating CompositeRegistry memento...");
            
            CompositeRegistryMemento compositeRegistryMemento = new CompositeRegistryMemento(
                    compositeRegistry.getPrototypesMap());

            System.out.println("[STATE DEBUG] Creating PrototypeRegistry memento...");
            PrototypeRegistryMemento prototypeRegistryMemento = new PrototypeRegistryMemento(
                    prototypeRegistry.getPrototypesMap()); 

            System.out.println("[STATE DEBUG] Creating AppStateMemento with all component mementos...");
            AppStateMemento appState = new AppStateMemento(whiteboardMemento, toolbarMemento, compositeRegistryMemento,
                    prototypeRegistryMemento); 

            
            try (FileOutputStream fileOut = new FileOutputStream(filePath);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                System.out.println("[STATE DEBUG] Writing AppStateMemento to file: " + filePath);
                out.writeObject(appState);
                System.out.println("[STATE DEBUG] Application state successfully saved to file.");
            }

            System.out.println("[STATE DEBUG] SaveStateCommand.execute() - END");

        } catch (IOException i) {
            System.err.println("[SaveStateCommand] Error saving state: " + i.getMessage());
            i.printStackTrace();
            
        } catch (Exception e) {
            System.err.println("[SaveStateCommand] Unexpected error during save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void undo() {
        
        
        
        System.out.println("[SaveStateCommand] Undo called - typically not implemented for save.");
        
        
    }
}
