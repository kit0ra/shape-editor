package com.editor.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.ImageDecorator;
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

        // Load the icon
        Image saveIcon = ImageLoader.loadImage("icons/save.png");

        // Create the base button (make it wider to fit icon + text)
        IButton saveButton = new CustomButton(0, 0, 130, 30, "Save"); // Wider width (130 instead of 100)

        // Decorate with icon if loaded
        if (saveIcon != null) {
            saveButton = new ImageDecorator(
                    saveButton,
                    saveIcon,
                    20, 20, // Icon dimensions
                    5 // Padding between icon and text
            );
        }

        horizontalPanel.addButton(saveButton);

        IButton loadButton = new CustomButton(0, 30, 100, 30, "Load");

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
