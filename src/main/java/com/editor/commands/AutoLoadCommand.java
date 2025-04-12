package com.editor.commands;

import java.io.File;

import com.editor.gui.WhiteBoard;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.state.AutoSaveManager;

/**
 * Command to automatically load the previously auto-saved state if it exists.
 */
public class AutoLoadCommand implements Command {
    private final WhiteBoard whiteBoard;
    private final ToolbarPanel toolbarPanel;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final AutoSaveManager autoSaveManager;
    
    /**
     * Creates a new AutoLoadCommand.
     * 
     * @param whiteBoard The WhiteBoard component
     * @param toolbarPanel The ToolbarPanel component
     * @param compositeRegistry The CompositeShapePrototypeRegistry
     * @param autoSaveManager The AutoSaveManager to get the autosave file path from
     */
    public AutoLoadCommand(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel,
                          CompositeShapePrototypeRegistry compositeRegistry,
                          AutoSaveManager autoSaveManager) {
        this.whiteBoard = whiteBoard;
        this.toolbarPanel = toolbarPanel;
        this.compositeRegistry = compositeRegistry;
        this.autoSaveManager = autoSaveManager;
    }
    
    @Override
    public void execute() {
        if (autoSaveManager.autoSaveExists()) {
            String filePath = autoSaveManager.getAutoSaveFilePath();
            System.out.println("[AutoLoadCommand] Found autosave file: " + filePath);
            
            try {
                // Create and execute a LoadStateCommand
                LoadStateCommand loadCommand = new LoadStateCommand(
                    whiteBoard, toolbarPanel, compositeRegistry, filePath);
                loadCommand.execute();
                System.out.println("[AutoLoadCommand] Successfully loaded autosaved state");
            } catch (Exception e) {
                System.err.println("[AutoLoadCommand] Error loading autosaved state: " + e.getMessage());
                e.printStackTrace();
                
                // If loading fails, delete the corrupted autosave file
                File autosaveFile = new File(filePath);
                if (autosaveFile.exists()) {
                    boolean deleted = autosaveFile.delete();
                    if (deleted) {
                        System.out.println("[AutoLoadCommand] Deleted corrupted autosave file");
                    } else {
                        System.err.println("[AutoLoadCommand] Failed to delete corrupted autosave file");
                    }
                }
            }
        } else {
            System.out.println("[AutoLoadCommand] No autosave file found, starting with empty state");
        }
    }
    
    @Override
    public void undo() {
        // Auto-loading cannot be undone in a meaningful way
        System.out.println("[AutoLoadCommand] Undo not supported for auto-loading");
    }
}
