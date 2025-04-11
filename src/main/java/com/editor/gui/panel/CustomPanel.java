
package com.editor.gui.panel;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import com.editor.gui.button.IButton;

public class CustomPanel extends Canvas {
    private int relX, relY, relWidth, relHeight;

    private List<IButton> buttons = new ArrayList<>();

    public CustomPanel() {

        setBackground(java.awt.Color.decode("#F6E9D7"));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (IButton button : buttons) {
                    if (button.isMouseOver(e.getX(), e.getY())) {
                        button.onClick();
                    }
                }
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                for (IButton button : buttons) {
                    if (button.isMouseOver(e.getX(), e.getY())) {
                        button.onMouseOver();
                    } else {
                        button.onMouseOut();
                    }
                }
                repaint();
            }
        });
    }

    public void addButton(IButton button) {
        buttons.add(button);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (IButton button : buttons) {
            button.draw(g);
        }
    }

    public void setRelativeBounds(int xPercent, int yPercent, int widthPercent, int heightPercent) {
        this.relX = xPercent;
        this.relY = yPercent;
        this.relWidth = widthPercent;
        this.relHeight = heightPercent;
    }

    public void applyResponsiveBounds(Dimension parentSize) {
        int x = parentSize.width * relX / 100;
        int y = parentSize.height * relY / 100;
        int w = parentSize.width * relWidth / 100;
        int h = parentSize.height * relHeight / 100;
        setBounds(x, y, w, h);
    }

    public void makeResponsiveTo(Frame frame) {
        applyResponsiveBounds(frame.getSize()); // initial
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                applyResponsiveBounds(frame.getSize());
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

}
