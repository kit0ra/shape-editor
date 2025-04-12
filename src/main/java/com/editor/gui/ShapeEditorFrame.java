package com.editor.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.editor.commands.AutoLoadCommand;
import com.editor.commands.LoadStateCommand;
import com.editor.commands.SaveStateCommand;
import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.ButtonDecorator;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.RedoButtonDecorator;
import com.editor.gui.button.decorators.ShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.gui.button.decorators.UndoButtonDecorator;
import com.editor.gui.panel.HorizontalPanel;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.gui.panel.TrashPanel;
import com.editor.gui.panel.VerticalPanel;
import com.editor.mediator.DragMediator;
import com.editor.mediator.ShapeDragMediator; // Ensure this import is present
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.state.AutoSaveManager;
import com.editor.state.StateChangeListener;
import com.editor.utils.ImageLoader;

public class ShapeEditorFrame extends Frame {

    private HorizontalPanel horizontalPanel;
    private VerticalPanel verticalPanel;
    private ToolbarPanel toolbarPanel;
    private TrashPanel trashPanel;
    private WhiteBoard whiteBoard;

    // Mediator for drag operations
    private DragMediator dragMediator;

    // Button spacing and positioning constants
    private static final int HORIZONTAL_BUTTON_SPACING = 10;

    // Shape prototype registries
    private ShapePrototypeRegistry prototypeRegistry;
    private CompositeShapePrototypeRegistry compositeRegistry; // Added

    // Auto-save manager
    private AutoSaveManager autoSaveManager;
    private static final int HORIZONTAL_INITIAL_OFFSET = 10;
    private static final int BUTTON_LEFT_MARGIN = 10; // Left margin for buttons in panels

    public ShapeEditorFrame() {
        super("Shape Editor");
        setSize(800, 600);
        setLayout(null);

        whiteBoard = new WhiteBoard(800, 600, Color.WHITE);
        whiteBoard.setRelativeBounds(20, 30, 80, 70); // x=20%, y=30% (below vertical panel), width=80%, height=70%
        whiteBoard.makeResponsiveTo(this);
        add(whiteBoard);

        horizontalPanel = new HorizontalPanel();
        horizontalPanel.setRelativeBounds(0, 10, 100, 10); // x=10%, y=10%, width=80%, height=10%
        horizontalPanel.makeResponsiveTo(this);
        add(horizontalPanel);

        verticalPanel = new VerticalPanel();
        verticalPanel.setRelativeBounds(0, 20, 20, 10); // x=0%, y=20% (below horizontal panel), width=20%, height=10%
        verticalPanel.makeResponsiveTo(this);
        verticalPanel.setTargetWhiteBoard(whiteBoard); // Définir le whiteboard comme cible pour le glisser-déposer
        add(verticalPanel);

        // Initialize the toolbar panel in the middle of the left side
        toolbarPanel = new ToolbarPanel();
        toolbarPanel.setRelativeBounds(0, 30, 20, 60); // x=0%, y=30% (below vertical panel), width=20%, height=60%
        toolbarPanel.makeResponsiveTo(this);
        toolbarPanel.setTargetWhiteBoard(whiteBoard);
        add(toolbarPanel);

        // Initialize the trash panel at the bottom left
        trashPanel = new TrashPanel();
        trashPanel.setRelativeBounds(0, 90, 20, 10); // x=0%, y=90% (bottom), width=20%, height=10%
        trashPanel.makeResponsiveTo(this);
        trashPanel.setTargetWhiteBoard(whiteBoard);
        add(trashPanel);

        // Preload all icons
        ImageLoader.preloadImages(
                "icons/save.png",
                "icons/load.png",
                "icons/undo.png",
                "icons/redo.png",
                "icons/rectangle.png",
                "icons/polygon.png");

        // Initialize the shape prototype registries
        initializePrototypeRegistry();
        initializeCompositeRegistry(); // Added call

        // Initialize and set up the drag mediator
        setupDragMediator();

        // Initialize the auto-save manager
        setupAutoSaveManager();

        // Add window listener to handle closing properly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Perform a final auto-save FIRST, before disposing components
                if (autoSaveManager != null) {
                    System.out.println("[LOG] ShapeEditorFrame.windowClosing - Performing final auto-save...");
                    autoSaveManager.shutdown(); // This calls performSave()
                    System.out.println("[LOG] ShapeEditorFrame.windowClosing - Final auto-save complete.");
                } else {
                    System.out.println(
                            "[LOG] ShapeEditorFrame.windowClosing - AutoSaveManager is null, skipping final save.");
                }

                // Now clean up resources
                System.out.println("[LOG] ShapeEditorFrame.windowClosing - Disposing whiteboard...");
                if (whiteBoard != null) {
                    whiteBoard.dispose();
                }

                System.out.println("[LOG] ShapeEditorFrame.windowClosing - Disposing frame...");
                dispose(); // Dispose the frame itself
                System.out.println("[LOG] ShapeEditorFrame.windowClosing - Exiting application...");
                System.exit(0);
            }
        });

        init();
    }

    private void init() {
        // Initialize buttons in both panels
        setupHorizontalButtons();
        setupVerticalButtons();
    }

    /**
     * Sets up the drag mediator to handle drag operations between components
     */
    private void setupDragMediator() {
        // Create a new mediator instance
        dragMediator = new ShapeDragMediator();

        // Enable debug messages
        dragMediator.setDebugEnabled(true);

        // Register components with the mediator
        dragMediator.registerWhiteBoard(whiteBoard);

        // Set the mediator in the panels and whiteboard
        verticalPanel.setDragMediator(dragMediator);
        horizontalPanel.setDragMediator(dragMediator);
        trashPanel.setDragMediator(dragMediator);
        toolbarPanel.setDragMediator(dragMediator);
        whiteBoard.setDragMediator(dragMediator);
    }

    /**
     * Sets up the auto-save manager and initializes auto-loading.
     */
    private void setupAutoSaveManager() {
        // Initialize the singleton auto-save manager with both registries
        autoSaveManager = AutoSaveManager.initialize(whiteBoard, toolbarPanel, compositeRegistry, prototypeRegistry);

        // Create a state change listener to trigger auto-save using lambda
        StateChangeListener stateChangeListener = (source, description) -> {
            System.out.println("[AutoSave] State changed: " + description);
            autoSaveManager.triggerAutoSave();
        };

        // Set the state change listener on the whiteboard and toolbar panel
        whiteBoard.setStateChangeListener(stateChangeListener);
        toolbarPanel.setStateChangeListener(stateChangeListener);

        // Set registries on ToolbarPanel BEFORE auto-load
        System.out.println("[LOG] ShapeEditorFrame - Setting registries on ToolbarPanel...");
        toolbarPanel.setPrototypeRegistry(prototypeRegistry);
        toolbarPanel.setCompositePrototypeRegistry(compositeRegistry); // Added

        // Auto-load the previous state if it exists
        if (autoSaveManager.autoSaveExists()) {
            System.out.println("[LOG] ShapeEditorFrame - Found previous state, auto-loading...");
            // Use the AutoLoadCommand with the updated constructor (uses singleton)
            AutoLoadCommand autoLoadCommand = new AutoLoadCommand(
                    whiteBoard, toolbarPanel, compositeRegistry, prototypeRegistry);
            autoLoadCommand.execute();
        } else {
            System.out.println("[AutoSave] No previous state found, starting with empty state.");
        }

        // REMOVED Shutdown hook - rely on windowClosing for final save

        // Register the trash panel with the mediator
        dragMediator.registerTrashPanel(trashPanel);

        // Register the toolbar panel with the mediator
        dragMediator.registerToolbarPanel(toolbarPanel);

        // Set the toolbar panel reference in the whiteboard (can stay here)
        whiteBoard.setToolbarPanel(toolbarPanel);

        // Registries were set above, before auto-load

        System.out.println("Drag mediator initialized and connected to components");
    }

    /**
     * Initializes the shape prototype registry with predefined shapes
     */
    private void initializePrototypeRegistry() {
        prototypeRegistry = new ShapePrototypeRegistry();

        // Register a rectangle prototype
        Rectangle rectanglePrototype = new Rectangle(0, 0, 80, 60);
        rectanglePrototype.setFillColor(Color.BLUE);
        rectanglePrototype.setBorderColor(Color.BLACK);
        prototypeRegistry.registerPrototype("Rectangle", rectanglePrototype);

        // Register a regular polygon (hexagon) prototype
        int radius = 40; // Radius of the hexagon
        int numPoints = 6; // Hexagon has 6 points

        // Create a regular hexagon centered at (0, 0) with radius 40
        RegularPolygon polygonPrototype = new RegularPolygon(0, 0, radius, numPoints);
        polygonPrototype.setFillColor(Color.GREEN);
        polygonPrototype.setBorderColor(Color.BLACK);
        prototypeRegistry.registerPrototype("Polygon", polygonPrototype);

        // Set the prototype registry in the whiteboard
        whiteBoard.setPrototypeRegistry(prototypeRegistry);
    }

    /**
     * Initializes the composite shape prototype registry.
     */
    private void initializeCompositeRegistry() {
        compositeRegistry = new CompositeShapePrototypeRegistry();
        // Initially empty, prototypes are added dynamically when groups are dragged to
        // the toolbar.
        System.out.println("Composite shape registry initialized.");
    }

    /**
     * Sets up the horizontal panel buttons (save, load, undo, redo)
     */
    private void setupHorizontalButtons() {
        int x = HORIZONTAL_INITIAL_OFFSET;

        // Create save button (icon only) - Base button, no action yet
        IButton saveButtonBase = createIconButton(x, 5, "icons/save.png", "Save the current drawing");
        // Decorate save button to add save action
        IButton saveButton = new ButtonDecorator(saveButtonBase) {
            @Override
            public void onClick() {
                super.onClick(); // Call decorated button's onClick (if any)
                handleSaveState(); // Add save action
            }
            // Assuming ButtonDecorator delegates other methods
        };
        horizontalPanel.addButton(saveButton); // Add the final decorated button

        // Create load button - Base button, no action yet
        x += saveButton.getWidth() + HORIZONTAL_BUTTON_SPACING; // Use final button width for spacing
        IButton loadButtonBase = createIconButton(x, 5, "icons/load.png", "Load a saved drawing");
        // Decorate load button to add load action
        IButton loadButton = new ButtonDecorator(loadButtonBase) {
            @Override
            public void onClick() {
                super.onClick(); // Call decorated button's onClick (if any)
                handleLoadState(); // Add load action
            }
            // Assuming ButtonDecorator delegates other methods
        };
        horizontalPanel.addButton(loadButton); // Add the final decorated button

        // Create undo button
        x += loadButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton undoButton = createIconButton(x, 5, "icons/undo.png", "Undo the last action");
        undoButton = new UndoButtonDecorator(undoButton, whiteBoard); // Decorate with Undo functionality
        horizontalPanel.addButton(undoButton);

        // Create redo button
        x += undoButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton redoButton = createIconButton(x, 5, "icons/redo.png", "Redo the last undone action");
        redoButton = new RedoButtonDecorator(redoButton, whiteBoard); // Decorate with Redo functionality
        horizontalPanel.addButton(redoButton);

        // Note: The previous Save/Load buttons were replaced above with functional
        // ones.
        // If you intended to keep separate Save/Load State buttons, adjust accordingly.
    }

    /**
     * Sets up the vertical panel buttons (rectangle, polygon) in a horizontal
     * layout
     */
    private void setupVerticalButtons() {
        // Center buttons vertically in the panel
        int y = 5; // Small top margin
        int x = BUTTON_LEFT_MARGIN;

        // Create rectangle button (icon only with drag capability)
        IButton rectangleButton = createDraggableShapeButton(
                x, y,
                "icons/rectangle.png",
                "Draw a rectangle",
                "Rectangle");
        verticalPanel.addButton(rectangleButton);

        // Create polygon button (icon only with drag capability) - positioned
        // horizontally
        x += rectangleButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton polygonButton = createDraggableShapeButton(
                x, y,
                "icons/polygon.png",
                "Draw a polygon",
                "Polygon");
        verticalPanel.addButton(polygonButton);
    }

    /**
     * Helper method to create an icon-only button with tooltip
     */
    private IButton createIconButton(int x, int y, String iconPath, String tooltipText) {
        // Load the icon
        Image icon = ImageLoader.loadImage(iconPath);

        // Create base button
        IButton button = new CustomButton(x, y, 40, 40, "");

        // Add icon decoration
        if (icon != null) {
            button = new ImageDecorator(
                    button,
                    icon,
                    24, 24, // Icon dimensions
                    8, // Padding
                    ImageDecorator.ImageMode.ICON_ONLY // Icon-only mode
            );
        }

        // Add tooltip decoration
        return new TooltipDecorator(button, tooltipText);
    }

    /**
     * Helper method to create a draggable shape button with icon and tooltip
     */
    private IButton createDraggableShapeButton(int x, int y, String iconPath, String tooltipText,
            String shapeType) {
        // First create a regular icon button
        IButton button = createIconButton(x, y, iconPath, tooltipText);

        // Add shape creation decorator to make it draggable
        button = new ShapeCreationButtonDecorator(button, whiteBoard, prototypeRegistry, shapeType);

        return button;
    }

    public void launch() {
        setVisible(true);
    }

    public static void main(String[] args) {
        ShapeEditorFrame frame = new ShapeEditorFrame();
        frame.launch();
    }

    // --- Save/Load Handling Methods ---

    private void handleSaveState() {
        System.out.println("[ShapeEditorFrame] Save button clicked.");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Application State");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Shape Editor State (*.ser)", "ser"));
        fileChooser.setSelectedFile(new File("editor_state.ser")); // Default filename

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure the file has the .ser extension
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".ser")) {
                filePath += ".ser";
            }
            System.out.println("[ShapeEditorFrame] Saving state to: " + filePath);

            // Create and execute the SaveStateCommand
            // Note: We assume whiteBoard, toolbarPanel, and both registries are correctly
            // initialized
            if (whiteBoard != null && toolbarPanel != null && compositeRegistry != null && prototypeRegistry != null) { // Check
                                                                                                                        // prototypeRegistry
                // Create the SaveStateCommand with both registries
                SaveStateCommand saveCommand = new SaveStateCommand(whiteBoard, toolbarPanel,
                        compositeRegistry, prototypeRegistry, filePath); // Pass prototypeRegistry
                // Execute directly, or use CommandHistory if undoing save makes sense
                saveCommand.execute();
                // whiteBoard.getCommandHistory().executeCommand(saveCommand); // If using
                // history
            } else {
                System.err.println(
                        "[ShapeEditorFrame] Cannot save: WhiteBoard, ToolbarPanel, or CompositeRegistry is null.");
            }
        } else {
            System.out.println("[ShapeEditorFrame] Save command cancelled by user.");
        }
    }

    private void handleLoadState() {
        System.out.println("[ShapeEditorFrame] Load button clicked.");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Application State");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Shape Editor State (*.ser)", "ser"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            String filePath = fileToLoad.getAbsolutePath();
            System.out.println("[ShapeEditorFrame] Loading state from: " + filePath);

            // Create and execute the LoadStateCommand, passing both registries
            if (whiteBoard != null && toolbarPanel != null && compositeRegistry != null && prototypeRegistry != null) { // Check
                                                                                                                        // prototypeRegistry
                LoadStateCommand loadCommand = new LoadStateCommand(whiteBoard, toolbarPanel,
                        compositeRegistry, prototypeRegistry, filePath); // Pass prototypeRegistry
                // Use CommandHistory to allow undoing the load operation
                whiteBoard.getCommandHistory().executeCommand(loadCommand);
            } else {
                System.err.println(
                        "[ShapeEditorFrame] Cannot load: WhiteBoard, ToolbarPanel, or CompositeRegistry is null.");
            }
        } else {
            System.out.println("[ShapeEditorFrame] Load command cancelled by user.");
        }
    }

}
