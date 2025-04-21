package oogasalad.engine.model.event.condition;

import java.util.Map;
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
  private final boolean  wantPressed;

  /**
   * Requires an input provided and specified keycode
   *
   * @param inputProvider interface that allows access to currently pressed inputs
   * @param keyCode       key to check
   */
  public InputCondition(InputProvider inputProvider, KeyCode keyCode, boolean wantPressed) {
    this.inputProvider = inputProvider;
    this.keyCode = keyCode;
    this.wantPressed  = wantPressed;
  }

  @Override
  public boolean isMet(GameObject gameObject, Map<String, String> stringParams, Map<String, Double> doubleParams) {
    return wantPressed
        ? inputProvider.isKeyPressed(keyCode)
        : inputProvider.isKeyReleased(keyCode);
  }

}
