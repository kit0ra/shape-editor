package com.editor.state;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.editor.commands.SaveStateCommand;
import com.editor.gui.WhiteBoard;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.ShapePrototypeRegistry;

/**
 * Manages automatic saving of the application state.
 * Implements a debounce mechanism to avoid saving too frequently.
 * Implemented as a Singleton to ensure only one instance exists.
 */
public class AutoSaveManager {
    private static final String DEFAULT_AUTOSAVE_FILENAME = "autosave.ser";
    private static final long DEBOUNCE_DELAY_MS = 1000; 

    
    private static AutoSaveManager instance;

    private WhiteBoard whiteBoard;
    private ToolbarPanel toolbarPanel;
    private CompositeShapePrototypeRegistry compositeRegistry;
    private ShapePrototypeRegistry prototypeRegistry;
    private String autoSaveFilePath;
    private final ScheduledExecutorService scheduler;

    private boolean saveScheduled = false;
    private boolean initialized = false;

    /**
     * Private constructor to prevent direct instantiation.
     * Use getInstance() methods instead.
     */
    private AutoSaveManager() {
        
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Gets the singleton instance of AutoSaveManager.
     * Must call initialize() before using this method.
     *
     * @return The singleton instance
     * @throws IllegalStateException if the manager hasn't been initialized
     */
    public static synchronized AutoSaveManager getInstance() {
        if (instance == null) {
            instance = new AutoSaveManager();
        }

        if (!instance.initialized) {
            throw new IllegalStateException("AutoSaveManager not initialized. Call initialize() first.");
        }

        return instance;
    }

    /**
     * Initializes the AutoSaveManager with the default autosave file path.
     *
     * @param whiteBoard        The WhiteBoard component
     * @param toolbarPanel      The ToolbarPanel component
     * @param compositeRegistry The CompositeShapePrototypeRegistry
     * @param prototypeRegistry The ShapePrototypeRegistry
     * @return The initialized singleton instance
     */
    public static synchronized AutoSaveManager initialize(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel,
            CompositeShapePrototypeRegistry compositeRegistry, ShapePrototypeRegistry prototypeRegistry) {
        return initialize(whiteBoard, toolbarPanel, compositeRegistry, prototypeRegistry,
                System.getProperty("user.dir") + File.separator + DEFAULT_AUTOSAVE_FILENAME);
    }

    /**
     * Initializes the AutoSaveManager with a custom autosave file path.
     *
     * @param whiteBoard        The WhiteBoard component
     * @param toolbarPanel      The ToolbarPanel component
     * @param compositeRegistry The CompositeShapePrototypeRegistry
     * @param prototypeRegistry The ShapePrototypeRegistry
     * @param autoSaveFilePath  The path to save the autosave file
     * @return The initialized singleton instance
     */
    public static synchronized AutoSaveManager initialize(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel,
            CompositeShapePrototypeRegistry compositeRegistry, ShapePrototypeRegistry prototypeRegistry,
            String autoSaveFilePath) {
        if (instance == null) {
            instance = new AutoSaveManager();
        }

        instance.whiteBoard = whiteBoard;
        instance.toolbarPanel = toolbarPanel;
        instance.compositeRegistry = compositeRegistry;
        instance.prototypeRegistry = prototypeRegistry;
        instance.autoSaveFilePath = autoSaveFilePath;
        instance.initialized = true;

        System.out.println("[AutoSaveManager] Initialized with autosave path: " + autoSaveFilePath);
        return instance;
    }

    /**
     * Triggers an auto-save operation with debouncing.
     * If multiple triggers occur within the debounce period, only one save will be
     * performed.
     */
    public synchronized void triggerAutoSave() {
        if (!saveScheduled) {
            saveScheduled = true;
            System.out.println("[AutoSaveManager] Auto-save scheduled");

            scheduler.schedule(() -> {
                performSave();
                
                saveScheduled = false;
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
                    whiteBoard, toolbarPanel, compositeRegistry, prototypeRegistry, autoSaveFilePath); 
                                                                                                       
            saveCommand.execute();
            System.out.println("[AutoSaveManager] Auto-save completed successfully");
        } catch (Exception e) {
            System.err.println("[AutoSaveManager] Error during auto-save: " + e.getMessage());
            System.err.println("[AutoSaveManager] Stack trace: " + e);
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
            
            performSave();

            
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
