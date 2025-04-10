
package oogasalad.engine.controller.api;

import javafx.scene.input.KeyCode;

/**
 * interface to provide event system user input
 *
 * @author Gage Garcia
 */
public interface InputProvider {

  /**
   * checks if the key was pressed
   * @param keyCode the computer key
   * @return true or false depending on if the key was pressed
   */
  boolean isKeyPressed(KeyCode keyCode);
}
