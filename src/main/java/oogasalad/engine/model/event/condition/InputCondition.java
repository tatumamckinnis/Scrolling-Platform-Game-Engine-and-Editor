package oogasalad.engine.model.event.condition;

import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.model.object.GameObject;

/**
 * Condition that is met based on if a certain input was pressed
 *
 * @author Billy McCune
 */
public class InputCondition implements Condition {

  private final InputProvider inputProvider;
  private final KeyCode keyCode;

  /**
   * Requires an input provided and specified keycode
   *
   * @param inputProvider interface that allows access to currently pressed inputs
   * @param keyCode       key to check
   */
  public InputCondition(InputProvider inputProvider, KeyCode keyCode) {
    this.inputProvider = inputProvider;
    this.keyCode = keyCode;
  }

  @Override
  public boolean isMet(GameObject gameObject) {
    return inputProvider.isKeyPressed(keyCode);
  }
}
