package com.editor.gui.panel;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics; // Ensure Graphics is imported
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import com.editor.gui.WhiteBoard;
import com.editor.gui.button.Draggable;
import com.editor.gui.button.IButton;
import com.editor.mediator.DragMediator;

public class CustomPanel extends Canvas {
    private int relX, relY, relWidth, relHeight;

    protected List<IButton> buttons = new ArrayList<>(); // Changed to protected for subclasses if needed

    // Variables pour le glisser-déposer
    private Draggable currentDraggedButton = null;
    private WhiteBoard targetWhiteBoard = null; // Keep for potential legacy use or direct reference if needed
    private boolean isDragging = false;
    private Point dragOffset = null;

    // Mediator for drag operations
    protected DragMediator dragMediator = null; // Changed to protected

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

                        // Use mediator if available
                        if (dragMediator != null) {
                            // Pass 'this' (the CustomPanel) as the source component
                            dragMediator.startDrag(CustomPanel.this, currentDraggedButton, e.getX(), e.getY());
                        } else {
                            // Fallback or legacy approach (should ideally not be needed if mediator is
                            // always set)
                            System.err.println("[CustomPanel] Warning: DragMediator not set. Using legacy drag start.");
                            currentDraggedButton.startDrag(e.getX(), e.getY());
                        }

                        // Repaint might be needed to show initial drag feedback on the button/panel
                        repaint();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging && currentDraggedButton != null) {
                    // Mediator handles the end drag logic, including coordinate conversion and
                    // calling endDrag on the button
                    if (dragMediator != null) {
                        // Pass the coordinates relative to this panel
                        dragMediator.endDrag(e.getX(), e.getY());
                    } else {
                        // Fallback or legacy approach
                        System.err.println("[CustomPanel] Warning: DragMediator not set. Using legacy drag end.");
                        currentDraggedButton.endDrag(-1, -1); // Cancel if no mediator
                    }

                    // Reset local drag state (mediator handles its own state)
                    isDragging = false;
                    currentDraggedButton = null;
                    dragOffset = null;
                    // Repaint might be needed to clear any drag feedback on the panel
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
                    // Mediator handles the drag logic
                    if (dragMediator != null) {
                        // Pass coordinates relative to this panel
                        dragMediator.drag(e.getX(), e.getY());
                    } else {
                        // Fallback or legacy approach
                        System.err.println("[CustomPanel] Warning: DragMediator not set. Using legacy drag.");
                        currentDraggedButton.drag(e.getX(), e.getY()); // Direct update as fallback
                    }
                    // Repainting is handled by the mediator calling repaint on the source component
                    // repaint(); // Repaint might still be needed here depending on visual feedback
                    // desired on the panel itself
                }
            }
        });
    }

    /**
     * Adds a button to this panel.
     * 
     * @param button The button to add.
     */
    public void addButton(IButton button) {
        buttons.add(button);
        repaint(); // Repaint after adding a button
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); // Let the superclass paint first (e.g., background)
        // Draw all buttons
        for (IButton button : buttons) {
            button.draw(g);
        }
        // Note: Drag preview drawing is handled by the Draggable button itself
    }

    /**
     * Définit le whiteboard cible pour le glisser-déposer (Potentially legacy or
     * for direct reference)
     *
     * @param whiteBoard Le whiteboard cible
     */
    public void setTargetWhiteBoard(WhiteBoard whiteBoard) {
        this.targetWhiteBoard = whiteBoard;

        // Register with mediator if available
        if (dragMediator != null) {
            dragMediator.registerWhiteBoard(whiteBoard);
        }
    }

    /**
     * Sets the drag mediator for this panel
     *
     * @param mediator The mediator to use for drag operations
     */
    public void setDragMediator(DragMediator mediator) {
        this.dragMediator = mediator;

        // Register this panel with the mediator
        if (mediator != null) {
            mediator.registerPanel(this);

            // Register the whiteboard if it's already set
            if (targetWhiteBoard != null) {
                mediator.registerWhiteBoard(targetWhiteBoard);
            }
        }
    }

    // Legacy convertToWhiteboardCoordinates method removed - handled by mediator

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
        // Provide a default preferred size, adjust as needed
        return new Dimension(100, 50); // Example size
    }
}
