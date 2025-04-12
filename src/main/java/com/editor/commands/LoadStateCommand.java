package com.editor.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import com.editor.gui.WhiteBoard;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.memento.AppStateMemento;
import com.editor.memento.CompositeRegistryMemento; // Add import for Map
import com.editor.memento.PrototypeRegistryMemento; // Added import
import com.editor.memento.ShapeMemento;
import com.editor.memento.ToolbarMemento;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Shape; // Added import
import com.editor.shapes.ShapeGroup; // Add import for ShapeGroup
import com.editor.shapes.ShapePrototypeRegistry; // Added import

/**
 * Command to load the application state (WhiteBoard and ToolbarPanel) from a
 * file.
 */
public class LoadStateCommand implements Command {

    private final WhiteBoard whiteBoard;
    private final ToolbarPanel toolbarPanel;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final ShapePrototypeRegistry prototypeRegistry; // Added standard registry
    private final String filePath;

    // Store the state *before* loading, in case we need to undo the load
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
            String filePath) { // Added prototypeRegistry
        this.whiteBoard = whiteBoard;
        this.toolbarPanel = toolbarPanel;
        this.compositeRegistry = compositeRegistry;
        this.prototypeRegistry = prototypeRegistry; // Added
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
    // Note: The deprecated constructor below likely needs updating or removal
    // if standard shapes added to the toolbar are expected to be saved/loaded via
    // this path.
    // For now, focusing on the main constructor used by AutoLoadCommand.
    // public LoadStateCommand(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel,
    // String filePath) { ... }

    @Override
    public void execute() {
        System.out.println("[STATE DEBUG] LoadStateCommand.execute() - START");
        System.out.println("[STATE DEBUG] Loading application state from: " + filePath);
        try {
            // 1. Store the current state before loading, for undo functionality
            System.out.println("[STATE DEBUG] Creating backup of current state for potential undo...");
            System.out.println("[STATE DEBUG] Creating WhiteBoard memento for backup...");
            ShapeMemento whiteboardBackup = whiteBoard.createMemento();

            System.out.println("[STATE DEBUG] Creating ToolbarPanel memento for backup...");
            ToolbarMemento toolbarBackup = toolbarPanel.createMemento();

            System.out.println("[STATE DEBUG] Creating CompositeRegistry memento for backup...");
            // *** CORRECTED LINE: Use getPrototypesMap() ***
            CompositeRegistryMemento compositeRegistryBackup = new CompositeRegistryMemento(
                    compositeRegistry.getPrototypesMap());
            // Backup standard registry too
            PrototypeRegistryMemento prototypeRegistryBackup = new PrototypeRegistryMemento(
                    prototypeRegistry.getPrototypesMap());

            System.out.println("[STATE DEBUG] Creating AppStateMemento with backup mementos...");
            this.previousState = new AppStateMemento(whiteboardBackup, toolbarBackup, compositeRegistryBackup,
                    prototypeRegistryBackup); // Add prototype backup

            // 2. Deserialize the AppStateMemento from the file
            System.out.println("[STATE DEBUG] Deserializing AppStateMemento from file...");
            AppStateMemento loadedState;
            try (FileInputStream fileIn = new FileInputStream(filePath);
                    ObjectInputStream in = new ObjectInputStream(fileIn)) {
                loadedState = (AppStateMemento) in.readObject();
                System.out.println("[STATE DEBUG] Application state successfully loaded from file.");
            }

            // 3. Restore state from the loaded memento
            if (loadedState != null) {
                System.out.println("[STATE DEBUG] Restoring state to components...");

                System.out.println("[STATE DEBUG] Getting WhiteBoard state from loaded AppStateMemento...");
                ShapeMemento loadedWhiteboardState = loadedState.getWhiteBoardState();

                System.out.println("[STATE DEBUG] Getting ToolbarPanel state from loaded AppStateMemento...");
                ToolbarMemento loadedToolbarState = loadedState.getToolbarState();

                System.out.println("[STATE DEBUG] Getting CompositeRegistry state from loaded AppStateMemento...");
                CompositeRegistryMemento loadedCompositeRegistryState = loadedState.getCompositeRegistryState();

                System.out.println("[STATE DEBUG] Getting PrototypeRegistry state from loaded AppStateMemento...");
                PrototypeRegistryMemento loadedPrototypeRegistryState = loadedState.getPrototypeRegistryState(); // Get
                                                                                                                 // standard
                                                                                                                 // registry
                                                                                                                 // memento

                // Restore WhiteBoard first
                System.out.println("[STATE DEBUG] Restoring WhiteBoard from memento...");
                whiteBoard.restoreFromMemento(loadedWhiteboardState);

                // Restore Registry *BEFORE* Toolbar
                System.out.println("[STATE DEBUG] Restoring CompositeRegistry from memento...");
                if (loadedCompositeRegistryState != null) {
                    // Log the keys *before* restoring
                    Map<String, ShapeGroup> keysToRestore = loadedCompositeRegistryState.getPrototypesState();
                    System.out.println(
                            "[LOG] LoadStateCommand - Memento contains composite keys: " + keysToRestore.keySet());
                    compositeRegistry.restoreFromMemento(loadedCompositeRegistryState);
                    // Add log AFTER restoring the registry, before restoring toolbar
                    System.out.println("[LOG] LoadStateCommand - compositeRegistry instance keys AFTER restore: "
                            + compositeRegistry.getPrototypesMap().keySet());
                } else {
                    System.err.println("[LOG] LoadStateCommand - ERROR: Loaded CompositeRegistryMemento is null!");
                }

                // Restore Standard Registry *BEFORE* Toolbar
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

                // Restore ToolbarPanel last
                System.out.println("[STATE DEBUG] Restoring ToolbarPanel from memento...");
                toolbarPanel.restoreFromMemento(loadedToolbarState); // Now registry should be populated

                System.out.println("[STATE DEBUG] State successfully restored to all components.");
            } else {
                System.err.println("[STATE DEBUG] ERROR: Loaded state was null.");
            }

            System.out.println("[STATE DEBUG] LoadStateCommand.execute() - END");

        } catch (IOException | ClassNotFoundException i) {
            System.err.println("[LoadStateCommand] Error loading state: " + i.getMessage());
            i.printStackTrace();
            // Reset previous state if load failed, so undo doesn't restore garbage
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

                // Restore WhiteBoard first
                System.out.println("[STATE DEBUG] Restoring WhiteBoard from backup memento...");
                whiteBoard.restoreFromMemento(backupWhiteboardState);

                // Restore Registry *BEFORE* Toolbar
                System.out.println("[STATE DEBUG] Restoring CompositeRegistry from backup memento...");
                if (backupCompositeRegistryState != null) {
                    compositeRegistry.restoreFromMemento(backupCompositeRegistryState);
                } else {
                    System.err.println("[STATE DEBUG] ERROR: Backup CompositeRegistryMemento is null during undo!");
                }

                // Restore ToolbarPanel last
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
        // Clear previous state after undo to prevent multiple undos of the same load
        this.previousState = null;
        System.out.println("[STATE DEBUG] LoadStateCommand.undo() - END");
    }
}
