package com.editor.gui.button;

import java.awt.Graphics;
import java.awt.Image;

public class ImageDecorator extends ButtonDecorator {
    private final Image image;
    private final int iconWidth, iconHeight;
    private final int iconPadding;

    public ImageDecorator(IButton decoratedButton, Image image, int iconWidth, int iconHeight, int iconPadding) {
        super(decoratedButton);
        this.image = image;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.iconPadding = iconPadding;
    }

    @Override
    public void draw(Graphics g) {
        // Draw the base button (background, borders, etc.)
        super.draw(g);

        if (image != null) {
            // Calculate icon position (centered vertically, left-aligned)
            int iconX = decoratedButton.getX() + iconPadding;
            int iconY = decoratedButton.getY() + (decoratedButton.getHeight() - iconHeight) / 2;

            // Draw the icon
            g.drawImage(image, iconX, iconY, iconWidth, iconHeight, null);

            // Adjust text position to avoid overlapping with the icon
            int textX = iconX + iconWidth + iconPadding;
            int textY = decoratedButton.getY() + (decoratedButton.getHeight() + g.getFontMetrics().getAscent()) / 2;

            // Draw the text (assuming CustomButton has a getText() method)
            g.drawString(decoratedButton.getText(), textX, textY);
        }
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
