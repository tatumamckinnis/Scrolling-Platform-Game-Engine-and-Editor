package oogasalad.engine.event.outcome;

import java.util.Map;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.object.GameObject;

/**
 * movement pattern that moves object back and forth horizontally
 *
 * @author Gage Garcia
 */
public class PatrolOutcome implements Outcome {

  private final GameExecutor gameExecutor;

  /**
   * Requires a game executor
   *
   * @param gameExecutor interfaces that allows outcomes to change game state
   */
  public PatrolOutcome(GameExecutor gameExecutor) {
    this.gameExecutor = gameExecutor;
  }

  @Override
  public void execute(GameObject gameObject,
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters) {
    double dx = gameObject.getDoubleParams().getOrDefault("MovementAmount", 4.0);
    if (gameObject.getXPosition() < 0) {
      gameObject.setXVelocity(dx);
    } else if (gameObject.getXPosition() + gameObject.getHitBoxWidth()
        >= gameExecutor.getMapObject().maxX()) {
      gameObject.setXVelocity(-dx);
    } else if (gameObject.getXVelocity() == 0) {
      gameObject.setXVelocity(-dx);
    }
  }
}
