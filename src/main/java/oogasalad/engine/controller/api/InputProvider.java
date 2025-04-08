/**
 * interface to provide event system user input
 * @author Gage Garcia
 */
package oogasalad.engine.controller.api;

import javafx.scene.input.KeyCode;

public interface InputProvider {
    boolean isKeyPressed(KeyCode keyCode);
}
