package com.editor.gui.button;

import java.awt.Color;
import java.awt.Graphics;

public class CustomButton implements IButton {

    private final int x, y, width, height;
    private final String text;
    private boolean isMouseOver = false; // Tracks hover state

    public CustomButton(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    @Override
    public boolean isMouseOver(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    @Override
    public boolean isCurrentlyHovered() {
        return isMouseOver;
    }

    @Override
    public void onClick() {
        System.out.println("Button clicked!");
    }

    @Override
    public void onMouseOver() {
        isMouseOver = true;
    }

    @Override
    public void onMouseOut() {
        isMouseOver = false;
    }

    // Button colors
    private static final Color BUTTON_COLOR = Color.LIGHT_GRAY;
    private static final Color BUTTON_HOVER_COLOR = new Color(200, 200, 200);

    @Override
    public void draw(Graphics g) {
        if (isMouseOver) {
            g.setColor(BUTTON_HOVER_COLOR);
        } else {
            g.setColor(BUTTON_COLOR);
        }
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        g.drawString(text, x + 10, y + 20);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public String getText() {
        return text;
    }
}
