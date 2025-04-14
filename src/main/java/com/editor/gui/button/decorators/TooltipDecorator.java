package com.editor.gui.button.decorators;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import com.editor.gui.button.IButton;

/**
 * A decorator that adds tooltip functionality to a button.
 * The tooltip is displayed when the mouse hovers over the button.
 */
public class TooltipDecorator extends ButtonDecorator {
    private String tooltip;
    private boolean showTooltip = false;
    private Color tooltipBgColor = new Color(255, 255, 225); 
    private Color tooltipTextColor = Color.BLACK;
    private int tooltipPadding = 5;
    private Font tooltipFont = new Font("SansSerif", Font.PLAIN, 12);

    public TooltipDecorator(IButton decoratedButton, String tooltip) {
        super(decoratedButton);
        this.tooltip = tooltip;
    }

    @Override
    public void draw(Graphics g) {
        
        super.draw(g);

        
        if (showTooltip && tooltip != null && !tooltip.isEmpty()) {
            
            Font originalFont = g.getFont();
            Color originalColor = g.getColor();

            
            g.setFont(tooltipFont);
            int textWidth = g.getFontMetrics().stringWidth(tooltip);
            int textHeight = g.getFontMetrics().getHeight();

            
            int tooltipX = decoratedButton.getX() + (decoratedButton.getWidth() - textWidth) / 2;
            int tooltipY = decoratedButton.getY() - textHeight - tooltipPadding * 2;

            
            if (tooltipY < 0) {
                
                tooltipY = decoratedButton.getY() + decoratedButton.getHeight() + tooltipPadding;
            }

            
            g.setColor(tooltipBgColor);
            g.fillRect(tooltipX - tooltipPadding,
                    tooltipY - textHeight,
                    textWidth + tooltipPadding * 2,
                    textHeight + tooltipPadding);

            
            g.setColor(Color.DARK_GRAY);
            g.drawRect(tooltipX - tooltipPadding,
                    tooltipY - textHeight,
                    textWidth + tooltipPadding * 2,
                    textHeight + tooltipPadding);

            
            g.setColor(tooltipTextColor);
            g.drawString(tooltip, tooltipX, tooltipY - tooltipPadding);

            
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
