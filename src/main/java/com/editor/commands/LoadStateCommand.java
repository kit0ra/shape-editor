package com.editor.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import com.editor.gui.WhiteBoard;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.memento.AppStateMemento;
import com.editor.memento.CompositeRegistryMemento; 
import com.editor.memento.PrototypeRegistryMemento; 
import com.editor.memento.ShapeMemento;
import com.editor.memento.ToolbarMemento;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Shape; 
import com.editor.shapes.ShapeGroup; 
import com.editor.shapes.ShapePrototypeRegistry; 

/**
 * Command to load the application state (WhiteBoard and ToolbarPanel) from a
 * file.
 */
public class LoadStateCommand implements Command {

    private final WhiteBoard whiteBoard;
    private final ToolbarPanel toolbarPanel;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final ShapePrototypeRegistry prototypeRegistry; 
    private final String filePath;

    
    private AppStateMemento previousState = null;

    /**
     * Constructor that takes all components including the composite registry.
     *
     * @param whiteBoard        The whiteboard component
     * @param toolbarPanel      The toolbar panel component
     * @param compositeRegistry The composite shape prototype registry
     * @param prototypeRegistry The standard shape prototype registry
     * @param filePath          The path to load the state from
     */
    public LoadStateCommand(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel,
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
     * @param filePath     The path to load the state from
     */
    
    
    
    
    
    

    @Override
    public void execute() {
        System.out.println("[STATE DEBUG] LoadStateCommand.execute() - START");
        System.out.println("[STATE DEBUG] Loading application state from: " + filePath);
        try {
            
            System.out.println("[STATE DEBUG] Creating backup of current state for potential undo...");
            System.out.println("[STATE DEBUG] Creating WhiteBoard memento for backup...");
            ShapeMemento whiteboardBackup = whiteBoard.createMemento();

            System.out.println("[STATE DEBUG] Creating ToolbarPanel memento for backup...");
            ToolbarMemento toolbarBackup = toolbarPanel.createMemento();

            System.out.println("[STATE DEBUG] Creating CompositeRegistry memento for backup...");
            
            CompositeRegistryMemento compositeRegistryBackup = new CompositeRegistryMemento(
                    compositeRegistry.getPrototypesMap());
            
            PrototypeRegistryMemento prototypeRegistryBackup = new PrototypeRegistryMemento(
                    prototypeRegistry.getPrototypesMap());

            System.out.println("[STATE DEBUG] Creating AppStateMemento with backup mementos...");
            this.previousState = new AppStateMemento(whiteboardBackup, toolbarBackup, compositeRegistryBackup,
                    prototypeRegistryBackup); 

            
            System.out.println("[STATE DEBUG] Deserializing AppStateMemento from file...");
            AppStateMemento loadedState;
            try (FileInputStream fileIn = new FileInputStream(filePath);
                    ObjectInputStream in = new ObjectInputStream(fileIn)) {
                loadedState = (AppStateMemento) in.readObject();
                System.out.println("[STATE DEBUG] Application state successfully loaded from file.");
            }

            
            if (loadedState != null) {
                System.out.println("[STATE DEBUG] Restoring state to components...");

                System.out.println("[STATE DEBUG] Getting WhiteBoard state from loaded AppStateMemento...");
                ShapeMemento loadedWhiteboardState = loadedState.getWhiteBoardState();

                System.out.println("[STATE DEBUG] Getting ToolbarPanel state from loaded AppStateMemento...");
                ToolbarMemento loadedToolbarState = loadedState.getToolbarState();

                System.out.println("[STATE DEBUG] Getting CompositeRegistry state from loaded AppStateMemento...");
                CompositeRegistryMemento loadedCompositeRegistryState = loadedState.getCompositeRegistryState();

                System.out.println("[STATE DEBUG] Getting PrototypeRegistry state from loaded AppStateMemento...");
                PrototypeRegistryMemento loadedPrototypeRegistryState = loadedState.getPrototypeRegistryState(); 
                                                                                                                 
                                                                                                                 
                                                                                                                 

                
                System.out.println("[STATE DEBUG] Restoring WhiteBoard from memento...");
                whiteBoard.restoreFromMemento(loadedWhiteboardState);

                
                System.out.println("[STATE DEBUG] Restoring CompositeRegistry from memento...");
                if (loadedCompositeRegistryState != null) {
                    
                    Map<String, ShapeGroup> keysToRestore = loadedCompositeRegistryState.getPrototypesState();
                    System.out.println(
                            "[LOG] LoadStateCommand - Memento contains composite keys: " + keysToRestore.keySet());
                    compositeRegistry.restoreFromMemento(loadedCompositeRegistryState);
                    
                    System.out.println("[LOG] LoadStateCommand - compositeRegistry instance keys AFTER restore: "
                            + compositeRegistry.getPrototypesMap().keySet());
                } else {
                    System.err.println("[LOG] LoadStateCommand - ERROR: Loaded CompositeRegistryMemento is null!");
                }

                
                System.out.println("[STATE DEBUG] Restoring PrototypeRegistry from memento...");
                if (loadedPrototypeRegistryState != null) {
                    Map<String, Shape> standardKeysToRestore = loadedPrototypeRegistryState.getPrototypesState();
                    System.out.println("[LOG] LoadStateCommand - Memento contains standard keys: "
                            + standardKeysToRestore.keySet());
                    prototypeRegistry.restoreFromMemento(loadedPrototypeRegistryState);
                    System.out.println("[LOG] LoadStateCommand - prototypeRegistry instance keys AFTER restore: "
                            + prototypeRegistry.getPrototypesMap().keySet());
                } else {
                    System.err.println("[LOG] LoadStateCommand - ERROR: Loaded PrototypeRegistryMemento is null!");
                }

                
                System.out.println("[STATE DEBUG] Restoring ToolbarPanel from memento...");
                toolbarPanel.restoreFromMemento(loadedToolbarState); 

                System.out.println("[STATE DEBUG] State successfully restored to all components.");
            } else {
                System.err.println("[STATE DEBUG] ERROR: Loaded state was null.");
            }

            System.out.println("[STATE DEBUG] LoadStateCommand.execute() - END");

        } catch (IOException | ClassNotFoundException i) {
            System.err.println("[LoadStateCommand] Error loading state: " + i.getMessage());
            i.printStackTrace();
            
            this.previousState = null;
        } catch (Exception e) {
            System.err.println("[LoadStateCommand] Unexpected error during load: " + e.getMessage());
            e.printStackTrace();
            this.previousState = null;
        }
    }

    @Override
    public void undo() {
        System.out.println("[STATE DEBUG] LoadStateCommand.undo() - START");
        if (previousState != null) {
            try {
                System.out.println("[STATE DEBUG] Restoring previous state (undoing load)...");

                System.out.println("[STATE DEBUG] Getting WhiteBoard state from backup AppStateMemento...");
                ShapeMemento backupWhiteboardState = previousState.getWhiteBoardState();

                System.out.println("[STATE DEBUG] Getting ToolbarPanel state from backup AppStateMemento...");
                ToolbarMemento backupToolbarState = previousState.getToolbarState();

                System.out.println("[STATE DEBUG] Getting CompositeRegistry state from backup AppStateMemento...");
                CompositeRegistryMemento backupCompositeRegistryState = previousState.getCompositeRegistryState();

                
                System.out.println("[STATE DEBUG] Restoring WhiteBoard from backup memento...");
                whiteBoard.restoreFromMemento(backupWhiteboardState);

                
                System.out.println("[STATE DEBUG] Restoring CompositeRegistry from backup memento...");
                if (backupCompositeRegistryState != null) {
                    compositeRegistry.restoreFromMemento(backupCompositeRegistryState);
                } else {
                    System.err.println("[STATE DEBUG] ERROR: Backup CompositeRegistryMemento is null during undo!");
                }

                
                System.out.println("[STATE DEBUG] Restoring ToolbarPanel from backup memento...");
                toolbarPanel.restoreFromMemento(backupToolbarState);

                System.out.println("[STATE DEBUG] Previous state successfully restored (Undo complete).");
            } catch (Exception e) {
                System.err.println("[STATE DEBUG] ERROR: Failed to restore previous state: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("[STATE DEBUG] ERROR: Cannot undo load - no previous state saved (maybe load failed?).");
        }
        
        this.previousState = null;
        System.out.println("[STATE DEBUG] LoadStateCommand.undo() - END");
    }
}
