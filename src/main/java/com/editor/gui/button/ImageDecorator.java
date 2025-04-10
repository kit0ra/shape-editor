package com.editor.gui.button;

import java.awt.Graphics;
import java.awt.Image;

public class ImageDecorator extends ButtonDecorator {
    public enum ImageMode {
        ICON_ONLY, // Button is just the image (centered)
        ICON_AND_TEXT, // Standard icon + text layout
        FILL_BUTTON // Image stretches to fill entire button
    }

    private final Image image;
    private final int desiredWidth, desiredHeight;
    private final int padding;
    private final ImageMode mode;

    public ImageDecorator(IButton decoratedButton, Image image,
            int desiredWidth, int desiredHeight,
            int padding, ImageMode mode) {
        super(decoratedButton);
        this.image = image;
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;
        this.padding = padding;
        this.mode = mode;
    }

    // Convenience constructor with default mode
    public ImageDecorator(IButton decoratedButton, Image image,
            int desiredWidth, int desiredHeight,
            int padding) {
        this(decoratedButton, image, desiredWidth, desiredHeight,
                padding, ImageMode.ICON_AND_TEXT);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        if (image == null)
            return;

        int imgX, imgY, imgWidth, imgHeight;

        switch (mode) {
            case FILL_BUTTON:
                // Fill entire button with image
                imgX = getX();
                imgY = getY();
                imgWidth = getWidth();
                imgHeight = getHeight();
                break;

            case ICON_ONLY:
                // Center image in button
                imgWidth = Math.min(desiredWidth, getWidth() - padding * 2);
                imgHeight = Math.min(desiredHeight, getHeight() - padding * 2);
                imgX = getX() + (getWidth() - imgWidth) / 2;
                imgY = getY() + (getHeight() - imgHeight) / 2;
                break;

            case ICON_AND_TEXT:
            default:
                // Standard icon + text layout
                imgWidth = desiredWidth;
                imgHeight = desiredHeight;
                imgX = getX() + padding;
                imgY = getY() + (getHeight() - imgHeight) / 2;

                // Draw text if exists
                String text = getText();
                if (text != null && !text.isEmpty()) {
                    int textX = imgX + imgWidth + padding;
                    int textY = getY() + (getHeight() + g.getFontMetrics().getAscent()) / 2;
                    g.drawString(text, textX, textY);
                }
                break;
        }

        g.drawImage(image, imgX, imgY, imgWidth, imgHeight, null);
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
