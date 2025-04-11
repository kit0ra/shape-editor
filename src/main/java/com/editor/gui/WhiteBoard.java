package com.editor.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.BasicStroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.editor.commands.CommandHistory;
import com.editor.commands.CreateShapeCommand;
import com.editor.commands.MoveShapeCommand;
import com.editor.drawing.AWTDrawing;
import com.editor.drawing.Drawer;
import com.editor.shapes.Rectangle;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapePrototypeRegistry;

public class WhiteBoard extends Canvas {
    private double relX, relY, relW, relH;
    private Color backgroundColor = Color.WHITE;
    private List<Shape> shapes = new ArrayList<>();
    private List<Shape> selectedShapes = new ArrayList<>(); // Liste des formes sélectionnées
    private CommandHistory commandHistory = new CommandHistory();
    private ShapePrototypeRegistry prototypeRegistry = null;
    private String currentShapeType = null;
    private Shape activeShape; // La forme actuellement active pour le déplacement
    private Point dragStartPoint; // Where the mouse was initially pressed
    private Point originalShapePosition; // Top-left corner of the shape when drag started
    private Point dragOffset; // Difference between dragStartPoint and originalShapePosition
    private boolean isDragging = false;
    private boolean isCtrlPressed = false; // Pour la sélection multiple

    // Variables pour le rectangle de sélection
    private Point selectionStart; // Point de départ du rectangle de sélection
    private Point selectionEnd; // Point actuel pendant le glissement
    private boolean isSelectionRectActive = false; // Indique si on dessine un rectangle de sélection

    // Double buffering
    private Image offscreenBuffer;
    private Graphics offscreenGraphics;

    public WhiteBoard(int width, int height, Color white) {
        this.setPreferredSize(new Dimension(width, height));
        this.backgroundColor = white;
        this.setBackground(white);

        setupMouseListeners();
        setupKeyListeners();

        // Pour permettre au composant de recevoir les événements clavier
        setFocusable(true);
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow(); // Pour s'assurer que le composant reçoit les événements clavier
                handleMousePress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseRelease(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        });
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isCtrlPressed = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isCtrlPressed = false;
                }
            }
        });
    }

    private void handleMousePress(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            // Vérifier si Ctrl est enfoncé pour la sélection multiple
            isCtrlPressed = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;

            // Reset drag state variables
            isDragging = false;
            originalShapePosition = null;
            dragStartPoint = null;
            dragOffset = null;

            // Réinitialiser les variables du rectangle de sélection
            isSelectionRectActive = false;
            selectionStart = null;
            selectionEnd = null;

            // Try to select a shape under the mouse cursor
            boolean foundShape = false;
            for (Shape shape : shapes) {
                if (shape.isSelected(e.getX(), e.getY())) {
                    // Found a shape under the cursor
                    foundShape = true;

                    // Si la forme est déjà sélectionnée et que Ctrl est enfoncé, on la désélectionne
                    if (selectedShapes.contains(shape) && isCtrlPressed) {
                        selectedShapes.remove(shape);
                        shape.setSelected(false);
                    } else {
                        // Si Ctrl n'est pas enfoncé, on désélectionne toutes les autres formes
                        if (!isCtrlPressed) {
                            for (Shape s : selectedShapes) {
                                s.setSelected(false);
                            }
                            selectedShapes.clear();
                        }

                        // Sélectionner la forme actuelle
                        if (!selectedShapes.contains(shape)) {
                            selectedShapes.add(shape);
                            shape.setSelected(true);
                        }

                        // Définir la forme active pour le déplacement
                        activeShape = shape;

                        // Store the starting point for drag operation
                        dragStartPoint = e.getPoint();

                        // Store the original shape position for undo/redo and offset calculation
                        Rectangle bounds = activeShape.getBounds();
                        originalShapePosition = new Point(bounds.getX(), bounds.getY());

                        // Calculate the offset between the click point and the shape's origin
                        dragOffset = new Point(
                                dragStartPoint.x - originalShapePosition.x,
                                dragStartPoint.y - originalShapePosition.y);

                        // Set dragging state
                        isDragging = true;
                    }
                    break;
                }
            }

            // Si aucune forme n'a été trouvée sous le curseur
            if (!foundShape) {
                // Si Ctrl n'est pas enfoncé, désélectionner toutes les formes
                if (!isCtrlPressed) {
                    for (Shape s : selectedShapes) {
                        s.setSelected(false);
                    }
                    selectedShapes.clear();
                    activeShape = null;
                }

                // Initialiser le rectangle de sélection
                selectionStart = e.getPoint();
                selectionEnd = e.getPoint(); // Au début, le point de fin est le même que le point de départ
                isSelectionRectActive = true;
            }

            // Create new shape if none selected and a shape type is selected
            if (selectedShapes.isEmpty() && currentShapeType != null && prototypeRegistry != null) {
                createShapeAt(e.getX(), e.getY());
            }

            repaint();
        }
    }

    private void handleMouseDrag(MouseEvent e) {
        // Si on est en train de dessiner un rectangle de sélection
        if (isSelectionRectActive && selectionStart != null) {
            // Mettre à jour le point de fin du rectangle de sélection
            selectionEnd = e.getPoint();
            repaint();
            return;
        }

        // Ensure dragging state, a shape is selected, and we have the offset
        if (isDragging && activeShape != null && dragOffset != null) {
            // Calculate the new top-left position based on mouse position and initial
            // offset
            int newX = e.getX() - dragOffset.x;
            int newY = e.getY() - dragOffset.y;

            // Calculer le déplacement par rapport à la position d'origine
            int deltaX = newX - originalShapePosition.x;
            int deltaY = newY - originalShapePosition.y;

            // Si une seule forme est sélectionnée, déplacer uniquement cette forme
            if (selectedShapes.size() == 1) {
                activeShape.setPosition(newX, newY);
            }
            // Si plusieurs formes sont sélectionnées, les déplacer toutes ensemble
            else if (selectedShapes.size() > 1) {
                // Déplacer la forme active à la nouvelle position
                activeShape.setPosition(newX, newY);

                // Déplacer toutes les autres formes sélectionnées du même delta
                for (Shape shape : selectedShapes) {
                    if (shape != activeShape) {
                        Rectangle bounds = shape.getBounds();
                        shape.setPosition(bounds.getX() + deltaX, bounds.getY() + deltaY);
                    }
                }
            }

            // Request a repaint to show the shapes in their new positions
            repaint();
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        // Si on a dessiné un rectangle de sélection
        if (isSelectionRectActive && selectionStart != null && selectionEnd != null) {
            // Calculer les coordonnées du rectangle de sélection
            int x1 = Math.min(selectionStart.x, selectionEnd.x);
            int y1 = Math.min(selectionStart.y, selectionEnd.y);
            int x2 = Math.max(selectionStart.x, selectionEnd.x);
            int y2 = Math.max(selectionStart.y, selectionEnd.y);

            // Si le rectangle est trop petit, c'est probablement un clic accidentel
            if (x2 - x1 < 5 || y2 - y1 < 5) {
                isSelectionRectActive = false;
                selectionStart = null;
                selectionEnd = null;
                repaint();
                return;
            }

            // Si Ctrl n'est pas enfoncé, désélectionner toutes les formes d'abord
            if (!isCtrlPressed) {
                for (Shape s : selectedShapes) {
                    s.setSelected(false);
                }
                selectedShapes.clear();
            }

            // Sélectionner toutes les formes qui se trouvent dans le rectangle
            for (Shape shape : shapes) {
                Rectangle bounds = shape.getBounds();

                // Vérifier si la forme est dans le rectangle de sélection
                if (bounds.getX() >= x1 && bounds.getX() + bounds.getWidth() <= x2 &&
                    bounds.getY() >= y1 && bounds.getY() + bounds.getHeight() <= y2) {

                    // Ajouter la forme à la sélection si elle n'y est pas déjà
                    if (!selectedShapes.contains(shape)) {
                        selectedShapes.add(shape);
                        shape.setSelected(true);
                    }
                }
            }

            // Réinitialiser les variables du rectangle de sélection
            isSelectionRectActive = false;
            selectionStart = null;
            selectionEnd = null;

            repaint();
            return;
        }

        // Check if a drag operation was in progress and completed
        if (isDragging && activeShape != null && originalShapePosition != null) {
            // Get the final position of the active shape after dragging
            Rectangle bounds = activeShape.getBounds();
            Point finalPosition = new Point(bounds.getX(), bounds.getY());

            // Only create and execute a command if the position actually changed
            if (!originalShapePosition.equals(finalPosition)) {
                // Pour l'instant, on ne gère que le déplacement de la forme active
                // TODO: Implémenter une commande pour déplacer plusieurs formes
                MoveShapeCommand moveCommand = new MoveShapeCommand(
                        activeShape,
                        originalShapePosition,
                        finalPosition);

                commandHistory.executeCommand(moveCommand);
            }

            // Reset drag state variables regardless of whether a move occurred
            isDragging = false;
            dragStartPoint = null;
            originalShapePosition = null;
            dragOffset = null;

            // Repaint to potentially remove drag-specific highlighting
            repaint();
        }
    }

    @Override
    public void update(Graphics g) {
        // Override update to prevent clearing the screen before painting
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        // Create offscreen buffer if it doesn't exist or if size has changed
        if (offscreenBuffer == null ||
                offscreenBuffer.getWidth(null) != getWidth() ||
                offscreenBuffer.getHeight(null) != getHeight()) {
            createOffscreenBuffer();
        }

        // Clear the offscreen buffer
        offscreenGraphics.setColor(backgroundColor);
        offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());

        // Draw all shapes to the offscreen buffer
        Drawer drawer = new AWTDrawing((Graphics2D) offscreenGraphics);
        for (Shape shape : shapes) {
            shape.draw(drawer);
        }

        // Highlight selected shapes with dashed border
        if (!selectedShapes.isEmpty()) {
            Graphics2D g2d = (Graphics2D) offscreenGraphics.create();
            try {
                // Définir le style de trait en pointillés
                float[] dash = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

                // Dessiner un contour en pointillés autour de chaque forme sélectionnée
                for (Shape shape : selectedShapes) {
                    Rectangle bounds = shape.getBounds();

                    if (isDragging && shape == activeShape) {
                        // Style spécifique pour la forme en cours de déplacement
                        g2d.setColor(new Color(255, 165, 0)); // Orange
                    } else {
                        // Style normal pour les formes sélectionnées
                        g2d.setColor(new Color(0, 0, 255)); // Bleu
                    }

                    // Dessiner le contour en pointillés légèrement plus grand que la forme
                    g2d.drawRect(bounds.getX() - 2, bounds.getY() - 2,
                            bounds.getWidth() + 4, bounds.getHeight() + 4);

                    // Dessiner un point rouge au centre de la forme (comme dans les images)
                    g2d.setColor(Color.RED);
                    g2d.fillOval(bounds.getX() + bounds.getWidth()/2 - 3,
                                bounds.getY() + bounds.getHeight()/2 - 3, 6, 6);
                }
            } finally {
                g2d.dispose();
            }
        }

        // Dessiner le rectangle de sélection s'il est actif
        if (isSelectionRectActive && selectionStart != null && selectionEnd != null) {
            Graphics2D g2d = (Graphics2D) offscreenGraphics.create();
            try {
                // Calculer les coordonnées du rectangle
                int x = Math.min(selectionStart.x, selectionEnd.x);
                int y = Math.min(selectionStart.y, selectionEnd.y);
                int width = Math.abs(selectionEnd.x - selectionStart.x);
                int height = Math.abs(selectionEnd.y - selectionStart.y);

                // Définir le style de trait en pointillés
                float[] dash = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

                // Dessiner le rectangle de sélection avec un fond semi-transparent
                g2d.setColor(new Color(0, 0, 255, 30)); // Bleu semi-transparent
                g2d.fillRect(x, y, width, height);

                // Dessiner le contour du rectangle
                g2d.setColor(new Color(0, 0, 255)); // Bleu
                g2d.drawRect(x, y, width, height);
            } finally {
                g2d.dispose();
            }
        }

        // Draw the offscreen buffer to the screen
        g.drawImage(offscreenBuffer, 0, 0, this);
    }

    /**
     * Creates or recreates the offscreen buffer with the current component
     * dimensions
     */
    private void createOffscreenBuffer() {
        offscreenBuffer = createImage(getWidth(), getHeight());
        if (offscreenBuffer != null) {
            if (offscreenGraphics != null) {
                offscreenGraphics.dispose();
            }
            offscreenGraphics = offscreenBuffer.getGraphics();
        }
    }

    /**
     * Sets the current shape type to use for creating shapes
     *
     * @param shapeType The type of shape to create (key in the prototype registry)
     */
    public void setCurrentShapeType(String shapeType) {
        this.currentShapeType = shapeType;
    }

    /**
     * Sets the prototype registry to use for creating shapes
     *
     * @param registry The shape prototype registry
     */
    public void setPrototypeRegistry(ShapePrototypeRegistry registry) {
        this.prototypeRegistry = registry;
    }

    /**
     * Creates a shape at the specified location using the current shape type
     */
    public void createShapeAt(int x, int y) {
        if (currentShapeType != null && prototypeRegistry != null) {
            Shape newShape = prototypeRegistry.createShape(currentShapeType, x, y);
            commandHistory.executeCommand(
                    new CreateShapeCommand(shapes, newShape, x, y));

            // Désélectionner toutes les formes précédemment sélectionnées
            for (Shape s : selectedShapes) {
                s.setSelected(false);
            }
            selectedShapes.clear();

            // Sélectionner la nouvelle forme
            selectedShapes.add(newShape);
            activeShape = newShape;
            newShape.setSelected(true);
            repaint();
        }
    }

    /**
     * Adds a shape to the center of the whiteboard using the current shape type
     */
    public void addShapeToCenter() {
        if (currentShapeType != null && prototypeRegistry != null) {
            // Calculate the center of the whiteboard
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            createShapeAt(centerX, centerY);
        }
    }

    /**
     * Adds a shape to the top-left corner of the whiteboard using the current shape
     * type
     */
    public void addShapeToTopLeft() {
        if (currentShapeType != null && prototypeRegistry != null) {
            // Use a small margin from the top-left corner
            int marginX = 20;
            int marginY = 20;

            createShapeAt(marginX, marginY);
        }
    }

    public void undo() {
        commandHistory.undo();
        repaint();
    }

    public void redo() {
        commandHistory.redo();
        repaint();
    }

    public void setRelativeBounds(double xPercent, double yPercent, double widthPercent, double heightPercent) {
        this.relX = xPercent / 100.0;
        this.relY = yPercent / 100.0;
        this.relW = widthPercent / 100.0;
        this.relH = heightPercent / 100.0;
    }

    public void makeResponsiveTo(Frame frame) {
        ComponentAdapter resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBounds(frame);
            }
        };

        frame.addComponentListener(resizeListener);

        // Initial positioning
        updateBounds(frame);
    }

    private void updateBounds(Frame frame) {
        int w = frame.getWidth();
        int h = frame.getHeight();

        int x = (int) (w * relX);
        int y = (int) (h * relY);
        int width = (int) (w * relW);
        int height = (int) (h * relH);

        setBounds(x, y, width, height);

        // Recreate the offscreen buffer when size changes
        createOffscreenBuffer();
        repaint();
    }

    /**
     * Cleans up resources used by this component
     */
    public void dispose() {
        if (offscreenGraphics != null) {
            offscreenGraphics.dispose();
            offscreenGraphics = null;
        }
        offscreenBuffer = null;
    }
}
