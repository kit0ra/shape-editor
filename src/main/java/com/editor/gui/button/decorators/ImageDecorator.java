package com.editor.gui.button.decorators;

import java.awt.Graphics;
import java.awt.Image;

import com.editor.gui.button.IButton;

public class ImageDecorator extends ButtonDecorator {
    public enum ImageMode {
        ICON_ONLY, 
        ICON_AND_TEXT, 
        FILL_BUTTON 
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
                
                imgX = getX();
                imgY = getY();
                imgWidth = getWidth();
                imgHeight = getHeight();
                break;

            case ICON_ONLY:
                
                imgWidth = Math.min(desiredWidth, getWidth() - padding * 2);
                imgHeight = Math.min(desiredHeight, getHeight() - padding * 2);
                imgX = getX() + (getWidth() - imgWidth) / 2;
                imgY = getY() + (getHeight() - imgHeight) / 2;
                break;

            case ICON_AND_TEXT:
            default:
                
                imgWidth = desiredWidth;
                imgHeight = desiredHeight;
                imgX = getX() + padding;
                imgY = getY() + (getHeight() - imgHeight) / 2;

                
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
