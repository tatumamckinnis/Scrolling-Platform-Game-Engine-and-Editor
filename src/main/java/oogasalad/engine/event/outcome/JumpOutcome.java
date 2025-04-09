package oogasalad.engine.event.outcome;

import oogasalad.engine.model.object.GameObject;

/**
 * Outcome that applies gravity to the game object
 *
 * @author Gage Garcia
 */
public class JumpOutcome implements Outcome {

  @Override
  public void execute(GameObject gameObject) {
    double dy = gameObject.getDoubleParams().getOrDefault("JumpAmount", 60.0);
    if (gameObject.isGrounded()) {
      gameObject.setYVelocity(-dy);
      gameObject.setGrounded(false); // Mark object as airborne
    }
  }
}
