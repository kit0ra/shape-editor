package com.editor.gui.button.factory;

import com.editor.gui.button.IButton;

/**
 * Interface for button factories that create different types of buttons.
 * This follows the Factory Method pattern to standardize button creation.
 */
public interface ButtonFactory {
    /**
     * Creates a button with the specified parameters.
     *
     * @param x The x-coordinate of the button
     * @param y The y-coordinate of the button
     * @param iconPath The path to the icon image
     * @param tooltipText The tooltip text for the button
     * @param typeKey The type key for the button (e.g., shape type)
     * @return A new button instance
     */
    IButton createButton(int x, int y, String iconPath, String tooltipText, String typeKey);
}
