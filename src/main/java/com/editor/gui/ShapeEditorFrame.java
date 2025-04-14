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
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.gui.button.decorators.UndoButtonDecorator;
import com.editor.gui.button.factory.ShapeButtonFactory;
import com.editor.gui.panel.HorizontalPanel;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.gui.panel.TrashPanel;
import com.editor.gui.panel.VerticalPanel;
import com.editor.mediator.DragMediator;
import com.editor.mediator.ShapeDragMediator;
import com.editor.shapes.Circle;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.state.AutoSaveManager;
import com.editor.state.StateChangeListener;
import com.editor.utils.ImageLoader;

public class ShapeEditorFrame extends Frame {

    private final HorizontalPanel horizontalPanel;
    private final VerticalPanel verticalPanel;
    private final ToolbarPanel toolbarPanel;
    private final TrashPanel trashPanel;
    private final WhiteBoard whiteBoard;

    
    private DragMediator dragMediator;

    
    private static final int HORIZONTAL_BUTTON_SPACING = 10;

    
    private ShapePrototypeRegistry prototypeRegistry;
    private CompositeShapePrototypeRegistry compositeRegistry; 

    
    private AutoSaveManager autoSaveManager;
    private static final int HORIZONTAL_INITIAL_OFFSET = 10;
    private static final int BUTTON_LEFT_MARGIN = 10; 

    public ShapeEditorFrame() {
        super("Shape Editor");
        setSize(800, 600);
        setLayout(null);

        whiteBoard = new WhiteBoard(800, 600, Color.WHITE);
        whiteBoard.setRelativeBounds(20, 30, 80, 70); 
        add(whiteBoard);

        horizontalPanel = new HorizontalPanel();
        horizontalPanel.setRelativeBounds(0, 10, 100, 10); 
        add(horizontalPanel);

        verticalPanel = new VerticalPanel();
        verticalPanel.setRelativeBounds(0, 20, 20, 10); 
        verticalPanel.setTargetWhiteBoard(whiteBoard); 
        add(verticalPanel);

        
        toolbarPanel = new ToolbarPanel();
        toolbarPanel.setRelativeBounds(0, 30, 20, 60); 
        toolbarPanel.setTargetWhiteBoard(whiteBoard);
        add(toolbarPanel);

        
        trashPanel = new TrashPanel();
        trashPanel.setRelativeBounds(0, 90, 20, 10); 
        trashPanel.setTargetWhiteBoard(whiteBoard);
        add(trashPanel);

        
        ImageLoader.preloadImages(
                "icons/save.png",
                "icons/load.png",
                "icons/undo.png",
                "icons/redo.png",
                "icons/rectangle.png",
                "icons/polygon.png",
                "icons/circle.png");

        
        initializePrototypeRegistry();
        initializeCompositeRegistry(); 

        
        setupDragMediator();

        
        setupAutoSaveManager();

        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                
                if (autoSaveManager != null) {
                    System.out.println("[LOG] ShapeEditorFrame.windowClosing - Performing final auto-save...");
                    autoSaveManager.shutdown(); 
                    System.out.println("[LOG] ShapeEditorFrame.windowClosing - Final auto-save complete.");
                } else {
                    System.out.println(
                            "[LOG] ShapeEditorFrame.windowClosing - AutoSaveManager is null, skipping final save.");
                }

                
                System.out.println("[LOG] ShapeEditorFrame.windowClosing - Disposing whiteboard...");
                if (whiteBoard != null) {
                    whiteBoard.dispose();
                }

                System.out.println("[LOG] ShapeEditorFrame.windowClosing - Disposing frame...");
                dispose(); 
                System.out.println("[LOG] ShapeEditorFrame.windowClosing - Exiting application...");
                System.exit(0);
            }
        });

        initializeResponsiveness();
        init();
    }

    private void initializeResponsiveness() {
        
        whiteBoard.makeResponsiveTo(this);
        horizontalPanel.makeResponsiveTo(this);
        verticalPanel.makeResponsiveTo(this);
        toolbarPanel.makeResponsiveTo(this);
        trashPanel.makeResponsiveTo(this);
    }

    private void init() {
        
        setupHorizontalButtons();
        setupVerticalButtons();
    }

    /**
     * Sets up the drag mediator to handle drag operations between components
     */
    private void setupDragMediator() {
        
        dragMediator = new ShapeDragMediator();

        
        dragMediator.setDebugEnabled(true);

        
        dragMediator.registerWhiteBoard(whiteBoard);

        
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
        
        autoSaveManager = AutoSaveManager.initialize(whiteBoard, toolbarPanel, compositeRegistry, prototypeRegistry);

        
        StateChangeListener stateChangeListener = (source, description) -> {
            System.out.println("[AutoSave] State changed: " + description);
            autoSaveManager.triggerAutoSave();
        };

        
        whiteBoard.setStateChangeListener(stateChangeListener);
        toolbarPanel.setStateChangeListener(stateChangeListener);

        
        System.out.println("[LOG] ShapeEditorFrame - Setting registries on ToolbarPanel...");
        toolbarPanel.setPrototypeRegistry(prototypeRegistry);
        toolbarPanel.setCompositePrototypeRegistry(compositeRegistry); 

        
        if (autoSaveManager.autoSaveExists()) {
            System.out.println("[LOG] ShapeEditorFrame - Found previous state, auto-loading...");
            
            AutoLoadCommand autoLoadCommand = new AutoLoadCommand(
                    whiteBoard, toolbarPanel, compositeRegistry, prototypeRegistry);
            autoLoadCommand.execute();
        } else {
            System.out.println("[AutoSave] No previous state found, starting with empty state.");
        }

        

        
        dragMediator.registerTrashPanel(trashPanel);

        
        dragMediator.registerToolbarPanel(toolbarPanel);

        
        whiteBoard.setToolbarPanel(toolbarPanel);

        

        System.out.println("Drag mediator initialized and connected to components");
    }

    /**
     * Initializes the shape prototype registry with predefined shapes
     */
    private void initializePrototypeRegistry() {
        prototypeRegistry = new ShapePrototypeRegistry();

        
        Color lightPink = new Color(255, 182, 193);

        
        Rectangle rectanglePrototype = new Rectangle(0, 0, 80, 60);
        rectanglePrototype.setFillColor(lightPink);
        rectanglePrototype.setBorderColor(Color.BLACK);
        prototypeRegistry.registerPrototype("Rectangle", rectanglePrototype);

        
        int radius = 40; 
        int numPoints = 6; 

        
        RegularPolygon polygonPrototype = new RegularPolygon(0, 0, radius, numPoints);
        polygonPrototype.setFillColor(lightPink);
        polygonPrototype.setBorderColor(Color.BLACK);
        prototypeRegistry.registerPrototype("Polygon", polygonPrototype);

        
        Circle circlePrototype = new Circle(0, 0, 40);
        circlePrototype.setFillColor(lightPink);
        circlePrototype.setBorderColor(Color.BLACK);
        prototypeRegistry.registerPrototype("Circle", circlePrototype);

        
        
        
        try {
            
            Rectangle defaultRect = (Rectangle) prototypeRegistry.createShape("Rectangle", 0, 0);
            System.out.println("Rectangle fill color: " + defaultRect.getFillColor());

            
            RegularPolygon defaultPoly = (RegularPolygon) prototypeRegistry.createShape("Polygon", 0, 0);
            System.out.println("Polygon fill color: " + defaultPoly.getFillColor());

            
            Circle defaultCircle = (Circle) prototypeRegistry.createShape("Circle", 0, 0);
            System.out.println("Circle fill color: " + defaultCircle.getFillColor());
        } catch (Exception e) {
            System.err.println("Error updating default colors: " + e.getMessage());
        }

        
        whiteBoard.setPrototypeRegistry(prototypeRegistry);
    }

    /**
     * Initializes the composite shape prototype registry.
     */
    private void initializeCompositeRegistry() {
        compositeRegistry = new CompositeShapePrototypeRegistry();
        
        
        System.out.println("Composite shape registry initialized.");
    }

    /**
     * Sets up the horizontal panel buttons (save, load, undo, redo)
     */
    private void setupHorizontalButtons() {
        int x = HORIZONTAL_INITIAL_OFFSET;

        
        IButton saveButtonBase = createIconButton(x, 5, "icons/save.png", "Save the current drawing");
        
        IButton saveButton = new ButtonDecorator(saveButtonBase) {
            @Override
            public void onClick() {
                super.onClick(); 
                handleSaveState(); 
            }
            
        };
        horizontalPanel.addButton(saveButton); 

        
        x += saveButton.getWidth() + HORIZONTAL_BUTTON_SPACING; 
        IButton loadButtonBase = createIconButton(x, 5, "icons/load.png", "Load a saved drawing");
        
        IButton loadButton = new ButtonDecorator(loadButtonBase) {
            @Override
            public void onClick() {
                super.onClick(); 
                handleLoadState(); 
            }
            
        };
        horizontalPanel.addButton(loadButton); 

        
        x += loadButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton undoButton = createIconButton(x, 5, "icons/undo.png", "Undo the last action");
        undoButton = new UndoButtonDecorator(undoButton, whiteBoard); 
        horizontalPanel.addButton(undoButton);

        
        x += undoButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton redoButton = createIconButton(x, 5, "icons/redo.png", "Redo the last undone action");
        redoButton = new RedoButtonDecorator(redoButton, whiteBoard); 
        horizontalPanel.addButton(redoButton);

        
        
        
    }

    /**
     * Sets up the vertical panel buttons (rectangle, polygon) in a horizontal
     * layout
     */
    private void setupVerticalButtons() {
        
        int y = 5; 
        int x = BUTTON_LEFT_MARGIN;

        
        ShapeButtonFactory buttonFactory = new ShapeButtonFactory(whiteBoard, prototypeRegistry,
                whiteBoard.getCommandHistory());
        buttonFactory.setDragMediator(dragMediator);

        
        IButton rectangleButton = buttonFactory.createButton(
                x, y,
                "icons/rectangle.png",
                "Draw a rectangle",
                "Rectangle");
        verticalPanel.addButton(rectangleButton);

        
        
        x += rectangleButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton polygonButton = buttonFactory.createButton(
                x, y,
                "icons/polygon.png",
                "Draw a polygon",
                "Polygon");
        verticalPanel.addButton(polygonButton);

        
        
        x += polygonButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton circleButton = buttonFactory.createButton(
                x, y,
                "icons/circle.png",
                "Draw a circle",
                "Circle");
        verticalPanel.addButton(circleButton);
    }

    /**
     * Helper method to create an icon-only button with tooltip
     */
    private IButton createIconButton(int x, int y, String iconPath, String tooltipText) {
        
        Image icon = ImageLoader.loadImage(iconPath);

        
        IButton button = new CustomButton(x, y, 40, 40, "");

        
        if (icon != null) {
            button = new ImageDecorator(
                    button,
                    icon,
                    24, 24, 
                    8, 
                    ImageDecorator.ImageMode.ICON_ONLY 
            );
        }

        
        return new TooltipDecorator(button, tooltipText);
    }

    public void launch() {
        setVisible(true);
    }

    public static void main(String[] args) {
        ShapeEditorFrame frame = new ShapeEditorFrame();
        frame.launch();
    }

    

    private void handleSaveState() {
        System.out.println("[ShapeEditorFrame] Save button clicked.");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Application State");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Shape Editor State (*.ser)", "ser"));
        fileChooser.setSelectedFile(new File("editor_state.ser")); 

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".ser")) {
                filePath += ".ser";
            }
            System.out.println("[ShapeEditorFrame] Saving state to: " + filePath);

            
            
            
            if (whiteBoard != null && toolbarPanel != null && compositeRegistry != null && prototypeRegistry != null) { 
                                                                                                                        
                
                SaveStateCommand saveCommand = new SaveStateCommand(whiteBoard, toolbarPanel,
                        compositeRegistry, prototypeRegistry, filePath); 
                
                saveCommand.execute();
                
                
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

            
            if (whiteBoard != null && toolbarPanel != null && compositeRegistry != null && prototypeRegistry != null) { 
                                                                                                                        
                LoadStateCommand loadCommand = new LoadStateCommand(whiteBoard, toolbarPanel,
                        compositeRegistry, prototypeRegistry, filePath); 
                
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
