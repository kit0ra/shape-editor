package com.editor.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.ImageDecorator;
import com.editor.gui.panel.HorizontalPanel;
import com.editor.gui.panel.VerticalPanel;
import com.editor.utils.ImageLoader;

public class ShapeEditorFrame extends Frame {

    private HorizontalPanel horizontalPanel;
    private VerticalPanel verticalPanel;
    private WhiteBoard whiteBoard;

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

        // Load icons
        Image saveIcon = ImageLoader.loadImage("icons/save.png");
        Image loadIcon = ImageLoader.loadImage("icons/load.png");

        // Constants for consistent UI
        final int BUTTON_HEIGHT = 40;
        final int ICON_SIZE = 24;
        final int BUTTON_PADDING = 8;
        final int BUTTON_SPACING = 10;

        // Create Save button (icon only)
        IButton saveButton = new CustomButton(
                20, // x
                5, // y
                ICON_SIZE + BUTTON_PADDING * 2, // width
                BUTTON_HEIGHT, // height
                "" // no text
        );

        if (saveIcon != null) {
            saveButton = new ImageDecorator(
                    saveButton,
                    saveIcon,
                    ICON_SIZE, ICON_SIZE,
                    BUTTON_PADDING,
                    ImageDecorator.ImageMode.ICON_ONLY);
        } else {
            System.err.println("Failed to load save icon");
        }

        // Create Load button (icon + text)
        IButton loadButton = new CustomButton(
                saveButton.getX() + saveButton.getWidth() + BUTTON_SPACING, // x position after save button
                5, // same y as save button
                ICON_SIZE + BUTTON_PADDING * 3 + 40, // width (icon + padding + text space)
                BUTTON_HEIGHT, // same height
                "Load" // button text
        );

        if (loadIcon != null) {
            loadButton = new ImageDecorator(
                    loadButton,
                    loadIcon,
                    ICON_SIZE, ICON_SIZE,
                    BUTTON_PADDING,
                    ImageDecorator.ImageMode.ICON_AND_TEXT);
        } else {
            System.err.println("Failed to load load icon");
            // Fallback to text-only button
            loadButton = new CustomButton(
                    loadButton.getX(), loadButton.getY(),
                    80, // wider to accommodate text
                    BUTTON_HEIGHT,
                    "Load");
        }

        // Add buttons to panel
        horizontalPanel.addButton(saveButton);
        horizontalPanel.addButton(loadButton);

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
        // Initialize the shape editor frame components
        // ...
    }

    public void launch() {
        setVisible(true);
    }

    public static void main(String[] args) {
        ShapeEditorFrame frame = new ShapeEditorFrame();
        frame.launch();
    }

}
