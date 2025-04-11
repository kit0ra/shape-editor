package com.editor.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.editor.gui.button.ButtonFactory;
import com.editor.gui.button.IButton;
import com.editor.gui.button.TooltipDecorator;
import com.editor.gui.panel.HorizontalPanel;
import com.editor.gui.panel.VerticalPanel;
import com.editor.shapes.RectangleFactory;
import com.editor.utils.ImageLoader;

public class ShapeEditorFrame extends Frame {

    private HorizontalPanel horizontalPanel;
    private VerticalPanel verticalPanel;
    private WhiteBoard whiteBoard;

    // Button spacing and positioning constants
    private static final int BUTTON_SPACING = 10;
    private static final int VERTICAL_BUTTON_SPACING = 15;
    private static final int VERTICAL_INITIAL_OFFSET = 80; // Initial Y offset to position below horizontal panel
    private static final int BUTTON_LEFT_MARGIN = 50; // Left margin for buttons in vertical panel

    public ShapeEditorFrame() {
        super("Shape Editor");
        setSize(800, 600);
        setLayout(null);

        whiteBoard = new WhiteBoard(800, 600, Color.WHITE);
        // Position whiteboard below horizontal panel (y=10) and right of vertical panel
        // (x=20)
        whiteBoard.setRelativeBounds(20, 10, 80, 90);
        whiteBoard.makeResponsiveTo(this);
        add(whiteBoard);

        horizontalPanel = new HorizontalPanel();
        // Position horizontal panel at the top
        horizontalPanel.setRelativeBounds(0, 0, 100, 10); // x=0%, y=0%, width=100%, height=10%
        horizontalPanel.makeResponsiveTo(this);
        add(horizontalPanel);

        verticalPanel = new VerticalPanel();
        // Position vertical panel below horizontal (y=10), on the left (x=0)
        verticalPanel.setRelativeBounds(0, 10, 20, 90); // x=0%, y=10%, width=20%, height=90%
        verticalPanel.makeResponsiveTo(this);
        add(verticalPanel);

        // Preload icons
        ImageLoader.preloadImages(
                "icons/save.png",
                "icons/load.png",
                "icons/undo.png",
                "icons/redo.png",
                "icons/rectangle.png",
                "icons/polygon.png");

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
        // Initialize buttons with tooltips
        initializeHorizontalButtons();
        initializeVerticalButtons();
    }

    private void initializeHorizontalButtons() {
        // Create save button with tooltip
        int x = BUTTON_SPACING;

        // Create save button (icon only)
        IButton saveButton = ButtonFactory.createToolbarButton(
                x, 5, "icons/save.png");

        // Add tooltip to save button
        saveButton = new TooltipDecorator(saveButton, "Save the current drawing");

        // Add to panel
        horizontalPanel.addButton(saveButton);

        // Create load button with tooltip (position it after the save button)
        x += saveButton.getWidth() + BUTTON_SPACING;

        IButton loadButton = ButtonFactory.createToolbarButton(
                x, 5, "icons/load.png");

        // Add tooltip to load button
        loadButton = new TooltipDecorator(loadButton, "Load a saved drawing");

        // Add to panel
        horizontalPanel.addButton(loadButton);

        // Create undo button with tooltip (position it after the load button)
        x += loadButton.getWidth() + BUTTON_SPACING;

        IButton undoButton = ButtonFactory.createToolbarButton(
                x, 5, "icons/undo.png");

        // Add tooltip to undo button
        undoButton = new TooltipDecorator(undoButton, "Undo the last action");

        // Add to panel
        horizontalPanel.addButton(undoButton);

        // Create redo button with tooltip (position it after the undo button)
        x += undoButton.getWidth() + BUTTON_SPACING;

        IButton redoButton = ButtonFactory.createToolbarButton(
                x, 5, "icons/redo.png");

        // Add tooltip to redo button
        redoButton = new TooltipDecorator(redoButton, "Redo the last undone action");

        // Add to panel
        horizontalPanel.addButton(redoButton);
    }

    private void initializeVerticalButtons() {
        // Create rectangle button with tooltip
        int y = VERTICAL_INITIAL_OFFSET;

        // Create rectangle button (icon only)
        IButton rectangleButton = ButtonFactory.createToolbarButton(
                BUTTON_LEFT_MARGIN, y, "icons/rectangle.png");

        // Add tooltip to rectangle button
        rectangleButton = new TooltipDecorator(rectangleButton, "Draw a rectangle");
        // Use setOnAction to assign the click behavior
        rectangleButton.setOnAction(() -> {
            whiteBoard.setCurrentShapeFactory(new RectangleFactory());
        });

        // Add to panel
        verticalPanel.addButton(rectangleButton);

        // Create polygon button with tooltip (position it below the rectangle button)
        y += rectangleButton.getHeight() + VERTICAL_BUTTON_SPACING;

        IButton polygonButton = ButtonFactory.createToolbarButton(
                BUTTON_LEFT_MARGIN, y, "icons/polygon.png");

        // Add tooltip to polygon button
        polygonButton = new TooltipDecorator(polygonButton, "Draw a polygon");

        // Add to panel
        verticalPanel.addButton(polygonButton);
    }

    public void launch() {
        setVisible(true);
    }

    public static void main(String[] args) {
        ShapeEditorFrame frame = new ShapeEditorFrame();
        frame.launch();
    }

}
