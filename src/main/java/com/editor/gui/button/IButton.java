package com.editor.gui.button;

import java.awt.Graphics;

public interface IButton {
    void draw(Graphics g);

    boolean isMouseOver(int mx, int my);

    /**
     * Returns whether the button is currently being hovered over
     *
     * @return true if the mouse is currently over the button
     */
    boolean isCurrentlyHovered();

    void onClick();

    void onMouseOver();

    void onMouseOut();

    /**
     * Sets the action to be performed when the button is clicked.
     * 
     * @param action The Runnable action to execute.
     */
    void setOnAction(Runnable action);

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    String getText();
}
