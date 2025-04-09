
package oogasalad.engine.controller.api;

import javafx.scene.input.KeyCode;

/**
 * interface to provide event system user input
 *
 * @author Gage Garcia
 */
public interface InputProvider {

  boolean isKeyPressed(KeyCode keyCode);
}
