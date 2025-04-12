package com.editor.commands;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.editor.gui.WhiteBoard;
import com.editor.gui.panel.ToolbarPanel;
import com.editor.memento.AppStateMemento;
import com.editor.memento.CompositeRegistryMemento;
import com.editor.memento.ShapeMemento;
import com.editor.memento.ToolbarMemento;
import com.editor.shapes.CompositeShapePrototypeRegistry;

/**
 * Command to save the application state (WhiteBoard and ToolbarPanel) to a
 * file.
 */
public class SaveStateCommand implements Command {

    // Need references to the components whose state we want to save
    private final WhiteBoard whiteBoard;
    private final ToolbarPanel toolbarPanel;
    private final CompositeShapePrototypeRegistry compositeRegistry;
    private final String filePath;

    // Keep track of the saved state in case we need undo (though unlikely for save)
    private AppStateMemento savedState = null;

    public SaveStateCommand(WhiteBoard whiteBoard, ToolbarPanel toolbarPanel,
            CompositeShapePrototypeRegistry compositeRegistry, String filePath) {
        this.whiteBoard = whiteBoard;
        this.toolbarPanel = toolbarPanel;
        this.compositeRegistry = compositeRegistry;
        this.filePath = filePath;
    }

    @Override
    public void execute() {
        System.out.println("[STATE DEBUG] SaveStateCommand.execute() - START");
        System.out.println("[STATE DEBUG] Saving application state to: " + filePath);
        try {
            // 1. Create mementos from the components
            System.out.println("[STATE DEBUG] Creating WhiteBoard memento...");
            ShapeMemento whiteboardMemento = whiteBoard.createMemento();

            System.out.println("[STATE DEBUG] Creating ToolbarPanel memento...");
            ToolbarMemento toolbarMemento = toolbarPanel.createMemento();

            System.out.println("[STATE DEBUG] Creating CompositeRegistry memento...");
            CompositeRegistryMemento compositeRegistryMemento = new CompositeRegistryMemento(compositeRegistry);

            System.out.println("[STATE DEBUG] Creating AppStateMemento with all component mementos...");
            AppStateMemento appState = new AppStateMemento(whiteboardMemento, toolbarMemento, compositeRegistryMemento);
            this.savedState = appState; // Store for potential undo

            // 2. Serialize the AppStateMemento to the file
            try (FileOutputStream fileOut = new FileOutputStream(filePath);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                System.out.println("[STATE DEBUG] Writing AppStateMemento to file: " + filePath);
                out.writeObject(appState);
                System.out.println("[STATE DEBUG] Application state successfully saved to file.");
            }

            System.out.println("[STATE DEBUG] SaveStateCommand.execute() - END");

        } catch (IOException i) {
            System.err.println("[SaveStateCommand] Error saving state: " + i.getMessage());
            i.printStackTrace();
            // Optionally, rethrow or handle more gracefully (e.g., show error dialog)
        } catch (Exception e) {
            System.err.println("[SaveStateCommand] Unexpected error during save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void undo() {
        // Undoing a save operation doesn't typically make sense in this context.
        // We could potentially delete the saved file, but that might be unexpected.
        // For now, we'll just log it.
        System.out.println("[SaveStateCommand] Undo called - typically not implemented for save.");
        // If we wanted to undo, we'd need to restore the state *before* this save,
        // which requires a more complex Memento/Command setup (Caretaker pattern).
    }
}
