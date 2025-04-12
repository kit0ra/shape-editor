package com.editor.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter; // Already exists, ensure it's used
import java.awt.event.ComponentEvent; // Already exists, ensure it's used
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import com.editor.commands.CommandHistory;
import com.editor.commands.CreateShapeCommand;
import com.editor.commands.EditShapeCommand;
import com.editor.commands.GroupShapesCommand;
import com.editor.commands.MoveShapeCommand;
import com.editor.commands.MoveShapesCommand;
import com.editor.commands.UngroupShapesCommand;
import com.editor.drawing.AWTDrawing;
import com.editor.drawing.Drawer;
import com.editor.memento.ShapeMemento; // Added Memento import
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.gui.button.Draggable;
import com.editor.gui.panel.ToolbarPanel; // Added import
import com.editor.mediator.DragMediator;
import com.editor.state.StateChangeListener;

public class WhiteBoard extends Canvas implements Draggable {
    private double relX, relY, relW, relH;
    private Color backgroundColor = Color.WHITE;
    private List<Shape> shapes = new ArrayList<>();
    private List<Shape> selectedShapes = new ArrayList<>(); // Liste des formes sélectionnées
    private CommandHistory commandHistory = new CommandHistory();
    private ShapePrototypeRegistry prototypeRegistry = null;
    private String currentShapeType = null;
    private Shape activeShape; // La forme actuellement active pour le déplacement

    // State change listener for auto-save functionality
    private StateChangeListener stateChangeListener;
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

    // Reference to ToolbarPanel for adding shapes
    private ToolbarPanel toolbarPanel;

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

                // Gérer le clic droit pour le menu contextuel
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClick(e);
                } else {
                    handleMousePress(e);
                }
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

        // Add a component listener to update the offscreen buffer on resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                createOffscreenBuffer();
            }
        });
    }

    /**
     * Gère le clic droit pour afficher le menu contextuel
     */
    private void handleRightClick(MouseEvent e) {
        // Vérifier si on a cliqué sur une forme sélectionnée
        boolean clickedOnSelected = false;
        for (Shape shape : selectedShapes) {
            if (shape.isSelected(e.getX(), e.getY())) {
                clickedOnSelected = true;
                break;
            }
        }

        // Si on a cliqué sur une forme sélectionnée, afficher le menu contextuel
        if (clickedOnSelected) {
            showContextMenu(e.getX(), e.getY());
        }
    }

    /**
     * Affiche le menu contextuel aux coordonnées spécifiées
     */
    private void showContextMenu(int x, int y) {
        PopupMenu contextMenu = new PopupMenu();

        // Option Edit
        MenuItem editItem = new MenuItem("Edit");
        editItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedShape();
            }
        });
        contextMenu.add(editItem);

        // Option Group
        MenuItem groupItem = new MenuItem("Group");
        groupItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                groupSelectedShapes();
            }
        });
        contextMenu.add(groupItem);

        // Option Ungroup (activée uniquement si une forme sélectionnée est un groupe)
        MenuItem ungroupItem = new MenuItem("Ungroup");
        ungroupItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ungroupSelectedShapes();
            }
        });

        // Vérifier si au moins une forme sélectionnée est un groupe
        boolean hasGroup = false;
        for (Shape shape : selectedShapes) {
            if (shape instanceof ShapeGroup) {
                hasGroup = true;
                break;
            }
        }
        ungroupItem.setEnabled(hasGroup);
        contextMenu.add(ungroupItem);

        // Afficher le menu contextuel
        this.add(contextMenu);
        contextMenu.show(this, x, y);
    }

    /**
     * Edite la forme sélectionnée
     */
    private void editSelectedShape() {
        if (selectedShapes.isEmpty()) {
            return; // Aucune forme sélectionnée
        }

        // Get the current properties from the first selected shape
        Shape firstShape = selectedShapes.get(0);
        Color currentBorderColor = getBorderColor(firstShape);
        Color currentFillColor = getFillColor(firstShape);
        double currentRotation = getRotation(firstShape);

        // Get border radius if it's a rectangle
        int currentBorderRadius = 0;
        boolean isRectangle = firstShape instanceof Rectangle;
        if (isRectangle) {
            currentBorderRadius = ((Rectangle) firstShape).getBorderRadius();
        }

        // Create a Swing dialog for editing properties
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        final JDialog editDialog = new JDialog(parentFrame, "Edit Shape", true);
        editDialog.setLayout(new BorderLayout());

        // Create a panel for the form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Border Color
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Border Color:"), gbc);

        final JButton borderColorButton = new JButton();
        borderColorButton.setBackground(currentBorderColor);
        borderColorButton.setPreferredSize(new Dimension(100, 25));
        final Color[] selectedBorderColor = { currentBorderColor };

        borderColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(editDialog, "Choose Border Color", selectedBorderColor[0]);
                if (newColor != null) {
                    selectedBorderColor[0] = newColor;
                    borderColorButton.setBackground(newColor);
                }
            }
        });

        gbc.gridx = 1;
        formPanel.add(borderColorButton, gbc);

        // Fill Color
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Fill Color:"), gbc);

        final JButton fillColorButton = new JButton();
        fillColorButton.setBackground(currentFillColor);
        fillColorButton.setPreferredSize(new Dimension(100, 25));
        final Color[] selectedFillColor = { currentFillColor };

        fillColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(editDialog, "Choose Fill Color", selectedFillColor[0]);
                if (newColor != null) {
                    selectedFillColor[0] = newColor;
                    fillColorButton.setBackground(newColor);
                }
            }
        });

        gbc.gridx = 1;
        formPanel.add(fillColorButton, gbc);

        // Rotation
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Rotation (degrees):"), gbc);

        final JTextField rotationField = new JTextField(String.valueOf(currentRotation));
        gbc.gridx = 1;
        formPanel.add(rotationField, gbc);

        // Border Radius (only for rectangles)
        final JSlider borderRadiusSlider;
        final JLabel borderRadiusValueLabel;

        if (isRectangle) {
            // Add a label for the radius control
            gbc.gridx = 0;
            gbc.gridy = 3;
            formPanel.add(new JLabel("Border Radius:"), gbc);

            // Create a simpler slider with better visibility
            borderRadiusSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, currentBorderRadius);

            // Set slider properties for better visibility
            borderRadiusSlider.setMajorTickSpacing(10);
            borderRadiusSlider.setMinorTickSpacing(5);
            borderRadiusSlider.setPaintTicks(true);
            borderRadiusSlider.setPaintLabels(true);
            borderRadiusSlider.setSnapToTicks(false); // Allow smooth sliding

            // Make the slider more visible with a border and background
            borderRadiusSlider.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            borderRadiusSlider.setBackground(new Color(240, 240, 240)); // Light gray background

            // Create a value display
            borderRadiusValueLabel = new JLabel(String.valueOf(currentBorderRadius));
            borderRadiusValueLabel.setHorizontalAlignment(JLabel.RIGHT);
            borderRadiusValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            // Add a change listener with live preview
            borderRadiusSlider.addChangeListener(e -> {
                int value = borderRadiusSlider.getValue();
                borderRadiusValueLabel.setText(String.valueOf(value));

                // Create a preview of the shape with the new border radius
                if (!selectedShapes.isEmpty() && selectedShapes.get(0) instanceof Rectangle) {
                    Rectangle previewRect = (Rectangle) selectedShapes.get(0);
                    previewRect.setBorderRadius(value);
                    repaint(); // Update the display to show the new border radius
                }
            });

            // Create a panel for the slider with the value label
            JPanel sliderPanel = new JPanel(new BorderLayout());
            sliderPanel.add(borderRadiusSlider, BorderLayout.CENTER);
            sliderPanel.add(borderRadiusValueLabel, BorderLayout.EAST);

            // Add the slider panel to the form
            gbc.gridx = 1;
            formPanel.add(sliderPanel, gbc);
        } else {
            // Create dummy objects to avoid null pointer exceptions
            borderRadiusSlider = new JSlider();
            borderRadiusValueLabel = new JLabel();
        }

        // Add the form panel to the dialog
        editDialog.add(formPanel, BorderLayout.CENTER);

        // Create buttons panel
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get the rotation value
                    double rotation = Double.parseDouble(rotationField.getText());

                    // Get the border radius value if it's a rectangle
                    int borderRadius = 0;
                    if (isRectangle) {
                        borderRadius = borderRadiusSlider.getValue();
                    }

                    // Create a command to make the edit undoable
                    EditShapeCommand command = new EditShapeCommand(
                            selectedShapes,
                            selectedBorderColor[0],
                            selectedFillColor[0],
                            rotation,
                            borderRadius);

                    // Execute the command and add it to the command history
                    command.execute();
                    commandHistory.addCommand(command);

                    repaint();

                    // Notify state change listener
                    notifyStateChanged("Shape properties edited");

                    editDialog.dispose();
                } catch (NumberFormatException ex) {
                    // Show an error message for invalid rotation value
                    JOptionPane.showMessageDialog(editDialog,
                            "Invalid rotation value. Please enter a number.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editDialog.dispose();
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog size and show it
        editDialog.setSize(450, isRectangle ? 350 : 250);
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
    }

    /**
     * Adds a color button to a dialog
     */
    private void addColorButton(Dialog dialog, Color color, final Color[] selectedColor) {
        Button colorBtn = new Button();
        colorBtn.setBackground(color);
        colorBtn.setSize(30, 30);
        colorBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor[0] = color;
                dialog.dispose();
            }
        });
        dialog.add(colorBtn);
    }

    /**
     * Returns a contrasting color (black or white) based on the brightness of the
     * input color
     */
    private Color getContrastColor(Color color) {
        // Calculate the perceived brightness using the formula
        // (0.299*R + 0.587*G + 0.114*B)
        double brightness = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return brightness > 0.5 ? Color.BLACK : Color.WHITE;
    }

    /**
     * Gets the border color of a shape
     */
    private Color getBorderColor(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getBorderColor();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getBorderColor();
        } else if (shape instanceof ShapeGroup) {
            return ((ShapeGroup) shape).getBorderColor();
        }
        return Color.BLACK; // Default
    }

    /**
     * Gets the fill color of a shape
     */
    private Color getFillColor(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getFillColor();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getFillColor();
        }
        return Color.WHITE; // Default
    }

    /**
     * Gets the rotation of a shape
     */
    private double getRotation(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getRotation();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getRotation();
        } else if (shape instanceof ShapeGroup) {
            return ((ShapeGroup) shape).getRotation();
        }
        return 0.0; // Default
    }

    /**
     * Convertit un nom de couleur en objet Color
     */
    private Color getColorFromName(String colorName) {
        switch (colorName) {
            case "Red":
                return Color.RED;
            case "Green":
                return Color.GREEN;
            case "Blue":
                return Color.BLUE;
            case "Yellow":
                return Color.YELLOW;
            case "White":
                return Color.WHITE;
            case "Black":
            default:
                return Color.BLACK;
        }
    }

    /**
     * Groupe les formes sélectionnées
     */
    private void groupSelectedShapes() {
        if (selectedShapes.size() < 2) {
            return; // Il faut au moins 2 formes pour créer un groupe
        }

        // Créer et exécuter la commande de groupement
        GroupShapesCommand command = new GroupShapesCommand(shapes, new ArrayList<>(selectedShapes));
        commandHistory.executeCommand(command);

        // Mettre à jour la sélection pour ne contenir que le groupe
        for (Shape shape : selectedShapes) {
            shape.setSelected(false);
        }
        selectedShapes.clear();

        ShapeGroup group = command.getGroup();
        group.setSelected(true);
        selectedShapes.add(group);
        activeShape = group;

        repaint();
    }

    /**
     * Dégroupe les groupes sélectionnés
     */
    private void ungroupSelectedShapes() {
        List<Shape> newSelection = new ArrayList<>();
        List<UngroupShapesCommand> commands = new ArrayList<>();

        // Trouver tous les groupes sélectionnés et les dégrouper
        for (Shape shape : selectedShapes) {
            if (shape instanceof ShapeGroup) {
                ShapeGroup group = (ShapeGroup) shape;
                UngroupShapesCommand command = new UngroupShapesCommand(shapes, group);
                commands.add(command);
                commandHistory.executeCommand(command);

                // Ajouter les formes dégroupées à la nouvelle sélection
                newSelection.addAll(command.getUngroupedShapes());
            } else {
                newSelection.add(shape);
            }
        }

        // Mettre à jour la sélection
        for (Shape shape : selectedShapes) {
            shape.setSelected(false);
        }
        selectedShapes.clear();

        for (Shape shape : newSelection) {
            shape.setSelected(true);
            selectedShapes.add(shape);
        }

        // Réinitialiser la forme active
        activeShape = null;

        repaint();
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

    // Stocke les positions originales de toutes les formes sélectionnées pour le
    // déplacement multiple
    private Map<Shape, Point> originalPositions = new HashMap<>();

    private void handleMousePress(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            // Vérifier si Ctrl est enfoncé pour la sélection multiple
            isCtrlPressed = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;

            // Reset drag state variables
            isDragging = false;
            originalShapePosition = null;
            dragStartPoint = null;
            dragOffset = null;
            originalPositions.clear();

            // Réinitialiser les variables du rectangle de sélection
            isSelectionRectActive = false;
            selectionStart = null;
            selectionEnd = null;

            // Try to select a shape under the mouse cursor
            boolean foundShape = false;
            Shape clickedShape = null;

            for (Shape shape : shapes) {
                if (shape.isSelected(e.getX(), e.getY())) {
                    // Found a shape under the cursor
                    foundShape = true;
                    clickedShape = shape;
                    break;
                }
            }

            if (foundShape) {
                // Si la forme est déjà sélectionnée et que Ctrl est enfoncé, on la
                // désélectionne
                if (selectedShapes.contains(clickedShape) && isCtrlPressed) {
                    selectedShapes.remove(clickedShape);
                    clickedShape.setSelected(false);
                } else {
                    // Si la forme est déjà sélectionnée, on commence simplement le déplacement
                    if (selectedShapes.contains(clickedShape)) {
                        // Définir la forme active pour le déplacement
                        activeShape = clickedShape;
                    } else {
                        // Si Ctrl n'est pas enfoncé, on désélectionne toutes les autres formes
                        if (!isCtrlPressed) {
                            for (Shape s : selectedShapes) {
                                s.setSelected(false);
                            }
                            selectedShapes.clear();
                        }

                        // Sélectionner la forme actuelle
                        selectedShapes.add(clickedShape);
                        clickedShape.setSelected(true);

                        // Définir la forme active pour le déplacement
                        activeShape = clickedShape;
                    }

                    // Store the starting point for drag operation
                    dragStartPoint = e.getPoint();

                    // Store the original shape position for undo/redo and offset calculation
                    Rectangle bounds = activeShape.getBounds();
                    originalShapePosition = new Point(bounds.getX(), bounds.getY());

                    // Calculate the offset between the click point and the shape's origin
                    dragOffset = new Point(
                            dragStartPoint.x - originalShapePosition.x,
                            dragStartPoint.y - originalShapePosition.y);

                    // Stocker les positions originales de toutes les formes sélectionnées
                    for (Shape s : selectedShapes) {
                        Rectangle b = s.getBounds();
                        originalPositions.put(s, new Point(b.getX(), b.getY()));
                    }

                    // Set dragging state (internal whiteboard drag)
                    isDragging = true;

                    // Notify the mediator that a drag started *from* the whiteboard
                    if (dragMediator != null && dragStartPoint != null) {
                        // Pass 'this' (the WhiteBoard) as the source component
                        // Use the initial click point (dragStartPoint) relative to the whiteboard
                        dragMediator.startDrag(this, this, dragStartPoint.x, dragStartPoint.y);
                        System.out.println("[WhiteBoard] Notified mediator of internal drag start.");
                    } else {
                        System.err.println(
                                "[WhiteBoard] Could not notify mediator of drag start (mediator or dragStartPoint is null).");
                    }
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
                // Ensure the shape stays within the whiteboard bounds with a small margin
                newX = Math.max(-20, Math.min(newX, getWidth() - 20));
                newY = Math.max(-20, Math.min(newY, getHeight() - 20));
                activeShape.setPosition(newX, newY);
            }
            // Si plusieurs formes sont sélectionnées, les déplacer toutes ensemble
            else if (selectedShapes.size() > 1) {
                // Déplacer toutes les formes sélectionnées en fonction de leur position
                // d'origine
                for (Shape shape : selectedShapes) {
                    Point originalPos = originalPositions.get(shape);
                    if (originalPos != null) {
                        // Calculer la nouvelle position en ajoutant le delta à la position d'origine
                        int shapeNewX = originalPos.x + deltaX;
                        int shapeNewY = originalPos.y + deltaY;

                        // Ensure the shape stays within the whiteboard bounds with a small margin
                        shapeNewX = Math.max(-20, Math.min(shapeNewX, getWidth() - 20));
                        shapeNewY = Math.max(-20, Math.min(shapeNewY, getHeight() - 20));

                        shape.setPosition(shapeNewX, shapeNewY);
                    }
                }
            }

            // Check if we're dragging shapes to the trash panel or toolbar panel
            if (dragMediator != null && !selectedShapes.isEmpty()) {
                // Convert whiteboard coordinates to screen coordinates
                Point screenPoint = new Point(e.getX(), e.getY());
                try {
                    screenPoint.translate(getLocationOnScreen().x, getLocationOnScreen().y);

                    // Use the mediator to check if we're over the trash panel
                    boolean isOverTrash = dragMediator.checkPointOverTrash(screenPoint);

                    // Update the trash panel visual state and our internal state
                    setDraggingToTrash(isOverTrash);

                    // If not over trash, check if we're over the toolbar panel
                    if (!isOverTrash) {
                        // Use the mediator to check if we're over the toolbar panel
                        boolean isOverToolbar = dragMediator.checkPointOverToolbar(screenPoint);

                        // Update the toolbar panel visual state and our internal state
                        setDraggingToToolbar(isOverToolbar);
                    } else {
                        // If over trash, make sure we're not also over toolbar
                        setDraggingToToolbar(false);
                    }
                } catch (Exception ex) {
                    // Ignore any exceptions during coordinate conversion
                    System.out.println("[WhiteBoard] Error checking drag position: " + ex.getMessage());
                }
            }

            // Request a repaint to show the shapes in their new positions
            repaint();
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        // --- Check for external drag ending on WhiteBoard ---
        if (dragMediator != null && dragMediator.isDragging() && dragMediator.getSourceComponentForDrag() != this) {
            System.out.println("[WhiteBoard] Mouse released, delegating endDrag to mediator for external drag.");
            // Pass coordinates relative to the WhiteBoard
            dragMediator.endDrag(e.getX(), e.getY());
            // Reset internal state just in case, although mediator should handle most of it
            isDragging = false; // Reset internal flag
            activeShape = null;
            isSelectionRectActive = false;
            selectionStart = null;
            selectionEnd = null;
            // Don't repaint here, mediator's endDrag should handle repaint
            return; // Skip internal whiteboard release logic
        }
        // --- End check for external drag ---

        // Si on a dessiné un rectangle de sélection (internal whiteboard interaction)
        if (isSelectionRectActive && selectionStart != null && selectionEnd != null) {
            System.out.println("[WhiteBoard] Handling mouse release for selection rectangle.");
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

        // Check if an internal drag operation (started on whiteboard) was in progress
        // and completed
        // Use mediator state to confirm it was an internal drag
        if (isDragging && activeShape != null && !originalPositions.isEmpty() && dragMediator != null
                && dragMediator.getSourceComponentForDrag() == this) {
            System.out.println("[WhiteBoard] Handling mouse release for internal shape drag.");
            // Convert whiteboard coordinates to screen coordinates for checking panels
            Point screenPoint = new Point(e.getX(), e.getY());
            try {
                screenPoint.translate(getLocationOnScreen().x, getLocationOnScreen().y);
            } catch (Exception ex) {
                // Ignore any exceptions during coordinate conversion
                System.out.println("[WhiteBoard] Error converting coordinates: " + ex.getMessage());
            }

            // Check if we're dropping shapes on the trash panel
            if (isBeingDraggedToTrash && dragMediator != null && screenPoint != null) {
                try {
                    // Check if we're still over the trash panel
                    if (dragMediator.checkPointOverTrash(screenPoint)) {
                        System.out.println("[WhiteBoard] Dropping shapes on trash panel");
                        // Delete the selected shapes
                        deleteSelectedShapes();

                        // Reset drag state variables
                        isDragging = false;
                        dragStartPoint = null;
                        originalShapePosition = null;
                        dragOffset = null;
                        originalPositions.clear();
                        isBeingDraggedToTrash = false;

                        // Repaint to update the view
                        repaint();
                        return;
                    }
                } catch (Exception ex) {
                    // Ignore any exceptions during coordinate conversion
                    System.out.println("[WhiteBoard] Error checking trash panel: " + ex.getMessage());
                }

                // Reset the trash panel visual state
                setDraggingToTrash(false);
            }

            // Check if we're dropping shapes on the toolbar panel
            boolean droppedOnToolbar = false;
            if (isBeingDraggedToToolbar && dragMediator != null && screenPoint != null && toolbarPanel != null) {
                try {
                    // Check if we're still over the toolbar panel
                    if (dragMediator.checkPointOverToolbar(screenPoint)) {
                        System.out.println("[WhiteBoard] Dropping shapes on toolbar panel - attempting to add");
                        // Call the toolbar panel's method to add the shapes
                        droppedOnToolbar = toolbarPanel.addSelectedShapesToToolbar();

                        // Reset drag state variables immediately after successful drop on toolbar
                        isDragging = false;
                        dragStartPoint = null;
                        originalShapePosition = null;
                        dragOffset = null;
                        originalPositions.clear();
                        isBeingDraggedToToolbar = false; // Reset toolbar drag state

                        // Reset toolbar visual state via mediator
                        dragMediator.checkPointOverToolbar(new Point(-1, -1)); // Force reset

                        // Repaint to update the view
                        repaint();
                        return; // Exit early, don't treat as a move
                    }
                } catch (Exception ex) {
                    // Ignore any exceptions during coordinate conversion or toolbar interaction
                    System.out.println("[WhiteBoard] Error checking/adding to toolbar panel: " + ex.getMessage());
                }

                // Reset the toolbar panel visual state if drop wasn't successful or error
                // occurred
                setDraggingToToolbar(false);
                if (dragMediator != null) {
                    dragMediator.checkPointOverToolbar(new Point(-1, -1)); // Force reset
                }
            }

            // If not dropped on trash or toolbar, proceed with move command logic
            // Create a map to store the final positions of all selected shapes
            Map<Shape, Point> finalPositions = new HashMap<>();
            boolean positionsChanged = false;

            // Collect the final positions of all selected shapes
            for (Shape shape : selectedShapes) {
                Rectangle bounds = shape.getBounds();
                Point finalPosition = new Point(bounds.getX(), bounds.getY());
                finalPositions.put(shape, finalPosition);

                // Check if at least one shape has moved
                Point originalPos = originalPositions.get(shape);
                if (originalPos != null && !originalPos.equals(finalPosition)) {
                    positionsChanged = true;
                }
            }

            // Only create and execute a command if at least one position actually changed
            if (positionsChanged) {
                if (selectedShapes.size() == 1) {
                    // If only one shape is selected, use the simpler MoveShapeCommand
                    MoveShapeCommand moveCommand = new MoveShapeCommand(
                            activeShape,
                            originalPositions.get(activeShape),
                            finalPositions.get(activeShape));

                    commandHistory.executeCommand(moveCommand);
                } else {
                    // If multiple shapes are selected, use MoveShapesCommand
                    MoveShapesCommand moveShapesCommand = new MoveShapesCommand(
                            selectedShapes,
                            originalPositions,
                            finalPositions);

                    commandHistory.executeCommand(moveShapesCommand);
                }
            }

            // Reset drag state variables regardless of whether a move occurred
            isDragging = false;
            dragStartPoint = null;
            originalShapePosition = null;
            dragOffset = null;
            originalPositions.clear();

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
                    g2d.fillOval(bounds.getX() + bounds.getWidth() / 2 - 3,
                            bounds.getY() + bounds.getHeight() / 2 - 3, 6, 6);
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

        // Draw overlay for shapes being dragged to trash
        System.out.println("[WhiteBoard] Paint method - isDragging=" + isDragging + ", isBeingDraggedToTrash="
                + isBeingDraggedToTrash + ", selectedShapes.isEmpty()=" + selectedShapes.isEmpty());
        if (isDragging && isBeingDraggedToTrash && !selectedShapes.isEmpty()) {
            System.out.println("[WhiteBoard] Condition met for drawing trash overlay");
            Graphics2D g2d = (Graphics2D) offscreenGraphics.create();
            try {
                drawTrashOverlay(g2d);
            } finally {
                g2d.dispose();
            }
        }

        // Draw the offscreen buffer to the screen
        g.drawImage(offscreenBuffer, 0, 0, this);
    }

    /**
     * Draws an overlay when shapes are being dragged to the trash panel
     *
     * @param g2d The graphics context to draw on
     */
    private void drawTrashOverlay(Graphics2D g2d) {
        System.out.println("[WhiteBoard] Drawing trash overlay, isDragging=" + isDragging + ", isBeingDraggedToTrash="
                + isBeingDraggedToTrash);

        if (dragMediator == null) {
            System.out.println("[WhiteBoard] Cannot draw trash overlay - dragMediator is null");
            return;
        }

        // Enable anti-aliasing for smoother drawing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Save the original graphics state
        Composite originalComposite = g2d.getComposite();
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        try {
            // Get the mouse position
            Point mousePos = getMousePosition();
            if (mousePos == null) {
                // If mouse position is not available, use the center of the whiteboard
                mousePos = new Point(getWidth() / 2, getHeight() / 2);
            }

            // Set semi-transparent composite for overlay
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

            // Draw a red overlay for each selected shape
            for (Shape shape : selectedShapes) {
                Rectangle bounds = shape.getBounds();

                // Fill with semi-transparent red
                g2d.setColor(new Color(255, 0, 0, 128)); // Red with 50% transparency
                g2d.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

                // Draw border with a thicker stroke
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            }

            // Draw a dashed line from the active shape to the mouse cursor
            if (activeShape != null) {
                Rectangle bounds = activeShape.getBounds();
                int centerX = bounds.getX() + bounds.getWidth() / 2;
                int centerY = bounds.getY() + bounds.getHeight() / 2;

                // Draw dashed line
                g2d.setColor(Color.RED);
                float[] dash = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                g2d.drawLine(centerX, centerY, mousePos.x, mousePos.y);

                // Draw a small trash icon near the cursor
                int iconSize = 32; // Larger icon for better visibility
                g2d.setColor(new Color(255, 0, 0, 200));
                g2d.fillOval(mousePos.x - iconSize / 2, mousePos.y - iconSize / 2, iconSize, iconSize);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3.0f)); // Thicker stroke for better visibility

                // Draw an X inside the circle
                int offset = iconSize / 4;
                g2d.drawLine(mousePos.x - offset, mousePos.y - offset, mousePos.x + offset, mousePos.y + offset);
                g2d.drawLine(mousePos.x + offset, mousePos.y - offset, mousePos.x - offset, mousePos.y + offset);
            }
        } finally {
            // Restore the original graphics state
            g2d.setComposite(originalComposite);
            g2d.setStroke(originalStroke);
            g2d.setColor(originalColor);
        }
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
     * Gets the current prototype registry
     *
     * @return The current shape prototype registry
     */
    public ShapePrototypeRegistry getPrototypeRegistry() {
        return this.prototypeRegistry;
    }

    /**
     * Gets the command history manager for this whiteboard.
     *
     * @return The CommandHistory instance.
     */
    public CommandHistory getCommandHistory() {
        return this.commandHistory;
    }

    /**
     * Gets the list of shapes currently on the whiteboard.
     * Note: Modifying this list directly bypasses the command pattern.
     * Use commands for adding/removing shapes where possible.
     *
     * @return The list of shapes.
     */
    public List<Shape> getShapesList() {
        return this.shapes;
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

            // Notify state change listener
            notifyStateChanged("Shape created on whiteboard");
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

    /**
     * Deletes all currently selected shapes from the whiteboard
     *
     * @return true if any shapes were deleted, false otherwise
     */
    public boolean deleteSelectedShapes() {
        if (selectedShapes.isEmpty()) {
            return false;
        }

        // Remove all selected shapes from the shapes list
        boolean removed = shapes.removeAll(selectedShapes);

        // Clear the selection
        selectedShapes.clear();
        activeShape = null;

        // Reset the dragging to trash state
        isBeingDraggedToTrash = false;

        // Reset the trash panel visual state if we have a mediator
        if (dragMediator != null) {
            dragMediator.resetTrashPanelState();
        }

        // Repaint to show the updated state
        repaint();

        // Notify state change listener
        if (removed) {
            notifyStateChanged("Shapes deleted from whiteboard");
        }

        return removed;
    }

    public void setRelativeBounds(double xPercent, double yPercent, double widthPercent, double heightPercent) {
        this.relX = xPercent / 100.0;
        this.relY = yPercent / 100.0;
        this.relW = widthPercent / 100.0;
        this.relH = heightPercent / 100.0;
    }

    // Draggable interface implementation
    private boolean isBeingDraggedToTrash = false;
    private boolean isBeingDraggedToToolbar = false;
    private DragMediator dragMediator;

    /**
     * Sets the drag mediator for this whiteboard
     *
     * @param mediator The mediator to use
     */
    public void setDragMediator(DragMediator mediator) {
        this.dragMediator = mediator;
    }

    /**
     * Sets the toolbar panel reference for adding shapes
     *
     * @param panel The ToolbarPanel instance
     */
    public void setToolbarPanel(ToolbarPanel panel) {
        this.toolbarPanel = panel;
    }

    @Override
    public void startDrag(int x, int y) {
        // This method is called when a shape is being dragged from the whiteboard
        // We don't need to do anything special here as the mouse handlers already
        // handle selection
    }

    @Override
    public void drag(int x, int y) {
        // This method is called when a shape is being dragged
        // The actual dragging is handled by the mouse motion handlers
    }

    @Override
    public void endDrag(int x, int y) {
        // This method is called when a shape drag operation ends
        // If the shape was being dragged to the trash, delete it
        if (isBeingDraggedToTrash) {
            deleteSelectedShapes();
            isBeingDraggedToTrash = false;
        }

        // Reset the toolbar drag state
        if (isBeingDraggedToToolbar) {
            System.out.println("[WhiteBoard] Ending drag to toolbar");
            isBeingDraggedToToolbar = false;
        }
    }

    @Override
    public String getShapeType() {
        // For the whiteboard, we don't have a specific shape type
        // This is used when dragging shapes from the whiteboard to the trash
        return "SelectedShapes";
    }

    /**
     * Gets a list of all currently selected shapes
     *
     * @return A list of selected shapes
     */
    public List<Shape> getSelectedShapes() {
        return new ArrayList<>(selectedShapes);
    }

    /**
     * Clears the current selection, deselecting all shapes.
     */
    public void clearSelection() {
        if (!selectedShapes.isEmpty()) {
            for (Shape s : selectedShapes) {
                s.setSelected(false);
            }
            selectedShapes.clear();
            activeShape = null; // Ensure no shape remains active
            System.out.println("[WhiteBoard] Selection cleared.");
            // No repaint here, let the caller decide when to repaint
        }
    }

    /**
     * Adds a single shape to the current selection.
     * Does not clear previous selections.
     *
     * @param shape The shape to add to the selection.
     */
    public void addSelectedShape(Shape shape) {
        if (shape != null && !selectedShapes.contains(shape)) {
            selectedShapes.add(shape);
            shape.setSelected(true); // Ensure the shape knows it's selected
            System.out.println("[WhiteBoard] Added shape to selection: " + shape);
            // No repaint here, let the caller decide
        }
    }

    /**
     * Sets whether the selected shapes are being dragged to the trash
     *
     * @param isDraggingToTrash Whether the shapes are being dragged to the trash
     */
    public void setDraggingToTrash(boolean isDraggingToTrash) {
        if (this.isBeingDraggedToTrash != isDraggingToTrash) {
            this.isBeingDraggedToTrash = isDraggingToTrash;
            // Debug output
            System.out.println("[WhiteBoard] Setting isBeingDraggedToTrash to " + isDraggingToTrash);
            // Trigger a repaint to show or hide the overlay
            repaint();
        }
    }

    /**
     * Sets whether the selected shapes are being dragged to the toolbar
     *
     * @param isDraggingToToolbar Whether the shapes are being dragged to the
     *                            toolbar
     */
    public void setDraggingToToolbar(boolean isDraggingToToolbar) {
        if (this.isBeingDraggedToToolbar != isDraggingToToolbar) {
            this.isBeingDraggedToToolbar = isDraggingToToolbar;
            // Debug output
            System.out.println("[WhiteBoard] Setting isBeingDraggedToToolbar to " + isDraggingToToolbar);
            // Trigger a repaint to show or hide the overlay
            repaint();
        }
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

    /**
     * Sets the state change listener for this whiteboard.
     * The listener will be notified when significant state changes occur.
     *
     * @param listener The state change listener
     */
    public void setStateChangeListener(StateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    /**
     * Notifies the state change listener that a significant state change has
     * occurred.
     *
     * @param description A description of the change
     */
    private void notifyStateChanged(String description) {
        if (stateChangeListener != null) {
            stateChangeListener.onStateChanged(this, description);
        }
    }

    // --- Memento Pattern Implementation ---

    /**
     * Creates a memento containing the current state of the whiteboard (shapes).
     *
     * @return A ShapeMemento object.
     */
    public ShapeMemento createMemento() {
        System.out.println("[WhiteBoard] Creating Memento...");
        // Pass the current list of shapes to the memento constructor
        return new ShapeMemento(new ArrayList<>(this.shapes)); // Pass a copy
    }

    /**
     * Restores the whiteboard state from a memento.
     *
     * @param memento The memento object containing the state to restore.
     */
    public void restoreFromMemento(ShapeMemento memento) {
        if (memento == null) {
            System.err.println("[WhiteBoard] Cannot restore from null memento.");
            return;
        }
        System.out.println("[WhiteBoard] Restoring state from Memento...");
        // Get the shapes from the memento (these are already clones)
        this.shapes = memento.getShapesState();
        // Clear selection and command history as the state is completely replaced
        this.selectedShapes.clear();
        this.activeShape = null;
        this.commandHistory.clear(); // Clear history after loading state
        System.out.println("[WhiteBoard] Memento restore complete. Shape count: " + this.shapes.size());
        repaint(); // Repaint to show the restored shapes
    }
}
