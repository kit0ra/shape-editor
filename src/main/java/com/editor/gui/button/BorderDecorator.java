package com.editor.gui.button;

import java.awt.Color;
import java.awt.Graphics;

public class BorderDecorator extends ButtonDecorator {
    private Color borderColor;
    private int thickness;

    public BorderDecorator(IButton decoratedButton, Color borderColor, int thickness) {
        super(decoratedButton);
        this.borderColor = borderColor;
        this.thickness = thickness;
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        Color oldColor = g.getColor();
        g.setColor(borderColor);
        for (int i = 0; i < thickness; i++) {
            g.drawRect(decoratedButton.getX() - i, decoratedButton.getY() - i,
                    decoratedButton.getWidth() + 2 * i, decoratedButton.getHeight() + 2 * i);
        }
        g.setColor(oldColor);
    }

    @Override
    public int getX() {
        return decoratedButton.getX() - thickness;
    }

    @Override
    public int getY() {
        return decoratedButton.getY() - thickness;
    }

    @Override
    public int getWidth() {
        return decoratedButton.getWidth() + 2 * thickness;
    }

    @Override
    public int getHeight() {
        return decoratedButton.getHeight() + 2 * thickness;
    }

    @Override
    public String getText() {
        return decoratedButton.getText();
    }
}
