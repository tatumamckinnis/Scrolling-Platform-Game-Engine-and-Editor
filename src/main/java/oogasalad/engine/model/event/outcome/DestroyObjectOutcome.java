package oogasalad.engine.model.event.outcome;

import java.util.Map;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.object.GameObject;

/**
 * removes game object from level
 *
 * @author Gage Garcia
 */
public class DestroyObjectOutcome implements Outcome {

  private final GameExecutor gameExecutor;

  /**
   * Requires a game executor interface
   *
   * @param gameExecutor interface that allows outcomes to affect game state
   */
  public DestroyObjectOutcome(GameExecutor gameExecutor) {
    this.gameExecutor = gameExecutor;
  }

  @Override
  public void execute(GameObject obj,
      Map<String, String> stringProperties,
      Map<String, Double> doubleParameters) {
    gameExecutor.destroyGameObject(obj);
  }
}
