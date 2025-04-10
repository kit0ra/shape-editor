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
import com.editor.utils.ImageLoader;

public class ShapeEditorFrame extends Frame {

    private HorizontalPanel horizontalPanel;
    private VerticalPanel verticalPanel;
    private WhiteBoard whiteBoard;

    // Button spacing for horizontal panel
    private static final int BUTTON_SPACING = 10;

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

        // Preload icons
        ImageLoader.preloadImages(
                "icons/save.png",
                "icons/load.png",
                "icons/undo.png",
                "icons/redo.png");

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
        initializeButtons();
    }

    private void initializeButtons() {
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

    public void launch() {
        setVisible(true);
    }

    public static void main(String[] args) {
        ShapeEditorFrame frame = new ShapeEditorFrame();
        frame.launch();
    }

}
