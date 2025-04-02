/**
 * Stores user input at each step
 * Provides method for condition checker to evaluate input conditions
 * @author Gage Garcia
 */
package oogasalad.engine.controller;

import java.util.ArrayList;
import java.util.List;

public class InputHandler {
    private static List<Character> currentKeysPressed = new ArrayList<>();

    /**
     *
     * @return whether a specified character is pressed
     */
    public boolean isKeyPressed(Character character) {
        if (currentKeysPressed == null) {
            return false;
        }
        return currentKeysPressed.contains(character);
    }

    /**
     * update list of
     * @param keysPressed
     */
    public void setKeysPressed(List<Character> keysPressed) {
        this.currentKeysPressed = keysPressed;
    }

    /**
     * clears user input
     */
    public void clearInputs() {
        currentKeysPressed = null;
    }
}
