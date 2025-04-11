package com.editor.gui.button;

import java.awt.Image;

import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.utils.ImageLoader;

public class ButtonFactory {
    // Default sizing constants
    public static final int DEFAULT_HEIGHT = 40;
    public static final int DEFAULT_ICON_SIZE = 24;
    public static final int DEFAULT_PADDING = 8;
    public static final int DEFAULT_SPACING = 8;

    public static IButton createImageButton(
            int x, int y,
            String imagePath,
            String text,
            ImageDecorator.ImageMode mode) {

        // Calculate button dimensions based on mode
        int width, height = DEFAULT_HEIGHT;
        Image image = ImageLoader.loadImage(imagePath);

        switch (mode) {
            case FILL_BUTTON:
                width = height; // Square button for full-image
                break;
            case ICON_ONLY:
                width = DEFAULT_ICON_SIZE + DEFAULT_PADDING * 2;
                break;
            case ICON_AND_TEXT:
            default:
                width = DEFAULT_ICON_SIZE + DEFAULT_PADDING * 3 +
                        (text.isEmpty() ? 0 : text.length() * 7); // Approximate text width
        }

        // Create base button
        IButton button = new CustomButton(x, y, width, height, text);

        // Add image if loaded
        if (image != null) {
            button = new ImageDecorator(
                    button,
                    image,
                    DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE,
                    DEFAULT_PADDING,
                    mode);
        }

        return button;
    }

    public static IButton createToolbarButton(int x, int y, String imagePath) {
        return createImageButton(
                x, y,
                imagePath,
                "",
                ImageDecorator.ImageMode.ICON_ONLY);
    }

    public static IButton createTextWithIconButton(int x, int y, String imagePath, String text) {
        return createImageButton(
                x, y,
                imagePath,
                text,
                ImageDecorator.ImageMode.ICON_AND_TEXT);
    }

    public static IButton createImageOnlyButton(int x, int y, String imagePath) {
        return createImageButton(
                x, y,
                imagePath,
                "",
                ImageDecorator.ImageMode.FILL_BUTTON);
    }
}
