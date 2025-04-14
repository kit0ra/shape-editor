package com.editor.gui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import com.editor.gui.WhiteBoard;
import com.editor.mediator.DragMediator;
import com.editor.utils.ImageLoader;

/**
 * A panel that acts as a trash bin for shapes.
 * When shapes are dragged onto this panel and released, they are deleted.
 */
public class TrashPanel extends CustomPanel {
    private final Image trashIcon;
    private boolean isShapeOverTrash = false;
    private WhiteBoard targetWhiteBoard;

    
    private final Color normalColor = java.awt.Color.decode("#F6E9D7");
    private final Color hoverColor = new Color(255, 200, 200); 

    public TrashPanel() {
        super();
        trashIcon = ImageLoader.loadImage("icons/trash.png");
        setBackground(normalColor);
    }

    @Override
    public void paint(Graphics g) {
        
        setBackground(isShapeOverTrash ? hoverColor : normalColor);

        super.paint(g);

        
        if (trashIcon != null) {
            int iconWidth = 32;
            int iconHeight = 32;
            int x = (getWidth() - iconWidth) / 2;
            int y = (getHeight() - iconHeight) / 2;
            g.drawImage(trashIcon, x, y, iconWidth, iconHeight, this);
        }
    }

    /**
     * Sets the whiteboard that this trash panel will delete shapes from
     *
     * @param whiteBoard The target whiteboard
     */
    @Override
    public void setTargetWhiteBoard(WhiteBoard whiteBoard) {
        this.targetWhiteBoard = whiteBoard;
    }

    /**
     * Sets the drag mediator for this panel
     *
     * @param mediator The mediator to use
     */
    @Override
    public void setDragMediator(DragMediator mediator) {
        this.dragMediator = mediator;

        
        if (mediator != null) {
            mediator.registerTrashPanel(this);
        }
    }

    /**
     * Called by the mediator when a shape is being dragged over this panel
     *
     * @param isOver Whether a shape is currently over the trash panel
     */
    public void setShapeOverTrash(boolean isOver) {
        if (this.isShapeOverTrash != isOver) {
            this.isShapeOverTrash = isOver;
            repaint();
        }
    }

    /**
     * Deletes the selected shapes from the whiteboard
     *
     * @return true if shapes were deleted, false otherwise
     */
    public boolean deleteSelectedShapes() {
        if (targetWhiteBoard != null) {
            return targetWhiteBoard.deleteSelectedShapes();
        }
        return false;
    }

    /**
     * Checks if a point in screen coordinates is over this trash panel
     *
     * @param screenPoint The point in screen coordinates
     * @return true if the point is over this panel, false otherwise
     */
    public boolean isPointOverTrash(Point screenPoint) {
        try {
            Point panelLocation = getLocationOnScreen();
            return screenPoint.x >= panelLocation.x &&
                    screenPoint.x < panelLocation.x + getWidth() &&
                    screenPoint.y >= panelLocation.y &&
                    screenPoint.y < panelLocation.y + getHeight();
        } catch (Exception e) {
            return false;
        }
    }
}
