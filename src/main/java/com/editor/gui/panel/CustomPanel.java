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
import com.editor.mediator.DragMediator;

public class CustomPanel extends Canvas {
    private int relX, relY, relWidth, relHeight;

    protected List<IButton> buttons = new ArrayList<>(); 

    
    private Draggable currentDraggedButton = null;
    private WhiteBoard targetWhiteBoard = null; 
    private boolean isDragging = false;
    private Point dragOffset = null;

    
    protected DragMediator dragMediator = null; 

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
                
                for (IButton button : buttons) {
                    if (button.isMouseOver(e.getX(), e.getY()) && button instanceof Draggable) {
                        currentDraggedButton = (Draggable) button;
                        isDragging = true;

                        
                        dragOffset = new Point(e.getX() - button.getX(), e.getY() - button.getY());

                        
                        if (dragMediator != null) {
                            
                            dragMediator.startDrag(CustomPanel.this, currentDraggedButton, e.getX(), e.getY());
                        } else {
                            
                            
                            System.err.println("[CustomPanel] Warning: DragMediator not set. Using legacy drag start.");
                            currentDraggedButton.startDrag(e.getX(), e.getY());
                        }

                        
                        repaint();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging && currentDraggedButton != null) {
                    
                    
                    if (dragMediator != null) {
                        
                        dragMediator.endDrag(e.getX(), e.getY());
                    } else {
                        
                        System.err.println("[CustomPanel] Warning: DragMediator not set. Using legacy drag end.");
                        currentDraggedButton.endDrag(-1, -1); 
                    }

                    
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

                    
                    if (isOver) {
                        
                        if (!button.isCurrentlyHovered()) {
                            button.onMouseOver();
                            stateChanged = true;
                        }
                    } else {
                        
                        if (button.isCurrentlyHovered()) {
                            button.onMouseOut();
                            stateChanged = true;
                        }
                    }
                }

                
                if (stateChanged) {
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && currentDraggedButton != null) {
                    
                    if (dragMediator != null) {
                        
                        dragMediator.drag(e.getX(), e.getY());
                    } else {
                        
                        System.err.println("[CustomPanel] Warning: DragMediator not set. Using legacy drag.");
                        currentDraggedButton.drag(e.getX(), e.getY()); 
                    }
                    
                    
                    
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
        repaint(); 
    }

    /**
     * Removes a specific button from this panel.
     * 
     * @param button The button to remove.
     * @return true if the button was found and removed, false otherwise.
     */
    public boolean removeButton(IButton button) {
        boolean removed = buttons.remove(button);
        if (removed) {
            System.out.println("[CustomPanel] Removed button: " + button.getClass().getSimpleName());
            
            
            repaint(); 
        }
        return removed;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); 
        
        for (IButton button : buttons) {
            button.draw(g);
        }
        
    }

    /**
     * Définit le whiteboard cible pour le glisser-déposer (Potentially legacy or
     * for direct reference)
     *
     * @param whiteBoard Le whiteboard cible
     */
    public void setTargetWhiteBoard(WhiteBoard whiteBoard) {
        this.targetWhiteBoard = whiteBoard;

        
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

        
        if (mediator != null) {
            mediator.registerPanel(this);

            
            if (targetWhiteBoard != null) {
                mediator.registerWhiteBoard(targetWhiteBoard);
            }
        }
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
        applyResponsiveBounds(frame.getSize()); 
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                applyResponsiveBounds(frame.getSize());
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        
        return new Dimension(100, 50); 
    }
}
