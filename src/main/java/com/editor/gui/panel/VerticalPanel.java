package com.editor.gui.panel;

import java.awt.Color;
import java.awt.Graphics;

public class VerticalPanel extends CustomPanel {
    public VerticalPanel() {
        super();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        // g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        // draw vertical line
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
    }
}
