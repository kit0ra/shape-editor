package com.editor.gui.button;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * A decorator that adds tooltip functionality to a button.
 * The tooltip is displayed when the mouse hovers over the button.
 */
public class TooltipDecorator extends ButtonDecorator {
    private String tooltip;
    private boolean showTooltip = false;
    private Color tooltipBgColor = new Color(255, 255, 225); // Light yellow
    private Color tooltipTextColor = Color.BLACK;
    private int tooltipPadding = 5;
    private Font tooltipFont = new Font("SansSerif", Font.PLAIN, 12);

    public TooltipDecorator(IButton decoratedButton, String tooltip) {
        super(decoratedButton);
        this.tooltip = tooltip;
    }

    @Override
    public void draw(Graphics g) {
        // Draw the base button
        super.draw(g);

        // Draw tooltip if mouse is over
        if (showTooltip && tooltip != null && !tooltip.isEmpty()) {
            // Save original font and colors
            Font originalFont = g.getFont();
            Color originalColor = g.getColor();

            // Set tooltip font and calculate dimensions
            g.setFont(tooltipFont);
            int textWidth = g.getFontMetrics().stringWidth(tooltip);
            int textHeight = g.getFontMetrics().getHeight();

            // Calculate tooltip position (above the button)
            int tooltipX = decoratedButton.getX() + (decoratedButton.getWidth() - textWidth) / 2;
            int tooltipY = decoratedButton.getY() - textHeight - tooltipPadding * 2;

            // Ensure tooltip stays within visible area
            if (tooltipY < 0) {
                // If not enough space above, show below
                tooltipY = decoratedButton.getY() + decoratedButton.getHeight() + tooltipPadding;
            }

            // Draw tooltip background
            g.setColor(tooltipBgColor);
            g.fillRect(tooltipX - tooltipPadding,
                    tooltipY - textHeight,
                    textWidth + tooltipPadding * 2,
                    textHeight + tooltipPadding);

            // Draw tooltip border
            g.setColor(Color.DARK_GRAY);
            g.drawRect(tooltipX - tooltipPadding,
                    tooltipY - textHeight,
                    textWidth + tooltipPadding * 2,
                    textHeight + tooltipPadding);

            // Draw tooltip text
            g.setColor(tooltipTextColor);
            g.drawString(tooltip, tooltipX, tooltipY - tooltipPadding);

            // Restore original font and color
            g.setFont(originalFont);
            g.setColor(originalColor);
        }
    }

    @Override
    public void onMouseOver() {
        super.onMouseOver();
        showTooltip = true;
    }

    @Override
    public void onMouseOut() {
        super.onMouseOut();
        showTooltip = false;
    }

    /**
     * The tooltip is shown when the button is hovered over
     * 
     * @return true if the tooltip is currently being shown
     */
    public boolean isTooltipVisible() {
        return showTooltip;
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

    public String getTooltip() {
        return tooltip;
    }
}
