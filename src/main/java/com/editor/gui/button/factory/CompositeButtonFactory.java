package com.editor.gui.button.factory;

import java.awt.Image;

import com.editor.commands.CommandHistory;
import com.editor.gui.WhiteBoard;
import com.editor.gui.button.CustomButton;
import com.editor.gui.button.IButton;
import com.editor.gui.button.decorators.DraggableCompositeShapeCreationButtonDecorator;
import com.editor.gui.button.decorators.ImageDecorator;
import com.editor.gui.button.decorators.ShapeDrawingButtonDecorator;
import com.editor.gui.button.decorators.TooltipDecorator;
import com.editor.mediator.DragMediator;
import com.editor.shapes.CompositeShapePrototypeRegistry;
import com.editor.shapes.ShapeGroup;
import com.editor.utils.ImageLoader;

/**
 * Factory for creating composite shape buttons that can be used to create
 * groups of shapes on the whiteboard.
 * Implements the ButtonFactory interface.
 */
public class CompositeButtonFactory implements ButtonFactory {
    private final WhiteBoard whiteBoard;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final CommandHistory commandHistory;
    private DragMediator dragMediator;

    // Default sizing constants
    private static final int DEFAULT_HEIGHT = 40;
    private static final int DEFAULT_ICON_SIZE = 24;
    private static final int DEFAULT_PADDING = 8;

    /**
     * Creates a new CompositeButtonFactory.
     *
     * @param whiteBoard        The whiteboard to create shapes on
     * @param compositeRegistry The composite shape prototype registry
     * @param commandHistory    The command history for undo/redo
     */
    public CompositeButtonFactory(WhiteBoard whiteBoard,
            CompositeShapePrototypeRegistry compositeRegistry,
            CommandHistory commandHistory) {
        this.whiteBoard = whiteBoard;
        this.compositeRegistry = compositeRegistry;
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
        // Calculate button dimensions
        int width = DEFAULT_ICON_SIZE + DEFAULT_PADDING * 2;
        int height = DEFAULT_HEIGHT;

        // Create base button
        IButton button = new CustomButton(x, y, width, height, "");

        // Add shape drawing if available
        ShapeGroup groupPrototype = null;
        if (compositeRegistry != null && compositeRegistry.hasPrototype(typeKey)) {
            groupPrototype = compositeRegistry.getPrototype(typeKey);
            if (groupPrototype != null) {
                button = new ShapeDrawingButtonDecorator(button, groupPrototype, 0.35, 6);
            }
        }

        // If no shape drawing, add image if available
        if (groupPrototype == null) {
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

        // Add tooltip
        button = new TooltipDecorator(button, tooltipText);

        // Add composite shape creation functionality
        DraggableCompositeShapeCreationButtonDecorator compositeButton = new DraggableCompositeShapeCreationButtonDecorator(
                button, whiteBoard, compositeRegistry, typeKey);

        // Set drag mediator if available
        if (dragMediator != null) {
            compositeButton.setDragMediator(dragMediator);
        }

        return compositeButton;
    }
}
