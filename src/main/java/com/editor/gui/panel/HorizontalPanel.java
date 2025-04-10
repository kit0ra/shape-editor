package com.editor.gui.panel;

import java.awt.Color;
import java.awt.Graphics;

public class HorizontalPanel extends CustomPanel {

    public HorizontalPanel() {
        super();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }
}
