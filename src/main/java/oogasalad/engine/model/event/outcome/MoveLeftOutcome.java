package oogasalad.engine.model.event.outcome;

import java.util.Map;
import oogasalad.engine.model.object.GameObject;

/**
 * An {@link Outcome} that moves a {@link GameObject} to the left when executed.
 *
 * <p>This outcome retrieves the "MoveLeftAmount" parameter from the object's dynamic
 * parameters and shifts its x-position to the left by that amount. If the parameter
 * is not defined, it defaults to 4.0 pixels.
 *
 * <p>This class is typically triggered by an event such as a user input or collision.
 */
public class MoveLeftOutcome implements Outcome {

  /**
   * Executes the leftward movement on the given {@link GameObject}.
   *
   * @param gameObject the game object to move
   */
  @Override
  public void execute(GameObject gameObject,
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters) {
    double dx = doubleParameters.getOrDefault("amount",4.0);
    gameObject.setXPosition((int) (gameObject.getXPosition() - dx));
  }
}

