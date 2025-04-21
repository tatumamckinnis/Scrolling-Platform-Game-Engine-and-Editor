package oogasalad.engine.model.event.condition;

import java.util.Locale;
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
  private final boolean  wantPressed;
  private static final Map<String,KeyCode> KEY_ALIASES = Map.of(
      "left_arrow", KeyCode.LEFT,
      "right_arrow", KeyCode.RIGHT,
      "spacebar", KeyCode.SPACE
  );

  /**
   * Requires an input provided and specified keycode
   *
   * @param inputProvider interface that allows access to currently pressed inputs
   */
  public InputCondition(InputProvider inputProvider, boolean wantPressed) {
    this.inputProvider = inputProvider;
    this.wantPressed  = wantPressed;
  }

  @Override
  public boolean isMet(GameObject gameObject, Map<String, String> stringParams, Map<String, Double> doubleParams) {
    String key = stringParams.get("key");
    KeyCode keyCode;
    if (KEY_ALIASES.containsKey(key)) {
      keyCode = KEY_ALIASES.get(key);
    } else {
      String normalized = key.toUpperCase(Locale.ROOT);  // → "W", "SPACE", "LEFT", etc.
      keyCode = KeyCode.valueOf(normalized);
    }

    return wantPressed
        ? inputProvider.isKeyPressed(keyCode)
        : inputProvider.isKeyReleased(keyCode);
  }

}
