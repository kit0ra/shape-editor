package com.editor.gui.panel;

import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import com.editor.shapes.Shape;

public class CompositePanel extends Panel {
    private List<Shape> shapes = new ArrayList<>();

    public CompositePanel() {
        // Add mouse listeners to handle shape drawing and selection
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Handle mouse press event
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Handle mouse release event
            }
        });
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Shape shape : shapes) {
            shape.draw(g);
        }
    }
}
