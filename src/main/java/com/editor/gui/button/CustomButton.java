package com.editor.gui.button;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CustomButton implements IButton {

    private int x, y, width, height;
    private String text;
    private boolean isMouseOver = false;
    private List<ActionListener> listeners = new ArrayList<>();

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
        fireActionPerformed();
    }

    @Override
    public void onMouseOver() {
        isMouseOver = true;
    }

    @Override
    public void onMouseOut() {
        isMouseOver = false;
    }

    @Override
    public void draw(Graphics g) {
        if (isMouseOver) {
            g.setColor(Color.GRAY);
        } else {
            g.setColor(Color.LIGHT_GRAY);
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

    /**
     * Implements the IButton interface method to set the action.
     * Delegates to the existing onClick(Runnable) method.
     */
    @Override
    public void setOnAction(Runnable action) {
        // Clear existing listeners? Or add to them? Let's add for now.
        // If only one action is desired, could clear listeners first:
        // this.listeners.clear();
        this.onClick(action);
    }

    // Keep the original overloaded methods for flexibility if needed directly
    public void onClick(Runnable action) {
        listeners.add(e -> action.run());
    }

    public void onClick(ActionListener listener) {
        listeners.add(listener);
    }

    private void fireActionPerformed() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

}
