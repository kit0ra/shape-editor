package com.editor.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.ShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.gui.panel.HorizontalPanel;
import com.editor.gui.panel.VerticalPanel;
import com.editor.shapes.PolygonShape;
import com.editor.shapes.Rectangle;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.utils.ImageLoader;

public class ShapeEditorFrame extends Frame {

    private HorizontalPanel horizontalPanel;
    private VerticalPanel verticalPanel;
    private WhiteBoard whiteBoard;

    // Button spacing and positioning constants
    private static final int HORIZONTAL_BUTTON_SPACING = 10;
    private static final int VERTICAL_BUTTON_SPACING = 15;

    // Shape prototype registry
    private ShapePrototypeRegistry prototypeRegistry;
    private static final int HORIZONTAL_INITIAL_OFFSET = 10;
    private static final int VERTICAL_INITIAL_OFFSET = 60; // Initial Y offset to position below horizontal panel
    private static final int BUTTON_LEFT_MARGIN = 10; // Left margin for buttons in vertical panel

    public ShapeEditorFrame() {
        super("Shape Editor");
        setSize(800, 600);
        setLayout(null);

        whiteBoard = new WhiteBoard(800, 600, Color.WHITE);
        whiteBoard.setRelativeBounds(20, 20, 80, 90); // fill remaining space
        whiteBoard.makeResponsiveTo(this);
        add(whiteBoard);

        horizontalPanel = new HorizontalPanel();
        horizontalPanel.setRelativeBounds(0, 10, 100, 10); // x=10%, y=10%, width=80%, height=10%
        horizontalPanel.makeResponsiveTo(this);
        add(horizontalPanel);

        verticalPanel = new VerticalPanel();
        verticalPanel.setRelativeBounds(0, 10, 20, 90); // x=10%, y=10%, width=10%, height=80%
        verticalPanel.makeResponsiveTo(this);
        add(verticalPanel);

        // Preload all icons
        ImageLoader.preloadImages(
                "icons/save.png",
                "icons/load.png",
                "icons/undo.png",
                "icons/redo.png",
                "icons/rectangle.png",
                "icons/polygon.png");

        // Initialize the shape prototype registry
        initializePrototypeRegistry();

        // ✅ Add this to close properly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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
     * Initializes the shape prototype registry with predefined shapes
     */
    private void initializePrototypeRegistry() {
        prototypeRegistry = new ShapePrototypeRegistry();

        // Register a rectangle prototype
        Rectangle rectanglePrototype = new Rectangle(0, 0, 80, 60);
        prototypeRegistry.registerPrototype("Rectangle", rectanglePrototype);

        // Register a polygon (triangle) prototype
        int[] xPoints = { 0, -40, 40 };
        int[] yPoints = { -40, 40, 40 };
        PolygonShape polygonPrototype = new PolygonShape(xPoints, yPoints, 3);
        prototypeRegistry.registerPrototype("Polygon", polygonPrototype);

        // Set the prototype registry in the whiteboard
        whiteBoard.setPrototypeRegistry(prototypeRegistry);
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
        horizontalPanel.addButton(undoButton);

        // Create redo button
        x += undoButton.getWidth() + HORIZONTAL_BUTTON_SPACING;
        IButton redoButton = createIconButton(x, 5, "icons/redo.png", "Redo the last undone action");
        horizontalPanel.addButton(redoButton);
    }

    /**
     * Sets up the vertical panel buttons (rectangle, polygon)
     */
    private void setupVerticalButtons() {
        int y = VERTICAL_INITIAL_OFFSET;

        // Create rectangle button (icon only with drag capability)
        IButton rectangleButton = createDraggableShapeButton(
                BUTTON_LEFT_MARGIN, y,
                "icons/rectangle.png",
                "Draw a rectangle",
                "Rectangle");
        verticalPanel.addButton(rectangleButton);

        // Create polygon button (icon only with drag capability)
        y += rectangleButton.getHeight() + VERTICAL_BUTTON_SPACING;
        IButton polygonButton = createDraggableShapeButton(
                BUTTON_LEFT_MARGIN, y,
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
