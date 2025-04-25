package oogasalad.engine.model.event.outcome;

import java.util.Map;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.object.GameObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents an outcome that causes the game to end in a win.
 *
 * @author Alana Zinkin
 */
public class WinGameOutcome implements Outcome {

  private final GameExecutor executor;

  /**
   * Logger used to record loss events
   */
  private static final Logger LOG = LogManager.getLogger();

  /**
   * Outcome that the player has lost the game
   *
   * @param executor allows for access to the game manager
   */
  public WinGameOutcome(GameExecutor executor) {
    this.executor = executor;
  }

  /**
   * Executes the outcome, logging that the player has lost the game.
   *
   * @param gameObject the game object that triggered the loss (e.g., the player)
   */
  @Override
  public void execute(GameObject gameObject,
      Map<String, String> stringParameters,
      Map<String, Double> doubleParameters) {
    executor.endGame(true);
    LOG.info("You win the game!");
  }

}
