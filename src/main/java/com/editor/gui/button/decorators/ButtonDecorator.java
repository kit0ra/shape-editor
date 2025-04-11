package com.editor.gui.button.decorators;

import java.awt.Graphics;

import com.editor.gui.button.IButton;

public abstract class ButtonDecorator implements IButton {
    protected IButton decoratedButton;

    public ButtonDecorator(IButton decoratedButton) {
        this.decoratedButton = decoratedButton;
    }

    @Override
    public void draw(Graphics g) {
        decoratedButton.draw(g);
    }

    @Override
    public boolean isMouseOver(int mx, int my) {
        return decoratedButton.isMouseOver(mx, my);
    }

    @Override
    public void onClick() {
        decoratedButton.onClick();
    }

    @Override
    public void onMouseOver() {
        decoratedButton.onMouseOver();
    }

    @Override
    public void onMouseOut() {
        decoratedButton.onMouseOut();
    }
}
