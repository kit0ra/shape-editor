package com.editor.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import com.editor.gui.WhiteBoard;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.memento.AppStateMemento;
import com.editor.memento.CompositeRegistryMemento;
import com.editor.memento.ShapeMemento;
import com.editor.memento.ToolbarMemento;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.ShapeGroup;

/**
 * Command to load the application state (WhiteBoard and ToolbarPanel) from a
 * file.
 */
public class LoadStateCommand implements Command {

    private final WhiteBoard whiteBoard;
    private final ToolbarPanel toolbarPanel;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final String filePath;

    // Store the state *before* loading, in case we need to undo the load
    private AppStateMemento previousState = null;

    public LoadStateCommand(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel,
            CompositeShapePrototypeRegistry compositeRegistry, String filePath) {
        this.whiteBoard = whiteBoard;
        this.toolbarPanel = toolbarPanel;
        this.compositeRegistry = compositeRegistry;
        this.filePath = filePath;
    }

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
            CompositeRegistryMemento compositeRegistryBackup = new CompositeRegistryMemento(compositeRegistry);

            System.out.println("[STATE DEBUG] Creating AppStateMemento with backup mementos...");
            this.previousState = new AppStateMemento(whiteboardBackup, toolbarBackup, compositeRegistryBackup);

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

                System.out.println("[STATE DEBUG] Restoring WhiteBoard from memento...");
                whiteBoard.restoreFromMemento(loadedWhiteboardState);

                System.out.println("[STATE DEBUG] Restoring ToolbarPanel from memento...");
                toolbarPanel.restoreFromMemento(loadedToolbarState);

                // Update the current composite registry with the loaded one
                System.out.println("[STATE DEBUG] Updating CompositeRegistry with loaded state...");
                // Since we can't replace the registry itself (it's final), we'll clear it and
                // copy the contents
                updateCompositeRegistry(compositeRegistry, loadedCompositeRegistryState.getRegistryState());

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

    /**
     * Helper method to update a composite registry with the contents of another.
     * This is used during loading to update the existing registry with the loaded
     * state.
     *
     * @param targetRegistry The registry to update
     * @param sourceRegistry The registry to copy from
     */
    private void updateCompositeRegistry(CompositeShapePrototypeRegistry targetRegistry,
            CompositeShapePrototypeRegistry sourceRegistry) {
        System.out.println("[COMPOSITE DEBUG] Starting updateCompositeRegistry");
        System.out.println("[COMPOSITE DEBUG] Target registry: " + targetRegistry);
        System.out.println("[COMPOSITE DEBUG] Source registry: " + sourceRegistry);

        // We need to access the internal map of the registry
        // Since we can't directly access it, we'll use reflection
        try {
            // Get the groupPrototypes field from the CompositeShapePrototypeRegistry class
            java.lang.reflect.Field field = CompositeShapePrototypeRegistry.class.getDeclaredField("groupPrototypes");
            field.setAccessible(true); // Make it accessible

            // Get the maps from both registries
            @SuppressWarnings("unchecked")
            Map<String, ShapeGroup> targetMap = (Map<String, ShapeGroup>) field.get(targetRegistry);
            @SuppressWarnings("unchecked")
            Map<String, ShapeGroup> sourceMap = (Map<String, ShapeGroup>) field.get(sourceRegistry);

            System.out.println("[COMPOSITE DEBUG] Target map before update: " + targetMap.keySet());
            System.out.println("[COMPOSITE DEBUG] Source map keys: " + sourceMap.keySet());
            System.out.println("[COMPOSITE DEBUG] Source map size: " + sourceMap.size());

            // Clear the target map and copy all entries from the source map
            targetMap.clear();
            System.out.println("[COMPOSITE DEBUG] Target map cleared");

            for (Map.Entry<String, ShapeGroup> entry : sourceMap.entrySet()) {
                String key = entry.getKey();
                ShapeGroup group = entry.getValue();
                System.out.println("[COMPOSITE DEBUG] Copying key: " + key + ", group: " + group);

                // Create a deep clone of the group to ensure proper serialization
                ShapeGroup clonedGroup = (ShapeGroup) group.clone();
                targetMap.put(key, clonedGroup);
                System.out.println("[COMPOSITE DEBUG] Added to target map: " + key);
            }

            System.out.println("[COMPOSITE DEBUG] Target map after update: " + targetMap.keySet());
            System.out.println("[COMPOSITE DEBUG] Successfully updated composite registry with " +
                    sourceMap.size() + " prototypes.");
        } catch (Exception e) {
            System.err.println("[COMPOSITE DEBUG] ERROR: Failed to update composite registry: " + e.getMessage());
            e.printStackTrace();
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

                System.out.println("[STATE DEBUG] Restoring ToolbarPanel from backup memento...");
                toolbarPanel.restoreFromMemento(backupToolbarState);

                System.out.println("[STATE DEBUG] Updating CompositeRegistry from backup memento...");
                updateCompositeRegistry(compositeRegistry, backupCompositeRegistryState.getRegistryState());

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
