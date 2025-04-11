package com.editor.gui.button;

import java.awt.Graphics;

public interface IButton {
    void draw(Graphics g);

    boolean isMouseOver(int mx, int my);

    void onClick();

    void onMouseOver();

    void onMouseOut();

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    String getText();
}
