
package com.editor.gui.panel;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.button.IButton;

public class CustomPanel extends Canvas {
    private int relX, relY, relWidth, relHeight;

    private List<IButton> buttons = new ArrayList<>();

    // Variables pour le glisser-déposer
    private Draggable currentDraggedButton = null;
    private WhiteBoard targetWhiteBoard = null;
    private boolean isDragging = false;
    private Point dragOffset = null;

    public CustomPanel() {
        setBackground(java.awt.Color.decode("#F6E9D7"));
        setupMouseListeners();
    }

    /**
     * Configure les écouteurs d'événements de souris pour le panneau
     */
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Ne traiter les clics que si nous ne sommes pas en train de glisser
                if (!isDragging) {
                    for (IButton button : buttons) {
                        if (button.isMouseOver(e.getX(), e.getY())) {
                            button.onClick();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Vérifier si un bouton draggable est sous la souris
                for (IButton button : buttons) {
                    if (button.isMouseOver(e.getX(), e.getY()) && button instanceof Draggable) {
                        currentDraggedButton = (Draggable) button;
                        isDragging = true;

                        // Calculer l'offset pour que le glisser soit relatif au point de clic
                        dragOffset = new Point(e.getX() - button.getX(), e.getY() - button.getY());

                        // Notifier le bouton que le glisser commence
                        currentDraggedButton.startDrag(e.getX(), e.getY());
                        repaint();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging && currentDraggedButton != null) {
                    // Convertir les coordonnées du panneau en coordonnées du whiteboard
                    if (targetWhiteBoard != null) {
                        Point whiteboardPoint = convertToWhiteboardCoordinates(e.getPoint());
                        if (whiteboardPoint != null) {
                            // Terminer le glisser sur le whiteboard
                            currentDraggedButton.endDrag(whiteboardPoint.x, whiteboardPoint.y);
                        } else {
                            // Annuler le glisser si on n'est pas sur le whiteboard
                            currentDraggedButton.endDrag(-1, -1);
                        }
                    } else {
                        // Annuler le glisser si pas de whiteboard cible
                        currentDraggedButton.endDrag(-1, -1);
                    }

                    // Réinitialiser l'état du glisser
                    isDragging = false;
                    currentDraggedButton = null;
                    dragOffset = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean stateChanged = false;
                int mouseX = e.getX();
                int mouseY = e.getY();

                for (IButton button : buttons) {
                    boolean isOver = button.isMouseOver(mouseX, mouseY);

                    // Check if this button is currently being hovered over
                    if (isOver) {
                        // Only call onMouseOver if the state is changing
                        if (!button.isCurrentlyHovered()) {
                            button.onMouseOver();
                            stateChanged = true;
                        }
                    } else {
                        // Only call onMouseOut if the state is changing
                        if (button.isCurrentlyHovered()) {
                            button.onMouseOut();
                            stateChanged = true;
                        }
                    }
                }

                // Only repaint if a button's state has changed
                if (stateChanged) {
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && currentDraggedButton != null) {
                    // Mettre à jour la position du glisser
                    currentDraggedButton.drag(e.getX(), e.getY());
                    repaint();
                }
            }
        });
    }

    public void addButton(IButton button) {
        buttons.add(button);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (IButton button : buttons) {
            button.draw(g);
        }
    }

    /**
     * Définit le whiteboard cible pour le glisser-déposer
     *
     * @param whiteBoard Le whiteboard cible
     */
    public void setTargetWhiteBoard(WhiteBoard whiteBoard) {
        this.targetWhiteBoard = whiteBoard;
    }

    /**
     * Convertit les coordonnées du panneau en coordonnées du whiteboard
     *
     * @param panelPoint Point dans les coordonnées du panneau
     * @return Point dans les coordonnées du whiteboard, ou null si hors du whiteboard
     */
    private Point convertToWhiteboardCoordinates(Point panelPoint) {
        if (targetWhiteBoard == null) {
            return null;
        }

        // Convertir les coordonnées du panneau en coordonnées de l'écran
        Point screenPoint = new Point(panelPoint);
        screenPoint.translate(getLocationOnScreen().x, getLocationOnScreen().y);

        // Vérifier si le point est dans les limites du whiteboard
        Point whiteboardLocation = targetWhiteBoard.getLocationOnScreen();
        if (screenPoint.x >= whiteboardLocation.x &&
            screenPoint.x < whiteboardLocation.x + targetWhiteBoard.getWidth() &&
            screenPoint.y >= whiteboardLocation.y &&
            screenPoint.y < whiteboardLocation.y + targetWhiteBoard.getHeight()) {

            // Convertir en coordonnées relatives au whiteboard
            return new Point(
                screenPoint.x - whiteboardLocation.x,
                screenPoint.y - whiteboardLocation.y
            );
        }

        return null;
    }

    public void setRelativeBounds(int xPercent, int yPercent, int widthPercent, int heightPercent) {
        this.relX = xPercent;
        this.relY = yPercent;
        this.relWidth = widthPercent;
        this.relHeight = heightPercent;
    }

    public void applyResponsiveBounds(Dimension parentSize) {
        int x = parentSize.width * relX / 100;
        int y = parentSize.height * relY / 100;
        int w = parentSize.width * relWidth / 100;
        int h = parentSize.height * relHeight / 100;
        setBounds(x, y, w, h);
    }

    public void makeResponsiveTo(Frame frame) {
        applyResponsiveBounds(frame.getSize()); // initial
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                applyResponsiveBounds(frame.getSize());
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

}
