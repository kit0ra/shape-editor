package com.editor.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
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
import com.editor.mediator.ShapeDragMediator;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;
import com.editor.shapes.ShapePrototypeRegistry; // Added
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

        // Add window listener to handle closing properly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Clean up resources
                if (whiteBoard != null) {
                    whiteBoard.dispose();
                }
                dispose();
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

        // Register the trash panel with the mediator
        dragMediator.registerTrashPanel(trashPanel);

        // Register the toolbar panel with the mediator
        dragMediator.registerToolbarPanel(toolbarPanel);

        // Set the prototype registry in the toolbar panel
        toolbarPanel.setPrototypeRegistry(prototypeRegistry);

        // Set the toolbar panel reference in the whiteboard
        whiteBoard.setToolbarPanel(toolbarPanel);

        // Set the composite registry in the toolbar panel
        toolbarPanel.setCompositePrototypeRegistry(compositeRegistry); // Added

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

        // Create save button (icon only)
        IButton saveButton = createIconButton(x, 5, "icons/save.png", "Save the current drawing");
        horizontalPanel.addButton(saveButton);

        // Create load button
        x += saveButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton loadButton = createIconButton(x, 5, "icons/load.png", "Load a saved drawing");
        horizontalPanel.addButton(loadButton);

        // Create undo button
        x += loadButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton undoButton = createIconButton(x, 5, "icons/undo.png", "Undo the last action");
        undoButton = new UndoButtonDecorator(undoButton, whiteBoard);
        horizontalPanel.addButton(undoButton);

        // Create redo button
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

}
