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
    public boolean isCurrentlyHovered() {
        return decoratedButton.isCurrentlyHovered();
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

    @Override
    public int getX() {
        return decoratedButton.getX();
    }

    @Override
    public int getY() {
        return decoratedButton.getY();
    }

    @Override
    public int getWidth() {
        return decoratedButton.getWidth();
    }

    @Override
    public int getHeight() {
        return decoratedButton.getHeight();
    }

    @Override
    public String getText() {
        return decoratedButton.getText();
    }
}
