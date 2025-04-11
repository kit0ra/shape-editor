package com.editor.gui.panel;

import java.awt.Graphics;

public class VerticalPanel extends CustomPanel {
    public VerticalPanel() {
        super();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // No longer drawing the vertical line at the right edge
        // This prevents shapes from disappearing when moved near the edge
    }
}
