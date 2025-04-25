
package oogasalad.engine.controller.api;

import java.awt.Point;
import javafx.scene.input.KeyCode;

/**
 * interface to provide event system user input
 *
 * @author Gage Garcia
 */
public interface InputProvider {

  /**
   * checks if the key was pressed
   *
   * @param keyCode the computer key
   * @return true or false depending on if the key was pressed
   */
  boolean isKeyPressed(KeyCode keyCode);

  /**
   * checks if the key was just released since last check
   */
  boolean isKeyReleased(KeyCode keyCode);

  /**
   * returns Point representing mouse position
   */
  Point getMousePosition();

  /**
   * call once per frame/tick to reset the released‐state
   */
  void clearReleased();
}
