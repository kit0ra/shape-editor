package com.editor.gui.button.factory;

import java.awt.Image;

import com.editor.commands.CommandHistory;
import com.editor.gui.WhiteBoard;
import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.ShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ShapeDrawingButtonDecorator;
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.mediator.DragMediator;
import com.editor.shapes.Shape;
import com.editor.shapes.ShapePrototypeRegistry;
import com.editor.utils.ImageLoader;

/**
 * Factory for creating shape buttons that can be used to create shapes on the
 * whiteboard.
 * Implements the ButtonFactory interface.
 */
public class ShapeButtonFactory implements ButtonFactory {
    private final WhiteBoard whiteBoard;
    private final ShapePrototypeRegistry registry;
    @SuppressWarnings("unused")
    private final CommandHistory commandHistory;
    private DragMediator dragMediator;

    
    private static final int DEFAULT_HEIGHT = 40;
    private static final int DEFAULT_ICON_SIZE = 24;
    private static final int DEFAULT_PADDING = 8;

    /**
     * Creates a new ShapeButtonFactory.
     *
     * @param whiteBoard     The whiteboard to create shapes on
     * @param registry       The shape prototype registry
     * @param commandHistory The command history for undo/redo
     */
    public ShapeButtonFactory(WhiteBoard whiteBoard, ShapePrototypeRegistry registry, CommandHistory commandHistory) {
        this.whiteBoard = whiteBoard;
        this.registry = registry;
        this.commandHistory = commandHistory;
    }

    /**
     * Sets the drag mediator for this factory.
     *
     * @param mediator The drag mediator
     */
    public void setDragMediator(DragMediator mediator) {
        this.dragMediator = mediator;
    }

    @Override
    public IButton createButton(int x, int y, String iconPath, String tooltipText, String typeKey) {
        
        int width = DEFAULT_ICON_SIZE + DEFAULT_PADDING * 2;
        int height = DEFAULT_HEIGHT;

        
        IButton button = new CustomButton(x, y, width, height, "");

        
        Shape shapePrototype = null;
        if (registry != null && registry.hasPrototype(typeKey)) {
            try {
                shapePrototype = registry.createShape(typeKey, 0, 0);
                if (shapePrototype != null) {
                    button = new ShapeDrawingButtonDecorator(button, shapePrototype, 0.35, 6);
                }
            } catch (Exception e) {
                System.err.println("[ShapeButtonFactory] Error creating shape from registry: " + e.getMessage());
            }
        }

        
        if (shapePrototype == null) {
            Image icon = ImageLoader.loadImage(iconPath);
            if (icon != null) {
                button = new ImageDecorator(
                        button,
                        icon,
                        DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE,
                        DEFAULT_PADDING,
                        ImageDecorator.ImageMode.ICON_ONLY);
            }
        }

        
        button = new TooltipDecorator(button, tooltipText);

        
        ShapeCreationButtonDecorator shapeButton = new ShapeCreationButtonDecorator(
                button, whiteBoard, registry, typeKey);

        
        if (dragMediator != null) {
            shapeButton.setDragMediator(dragMediator);
        }

        return shapeButton;
    }
}
