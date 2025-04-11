package oogasalad.engine.model.event.outcome;

import java.util.Map;
import oogasalad.engine.model.object.GameObject;

/**
 * Outcome that applies gravity to the game object
 *
 * @author Gage Garcia
 */
public class JumpOutcome implements Outcome {

  @Override
  public void execute(GameObject gameObject,
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters) {
    double dy = gameObject.getDoubleParams().getOrDefault("JumpAmount", 60.0);
    if (gameObject.isGrounded()) {
      gameObject.setYVelocity(-dy);
      gameObject.setGrounded(false); // Mark object as airborne
    }
  }
}
