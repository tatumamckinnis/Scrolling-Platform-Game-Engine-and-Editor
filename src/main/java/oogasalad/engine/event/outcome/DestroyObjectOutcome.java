/**
 * removes game object from level
 */
package oogasalad.engine.event.outcome;

import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.object.GameObject;

public class DestroyObjectOutcome implements Outcome {

  private final GameExecutor gameExecutor;

  public DestroyObjectOutcome(GameExecutor gameExecutor) {
    this.gameExecutor = gameExecutor;
  }

  @Override
  public void execute(GameObject obj) {
    gameExecutor.destroyGameObject(obj);
  }
}
