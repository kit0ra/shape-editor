package com.editor.state;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.editor.commands.SaveStateCommand;
import com.editor.gui.WhiteBoard;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.shapes.CompositeShapePrototypeRegistry;

/**
 * Manages automatic saving of the application state.
 * Implements a debounce mechanism to avoid saving too frequently.
 */
public class AutoSaveManager {
    private static final String DEFAULT_AUTOSAVE_FILENAME = "autosave.ser";
    private static final long DEBOUNCE_DELAY_MS = 1000; // 1 second debounce
    
    private final WhiteBoard whiteBoard;
    private final ToolbarPanel toolbarPanel;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final String autoSaveFilePath;
    private final ScheduledExecutorService scheduler;
    
    private boolean saveScheduled = false;
    
    /**
     * Creates a new AutoSaveManager with the default autosave file path.
     * 
     * @param whiteBoard The WhiteBoard component
     * @param toolbarPanel The ToolbarPanel component
     * @param compositeRegistry The CompositeShapePrototypeRegistry
     */
    public AutoSaveManager(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel, 
                          CompositeShapePrototypeRegistry compositeRegistry) {
        this(whiteBoard, toolbarPanel, compositeRegistry, 
             System.getProperty("user.dir") + File.separator + DEFAULT_AUTOSAVE_FILENAME);
    }
    
    /**
     * Creates a new AutoSaveManager with a custom autosave file path.
     * 
     * @param whiteBoard The WhiteBoard component
     * @param toolbarPanel The ToolbarPanel component
     * @param compositeRegistry The CompositeShapePrototypeRegistry
     * @param autoSaveFilePath The path to save the autosave file
     */
    public AutoSaveManager(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel, 
                          CompositeShapePrototypeRegistry compositeRegistry,
                          String autoSaveFilePath) {
        this.whiteBoard = whiteBoard;
        this.toolbarPanel = toolbarPanel;
        this.compositeRegistry = compositeRegistry;
        this.autoSaveFilePath = autoSaveFilePath;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        System.out.println("[AutoSaveManager] Initialized with autosave path: " + autoSaveFilePath);
    }
    
    /**
     * Triggers an auto-save operation with debouncing.
     * If multiple triggers occur within the debounce period, only one save will be performed.
     */
    public synchronized void triggerAutoSave() {
        if (!saveScheduled) {
            saveScheduled = true;
            System.out.println("[AutoSaveManager] Auto-save scheduled");
            
            scheduler.schedule(() -> {
                performSave();
                synchronized (AutoSaveManager.this) {
                    saveScheduled = false;
                }
            }, DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Performs the actual save operation.
     */
    private void performSave() {
        System.out.println("[AutoSaveManager] Performing auto-save...");
        try {
            SaveStateCommand saveCommand = new SaveStateCommand(
                whiteBoard, toolbarPanel, compositeRegistry, autoSaveFilePath);
            saveCommand.execute();
            System.out.println("[AutoSaveManager] Auto-save completed successfully");
        } catch (Exception e) {
            System.err.println("[AutoSaveManager] Error during auto-save: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if an autosave file exists.
     * 
     * @return true if an autosave file exists, false otherwise
     */
    public boolean autoSaveExists() {
        File file = new File(autoSaveFilePath);
        return file.exists() && file.isFile() && file.length() > 0;
    }
    
    /**
     * Gets the path to the autosave file.
     * 
     * @return The autosave file path
     */
    public String getAutoSaveFilePath() {
        return autoSaveFilePath;
    }
    
    /**
     * Shuts down the scheduler.
     * Should be called when the application is closing.
     */
    public void shutdown() {
        System.out.println("[AutoSaveManager] Shutting down...");
        scheduler.shutdown();
        try {
            // Perform one final save before shutting down
            performSave();
            
            // Wait for any pending tasks to complete
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
