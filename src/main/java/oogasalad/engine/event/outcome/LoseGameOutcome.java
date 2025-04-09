/**
 * Outcome that triggers a loss condition in the game.
 *
 * <p>When executed, this outcome logs that the player has lost the game.
 * This can later be expanded to interface with game state or UI logic to end the game session or
 * transition to a game over screen.
 *
 * <p>This outcome is typically triggered by an in-game event such as
 * the player colliding with a hazard or running out of time.
 *
 * @author Gage Garcia
 */
package oogasalad.engine.event.outcome;

import java.util.logging.Logger;
import oogasalad.engine.model.object.GameObject;

/**
 * Represents an outcome that causes the game to end in a loss.
 *
 * @author Alana Zinkin
 */
public class LoseGameOutcome implements Outcome {

  /** Logger used to record loss events */
  Logger LOG = Logger.getLogger(LoseGameOutcome.class.getName());

  /**
   * Executes the outcome, logging that the player has lost the game.
   *
   * @param gameObject the game object that triggered the loss (e.g., the player)
   */
  @Override
  public void execute(GameObject gameObject) {
    LOG.info("You lose the game");
  }
}

