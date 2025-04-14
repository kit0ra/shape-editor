package com.editor.memento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Memento class to store the state of the ToolbarPanel.
 * Specifically, it stores the keys of the prototypes used to create
 * the dynamic buttons currently on the toolbar.
 */
public class ToolbarMemento implements Serializable {

    private static final long serialVersionUID = 1L; 

    
    private final List<String> buttonPrototypeKeys;

    /**
     * Constructs a ToolbarMemento with the current state.
     *
     * @param buttonPrototypeKeys A list of String keys representing the prototypes
     *                            of the buttons currently on the toolbar.
     */
    public ToolbarMemento(List<String> buttonPrototypeKeys) {
        
        this.buttonPrototypeKeys = new ArrayList<>(buttonPrototypeKeys);

        
        System.out.println("[STATE DEBUG] ToolbarMemento constructor - START");
        System.out.println("[STATE DEBUG] Creating memento with " + this.buttonPrototypeKeys.size() + " keys");
        System.out.println("[STATE DEBUG] Keys stored in memento: " + this.buttonPrototypeKeys);

        
        for (int i = 0; i < this.buttonPrototypeKeys.size(); i++) {
            System.out.println("[STATE DEBUG] Memento key " + i + ": " + this.buttonPrototypeKeys.get(i));
        }
        System.out.println("[STATE DEBUG] ToolbarMemento constructor - END");
    }

    /**
     * Gets the saved list of button prototype keys.
     *
     * @return An immutable list of String keys.
     */
    public List<String> getButtonPrototypeKeys() {
        
        System.out.println("[STATE DEBUG] ToolbarMemento.getButtonPrototypeKeys() - START");
        System.out.println("[STATE DEBUG] Returning " + buttonPrototypeKeys.size() + " keys from memento");
        System.out.println("[STATE DEBUG] Keys being returned: " + buttonPrototypeKeys);

        
        for (int i = 0; i < buttonPrototypeKeys.size(); i++) {
            System.out.println("[STATE DEBUG] Returning key " + i + ": " + buttonPrototypeKeys.get(i));
        }
        System.out.println("[STATE DEBUG] ToolbarMemento.getButtonPrototypeKeys() - END");

        
        return new ArrayList<>(this.buttonPrototypeKeys);
    }
}
