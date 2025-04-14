package com.editor.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.editor.commands.CommandHistory;
import com.editor.commands.CreateShapeCommand;
import com.editor.commands.EditShapeCommand;
import com.editor.commands.GroupShapesCommand;
import com.editor.commands.MoveShapeCommand;
import com.editor.commands.MoveShapesCommand;
import com.editor.commands.UngroupShapesCommand;
import com.editor.drawing.AWTDrawing;
import com.editor.drawing.Drawer;
import com.editor.gui.button.Draggable;
import com.editor.gui.panel.ToolbarPanel; 
import com.editor.mediator.DragMediator;
import com.editor.memento.ShapeMemento;
import com.editor.shapes.Circle;
import com.editor.shapes.Rectangle;
import com.editor.shapes.RegularPolygon;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapeGroup;
import com.editor.shapes.ShapePrototypeRegistry; 
import com.editor.state.StateChangeListener;

public class WhiteBoard extends Canvas implements Draggable {
    private double relX, relY, relW, relH;
    private Color backgroundColor = Color.WHITE;
    private List<Shape> shapes = new ArrayList<>();
    private final List<Shape> selectedShapes = new ArrayList<>();
    private final CommandHistory commandHistory = new CommandHistory();
    private ShapePrototypeRegistry prototypeRegistry = null;
    private String currentShapeType = null;
    private Shape activeShape; 

    
    private StateChangeListener stateChangeListener;
    private Point dragStartPoint; 
    private Point originalShapePosition; 
    private Point dragOffset; 
    private boolean isDragging = false;
    private boolean isCtrlPressed = false; 

    
    private Point selectionStart; 
    private Point selectionEnd; 
    private boolean isSelectionRectActive = false; 

    
    private Image offscreenBuffer;
    private Graphics offscreenGraphics;

    
    private ToolbarPanel toolbarPanel;

    public WhiteBoard(int width, int height, Color white) {
        this.setPreferredSize(new Dimension(width, height));
        this.backgroundColor = white;
        this.setBackground(white);

        setupMouseListeners();
        setupKeyListeners();

        
        setFocusable(true);
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow(); 

                
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
        
        boolean clickedOnSelected = false;
        for (Shape shape : selectedShapes) {
            if (shape.isSelected(e.getX(), e.getY())) {
                clickedOnSelected = true;
                break;
            }
        }

        
        if (clickedOnSelected) {
            showContextMenu(e.getX(), e.getY());
        }
    }

    /**
     * Affiche le menu contextuel aux coordonnées spécifiées
     */
    private void showContextMenu(int x, int y) {
        PopupMenu contextMenu = new PopupMenu();

        
        MenuItem editItem = new MenuItem("Edit");
        editItem.addActionListener((ActionEvent e) -> {
            editSelectedShape();
        });
        contextMenu.add(editItem);

        
        MenuItem groupItem = new MenuItem("Group");
        groupItem.addActionListener((ActionEvent e) -> {
            groupSelectedShapes();
        });
        contextMenu.add(groupItem);

        
        MenuItem ungroupItem = new MenuItem("Ungroup");
        ungroupItem.addActionListener((ActionEvent e) -> {
            ungroupSelectedShapes();
        });

        
        boolean hasGroup = false;
        for (Shape shape : selectedShapes) {
            if (shape instanceof ShapeGroup) {
                hasGroup = true;
                break;
            }
        }
        ungroupItem.setEnabled(hasGroup);
        contextMenu.add(ungroupItem);

        
        this.add(contextMenu);
        contextMenu.show(this, x, y);
    }

    /**
     * Edite la forme sélectionnée
     */
    private void editSelectedShape() {
        if (selectedShapes.isEmpty()) {
            return; 
        }

        
        Shape firstShape = selectedShapes.get(0);
        Color currentBorderColor = getBorderColor(firstShape);
        Color currentFillColor = getFillColor(firstShape);
        double currentRotation = getRotation(firstShape);

        
        int currentBorderRadius = 0;
        boolean isRectangle = firstShape instanceof Rectangle;
        if (isRectangle) {
            currentBorderRadius = ((Rectangle) firstShape).getBorderRadius();
        }

        
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        final JDialog editDialog = new JDialog(parentFrame, "Edit Shape", true);
        editDialog.setLayout(new BorderLayout());

        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Border Color:"), gbc);

        final JButton borderColorButton = new JButton();
        borderColorButton.setBackground(currentBorderColor);
        borderColorButton.setPreferredSize(new Dimension(100, 25));
        final Color[] selectedBorderColor = { currentBorderColor };

        borderColorButton.addActionListener((ActionEvent e) -> {
            Color newColor = JColorChooser.showDialog(editDialog, "Choose Border Color", selectedBorderColor[0]);
            if (newColor != null) {
                selectedBorderColor[0] = newColor;
                borderColorButton.setBackground(newColor);
            }
        });

        gbc.gridx = 1;
        formPanel.add(borderColorButton, gbc);

        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Fill Color:"), gbc);

        final JButton fillColorButton = new JButton();
        fillColorButton.setBackground(currentFillColor);
        fillColorButton.setPreferredSize(new Dimension(100, 25));
        final Color[] selectedFillColor = { currentFillColor };

        fillColorButton.addActionListener((ActionEvent e) -> {
            Color newColor = JColorChooser.showDialog(editDialog, "Choose Fill Color", selectedFillColor[0]);
            if (newColor != null) {
                selectedFillColor[0] = newColor;
                fillColorButton.setBackground(newColor);
            }
        });

        gbc.gridx = 1;
        formPanel.add(fillColorButton, gbc);

        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Rotation (degrees):"), gbc);

        final JTextField rotationField = new JTextField(String.valueOf(currentRotation));
        gbc.gridx = 1;
        formPanel.add(rotationField, gbc);

        
        final JSlider borderRadiusSlider;
        final JLabel borderRadiusValueLabel;

        if (isRectangle) {
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            formPanel.add(new JLabel("Border Radius:"), gbc);

            
            borderRadiusSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, currentBorderRadius);

            
            borderRadiusSlider.setMajorTickSpacing(10);
            borderRadiusSlider.setMinorTickSpacing(5);
            borderRadiusSlider.setPaintTicks(true);
            borderRadiusSlider.setPaintLabels(true);
            borderRadiusSlider.setSnapToTicks(false); 

            
            borderRadiusSlider.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            borderRadiusSlider.setBackground(new Color(240, 240, 240)); 

            
            borderRadiusValueLabel = new JLabel(String.valueOf(currentBorderRadius));
            borderRadiusValueLabel.setHorizontalAlignment(JLabel.RIGHT);
            borderRadiusValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            
            borderRadiusSlider.addChangeListener(e -> {
                int value = borderRadiusSlider.getValue();
                borderRadiusValueLabel.setText(String.valueOf(value));

                
                if (!selectedShapes.isEmpty() && selectedShapes.get(0) instanceof Rectangle) {
                    Rectangle previewRect = (Rectangle) selectedShapes.get(0);
                    previewRect.setBorderRadius(value);
                    repaint(); 
                }
            });

            
            JPanel sliderPanel = new JPanel(new BorderLayout());
            sliderPanel.add(borderRadiusSlider, BorderLayout.CENTER);
            sliderPanel.add(borderRadiusValueLabel, BorderLayout.EAST);

            
            gbc.gridx = 1;
            formPanel.add(sliderPanel, gbc);
        } else {
            
            borderRadiusSlider = new JSlider();

        }

        
        editDialog.add(formPanel, BorderLayout.CENTER);

        
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener((ActionEvent e) -> {
            try {
                
                double rotation = Double.parseDouble(rotationField.getText());

                
                int borderRadius = 0;
                if (isRectangle) {
                    borderRadius = borderRadiusSlider.getValue();
                }

                
                EditShapeCommand command = new EditShapeCommand(
                        selectedShapes,
                        selectedBorderColor[0],
                        selectedFillColor[0],
                        rotation,
                        borderRadius);

                
                command.execute();
                commandHistory.addCommand(command);

                repaint();

                
                notifyStateChanged("Shape properties edited");

                editDialog.dispose();
            } catch (NumberFormatException ex) {
                
                JOptionPane.showMessageDialog(editDialog,
                        "Invalid rotation value. Please enter a number.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener((ActionEvent e) -> {
            editDialog.dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        
        editDialog.setSize(450, isRectangle ? 350 : 250);
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
    }

    /**
     * Gets the border color of a shape
     */
    private Color getBorderColor(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getBorderColor();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getBorderColor();
        } else if (shape instanceof Circle) {
            return ((Circle) shape).getBorderColor();
        } else if (shape instanceof ShapeGroup) {
            return ((ShapeGroup) shape).getBorderColor();
        }
        return Color.BLACK; 
    }

    /**
     * Gets the fill color of a shape
     */
    private Color getFillColor(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getFillColor();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getFillColor();
        } else if (shape instanceof Circle) {
            return ((Circle) shape).getFillColor();
        }
        return Color.WHITE; 
    }

    /**
     * Gets the rotation of a shape
     */
    private double getRotation(Shape shape) {
        if (shape instanceof Rectangle) {
            return ((Rectangle) shape).getRotation();
        } else if (shape instanceof RegularPolygon) {
            return ((RegularPolygon) shape).getRotation();
        } else if (shape instanceof Circle) {
            return ((Circle) shape).getRotation();
        } else if (shape instanceof ShapeGroup) {
            return ((ShapeGroup) shape).getRotation();
        }
        return 0.0; 
    }

    /**
     * Groupe les formes sélectionnées
     */
    private void groupSelectedShapes() {
        if (selectedShapes.size() < 2) {
            return; 
        }

        
        GroupShapesCommand command = new GroupShapesCommand(shapes, new ArrayList<>(selectedShapes));
        commandHistory.executeCommand(command);

        
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

        
        for (Shape shape : selectedShapes) {
            if (shape instanceof ShapeGroup) {
                ShapeGroup group = (ShapeGroup) shape;
                UngroupShapesCommand command = new UngroupShapesCommand(shapes, group);
                commandHistory.executeCommand(command);

                
                newSelection.addAll(command.getUngroupedShapes());
            } else {
                newSelection.add(shape);
            }
        }

        
        for (Shape shape : selectedShapes) {
            shape.setSelected(false);
        }
        selectedShapes.clear();

        for (Shape shape : newSelection) {
            shape.setSelected(true);
            selectedShapes.add(shape);
        }

        
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

    private final Map<Shape, Point> originalPositions = new HashMap<>();

    private void handleMousePress(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            
            isCtrlPressed = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;

            
            isDragging = false;
            originalShapePosition = null;
            dragStartPoint = null;
            dragOffset = null;
            originalPositions.clear();

            
            isSelectionRectActive = false;
            selectionStart = null;
            selectionEnd = null;

            
            boolean foundShape = false;
            Shape clickedShape = null;

            for (Shape shape : shapes) {
                if (shape.isSelected(e.getX(), e.getY())) {
                    
                    foundShape = true;
                    clickedShape = shape;
                    break;
                }
            }

            if (foundShape) {
                
                
                if (clickedShape != null && selectedShapes.contains(clickedShape) && isCtrlPressed) {
                    selectedShapes.remove(clickedShape);
                    clickedShape.setSelected(false);
                } else {
                    
                    if (selectedShapes.contains(clickedShape)) {
                        
                        activeShape = clickedShape;
                    } else {
                        
                        if (!isCtrlPressed) {
                            for (Shape s : selectedShapes) {
                                s.setSelected(false);
                            }
                            selectedShapes.clear();
                        }

                        
                        if (clickedShape != null) {
                            selectedShapes.add(clickedShape);
                            clickedShape.setSelected(true);
                        }

                        
                        activeShape = clickedShape;
                    }

                    
                    dragStartPoint = e.getPoint();

                    
                    Rectangle bounds = activeShape.getBounds();
                    originalShapePosition = new Point(bounds.getX(), bounds.getY());

                    
                    dragOffset = new Point(
                            dragStartPoint.x - originalShapePosition.x,
                            dragStartPoint.y - originalShapePosition.y);

                    
                    for (Shape s : selectedShapes) {
                        Rectangle b = s.getBounds();
                        originalPositions.put(s, new Point(b.getX(), b.getY()));
                    }

                    
                    isDragging = true;

                    
                    if (dragMediator != null && dragStartPoint != null) {
                        
                        
                        dragMediator.startDrag(this, this, dragStartPoint.x, dragStartPoint.y);
                        System.out.println("[WhiteBoard] Notified mediator of internal drag start.");
                    } else {
                        System.err.println(
                                "[WhiteBoard] Could not notify mediator of drag start (mediator or dragStartPoint is null).");
                    }
                }
            }

            
            if (!foundShape) {
                
                if (!isCtrlPressed) {
                    for (Shape s : selectedShapes) {
                        s.setSelected(false);
                    }
                    selectedShapes.clear();
                    activeShape = null;
                }

                
                selectionStart = e.getPoint();
                selectionEnd = e.getPoint(); 
                isSelectionRectActive = true;
            }

            
            if (selectedShapes.isEmpty() && currentShapeType != null && prototypeRegistry != null) {
                createShapeAt(e.getX(), e.getY());
            }

            repaint();
        }
    }

    private void handleMouseDrag(MouseEvent e) {
        
        if (isSelectionRectActive && selectionStart != null) {
            
            selectionEnd = e.getPoint();
            repaint();
            return;
        }

        
        if (isDragging && activeShape != null && dragOffset != null) {
            
            
            int newX = e.getX() - dragOffset.x;
            int newY = e.getY() - dragOffset.y;

            
            int deltaX = newX - originalShapePosition.x;
            int deltaY = newY - originalShapePosition.y;

            
            if (selectedShapes.size() == 1) {
                
                newX = Math.max(-20, Math.min(newX, getWidth() - 20));
                newY = Math.max(-20, Math.min(newY, getHeight() - 20));
                activeShape.setPosition(newX, newY);
            }
            
            else if (selectedShapes.size() > 1) {
                
                
                for (Shape shape : selectedShapes) {
                    Point originalPos = originalPositions.get(shape);
                    if (originalPos != null) {
                        
                        int shapeNewX = originalPos.x + deltaX;
                        int shapeNewY = originalPos.y + deltaY;

                        
                        shapeNewX = Math.max(-20, Math.min(shapeNewX, getWidth() - 20));
                        shapeNewY = Math.max(-20, Math.min(shapeNewY, getHeight() - 20));

                        shape.setPosition(shapeNewX, shapeNewY);
                    }
                }
            }

            
            if (dragMediator != null && !selectedShapes.isEmpty()) {
                
                Point screenPoint = new Point(e.getX(), e.getY());
                try {
                    screenPoint.translate(getLocationOnScreen().x, getLocationOnScreen().y);

                    
                    boolean isOverTrash = dragMediator.checkPointOverTrash(screenPoint);

                    
                    setDraggingToTrash(isOverTrash);

                    
                    if (!isOverTrash) {
                        
                        boolean isOverToolbar = dragMediator.checkPointOverToolbar(screenPoint);

                        
                        setDraggingToToolbar(isOverToolbar);
                    } else {
                        
                        setDraggingToToolbar(false);
                    }
                } catch (Exception ex) {
                    
                    System.out.println("[WhiteBoard] Error checking drag position: " + ex.getMessage());
                }
            }

            
            repaint();
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        
        if (dragMediator != null && dragMediator.isDragging() && dragMediator.getSourceComponentForDrag() != this) {
            System.out.println("[WhiteBoard] Mouse released, delegating endDrag to mediator for external drag.");
            
            dragMediator.endDrag(e.getX(), e.getY());
            
            isDragging = false; 
            activeShape = null;
            isSelectionRectActive = false;
            selectionStart = null;
            selectionEnd = null;
            
            return; 
        }
        

        
        if (isSelectionRectActive && selectionStart != null && selectionEnd != null) {
            System.out.println("[WhiteBoard] Handling mouse release for selection rectangle.");
            
            int x1 = Math.min(selectionStart.x, selectionEnd.x);
            int y1 = Math.min(selectionStart.y, selectionEnd.y);
            int x2 = Math.max(selectionStart.x, selectionEnd.x);
            int y2 = Math.max(selectionStart.y, selectionEnd.y);

            
            if (x2 - x1 < 5 || y2 - y1 < 5) {
                isSelectionRectActive = false;
                selectionStart = null;
                selectionEnd = null;
                repaint();
                return;
            }

            
            if (!isCtrlPressed) {
                for (Shape s : selectedShapes) {
                    s.setSelected(false);
                }
                selectedShapes.clear();
            }

            
            for (Shape shape : shapes) {
                Rectangle bounds = shape.getBounds();

                
                if (bounds.getX() >= x1 && bounds.getX() + bounds.getWidth() <= x2 &&
                        bounds.getY() >= y1 && bounds.getY() + bounds.getHeight() <= y2) {

                    
                    if (!selectedShapes.contains(shape)) {
                        selectedShapes.add(shape);
                        shape.setSelected(true);
                    }
                }
            }

            
            isSelectionRectActive = false;
            selectionStart = null;
            selectionEnd = null;

            repaint();
            return;
        }

        
        
        
        if (isDragging && activeShape != null && !originalPositions.isEmpty() && dragMediator != null
                && dragMediator.getSourceComponentForDrag() == this) {
            System.out.println("[WhiteBoard] Handling mouse release for internal shape drag.");
            
            Point screenPoint = new Point(e.getX(), e.getY());
            try {
                screenPoint.translate(getLocationOnScreen().x, getLocationOnScreen().y);
            } catch (Exception ex) {
                
                System.out.println("[WhiteBoard] Error converting coordinates: " + ex.getMessage());
            }

            
            if (isBeingDraggedToTrash && dragMediator != null) {
                try {
                    
                    if (dragMediator.checkPointOverTrash(screenPoint)) {
                        System.out.println("[WhiteBoard] Dropping shapes on trash panel");
                        
                        deleteSelectedShapes();

                        
                        isDragging = false;
                        dragStartPoint = null;
                        originalShapePosition = null;
                        dragOffset = null;
                        originalPositions.clear();
                        isBeingDraggedToTrash = false;

                        
                        repaint();
                        return;
                    }
                } catch (Exception ex) {
                    
                    System.out.println("[WhiteBoard] Error checking trash panel: " + ex.getMessage());
                }

                
                setDraggingToTrash(false);
            }

            if (isBeingDraggedToToolbar && dragMediator != null && toolbarPanel != null) {
                try {
                    
                    if (dragMediator.checkPointOverToolbar(screenPoint)) {
                        System.out.println("[WhiteBoard] Dropping shapes on toolbar panel - attempting to add");
                        
                        toolbarPanel.addSelectedShapesToToolbar();

                        
                        isDragging = false;
                        dragStartPoint = null;
                        originalShapePosition = null;
                        dragOffset = null;
                        originalPositions.clear();
                        isBeingDraggedToToolbar = false; 

                        
                        dragMediator.checkPointOverToolbar(new Point(-1, -1)); 

                        
                        repaint();
                        return; 
                    }
                } catch (Exception ex) {
                    
                    System.out.println("[WhiteBoard] Error checking/adding to toolbar panel: " + ex.getMessage());
                }

                
                
                setDraggingToToolbar(false);
                if (dragMediator != null) {
                    dragMediator.checkPointOverToolbar(new Point(-1, -1)); 
                }
            }

            
            
            Map<Shape, Point> finalPositions = new HashMap<>();
            boolean positionsChanged = false;

            
            for (Shape shape : selectedShapes) {
                Rectangle bounds = shape.getBounds();
                Point finalPosition = new Point(bounds.getX(), bounds.getY());
                finalPositions.put(shape, finalPosition);

                
                Point originalPos = originalPositions.get(shape);
                if (originalPos != null && !originalPos.equals(finalPosition)) {
                    positionsChanged = true;
                }
            }

            
            if (positionsChanged) {
                if (selectedShapes.size() == 1) {
                    
                    MoveShapeCommand moveCommand = new MoveShapeCommand(
                            activeShape,
                            originalPositions.get(activeShape),
                            finalPositions.get(activeShape));

                    commandHistory.executeCommand(moveCommand);
                } else {
                    
                    MoveShapesCommand moveShapesCommand = new MoveShapesCommand(
                            selectedShapes,
                            originalPositions,
                            finalPositions);

                    commandHistory.executeCommand(moveShapesCommand);
                }
            }

            
            isDragging = false;
            dragStartPoint = null;
            originalShapePosition = null;
            dragOffset = null;
            originalPositions.clear();

            
            repaint();
        }
    }

    @Override
    public void update(Graphics g) {
        
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        
        if (offscreenBuffer == null ||
                offscreenBuffer.getWidth(null) != getWidth() ||
                offscreenBuffer.getHeight(null) != getHeight()) {
            createOffscreenBuffer();
        }

        
        offscreenGraphics.setColor(backgroundColor);
        offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());

        
        Drawer drawer = new AWTDrawing((Graphics2D) offscreenGraphics);
        for (Shape shape : shapes) {
            shape.draw(drawer);
        }

        
        if (!selectedShapes.isEmpty()) {
            Graphics2D g2d = (Graphics2D) offscreenGraphics.create();
            try {
                
                float[] dash = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

                
                for (Shape shape : selectedShapes) {
                    Rectangle bounds = shape.getBounds();

                    if (isDragging && shape == activeShape) {
                        
                        g2d.setColor(new Color(255, 165, 0)); 
                    } else {
                        
                        g2d.setColor(new Color(0, 0, 255)); 
                    }

                    
                    g2d.drawRect(bounds.getX() - 2, bounds.getY() - 2,
                            bounds.getWidth() + 4, bounds.getHeight() + 4);

                    
                    g2d.setColor(Color.RED);
                    g2d.fillOval(bounds.getX() + bounds.getWidth() / 2 - 3,
                            bounds.getY() + bounds.getHeight() / 2 - 3, 6, 6);
                }
            } finally {
                g2d.dispose();
            }
        }

        
        if (isSelectionRectActive && selectionStart != null && selectionEnd != null) {
            Graphics2D g2d = (Graphics2D) offscreenGraphics.create();
            try {
                
                int x = Math.min(selectionStart.x, selectionEnd.x);
                int y = Math.min(selectionStart.y, selectionEnd.y);
                int width = Math.abs(selectionEnd.x - selectionStart.x);
                int height = Math.abs(selectionEnd.y - selectionStart.y);

                
                float[] dash = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

                
                g2d.setColor(new Color(0, 0, 255, 30)); 
                g2d.fillRect(x, y, width, height);

                
                g2d.setColor(new Color(0, 0, 255)); 
                g2d.drawRect(x, y, width, height);
            } finally {
                g2d.dispose();
            }
        }

        
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

        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        Composite originalComposite = g2d.getComposite();
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        try {
            
            Point mousePos = getMousePosition();
            if (mousePos == null) {
                
                mousePos = new Point(getWidth() / 2, getHeight() / 2);
            }

            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

            
            for (Shape shape : selectedShapes) {
                Rectangle bounds = shape.getBounds();

                
                g2d.setColor(new Color(255, 0, 0, 128)); 
                g2d.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

                
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            }

            
            if (activeShape != null) {
                Rectangle bounds = activeShape.getBounds();
                int centerX = bounds.getX() + bounds.getWidth() / 2;
                int centerY = bounds.getY() + bounds.getHeight() / 2;

                
                g2d.setColor(Color.RED);
                float[] dash = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                g2d.drawLine(centerX, centerY, mousePos.x, mousePos.y);

                
                int iconSize = 32; 
                g2d.setColor(new Color(255, 0, 0, 200));
                g2d.fillOval(mousePos.x - iconSize / 2, mousePos.y - iconSize / 2, iconSize, iconSize);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3.0f)); 

                
                int offset = iconSize / 4;
                g2d.drawLine(mousePos.x - offset, mousePos.y - offset, mousePos.x + offset, mousePos.y + offset);
                g2d.drawLine(mousePos.x + offset, mousePos.y - offset, mousePos.x - offset, mousePos.y + offset);
            }
        } finally {
            
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

            
            for (Shape s : selectedShapes) {
                s.setSelected(false);
            }
            selectedShapes.clear();

            
            selectedShapes.add(newShape);
            activeShape = newShape;
            newShape.setSelected(true);
            repaint();

            
            notifyStateChanged("Shape created on whiteboard");
        }
    }

    /**
     * Adds a shape to the center of the whiteboard using the current shape type
     */
    public void addShapeToCenter() {
        if (currentShapeType != null && prototypeRegistry != null) {
            
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

        
        boolean removed = shapes.removeAll(selectedShapes);

        
        selectedShapes.clear();
        activeShape = null;

        
        isBeingDraggedToTrash = false;

        
        if (dragMediator != null) {
            dragMediator.resetTrashPanelState();
        }

        
        repaint();

        
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
        
        
        
    }

    @Override
    public void drag(int x, int y) {
        
        
    }

    @Override
    public void endDrag(int x, int y) {
        
        
        if (isBeingDraggedToTrash) {
            deleteSelectedShapes();
            isBeingDraggedToTrash = false;
        }

        
        if (isBeingDraggedToToolbar) {
            System.out.println("[WhiteBoard] Ending drag to toolbar");
            isBeingDraggedToToolbar = false;
        }
    }

    @Override
    public String getShapeType() {
        
        
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
            activeShape = null; 
            System.out.println("[WhiteBoard] Selection cleared.");
            
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
            shape.setSelected(true); 
            System.out.println("[WhiteBoard] Added shape to selection: " + shape);
            
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
            
            System.out.println("[WhiteBoard] Setting isBeingDraggedToTrash to " + isDraggingToTrash);
            
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
            
            System.out.println("[WhiteBoard] Setting isBeingDraggedToToolbar to " + isDraggingToToolbar);
            
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

    

    /**
     * Creates a memento containing the current state of the whiteboard (shapes).
     *
     * @return A ShapeMemento object.
     */
    public ShapeMemento createMemento() {
        System.out.println("[WhiteBoard] Creating Memento...");
        
        return new ShapeMemento(new ArrayList<>(this.shapes)); 
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
        
        this.shapes = memento.getShapesState();
        
        this.selectedShapes.clear();
        this.activeShape = null;
        this.commandHistory.clear(); 
        System.out.println("[WhiteBoard] Memento restore complete. Shape count: " + this.shapes.size());
        repaint(); 
    }
}
