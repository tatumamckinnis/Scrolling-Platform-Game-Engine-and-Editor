
package oogasalad.engine.model.event.outcome;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.object.GameObject;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Outcome that triggers a restart level
 *
 * @author Gage Garcia
 */
public class RestartLevelOutcome implements Outcome {

  private final GameExecutor executor;
  private static final Logger LOG = LogManager.getLogger();
  /**
   * Outcome that the player has lost the game
   * @param executor allows for access to the game manager
   */
  public RestartLevelOutcome(GameExecutor executor) {
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
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    executor.restartLevel();
    LOG.info("restarting level");
  }
}

