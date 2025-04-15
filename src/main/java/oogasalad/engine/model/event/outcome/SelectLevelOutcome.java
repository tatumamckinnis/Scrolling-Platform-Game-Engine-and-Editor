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

/**
 * Outcome that changes level to a parameter defined new level
 *
 * @author Gage Garcia
 */
public class SelectLevelOutcome implements Outcome {

  private final GameExecutor executor;

  public SelectLevelOutcome(GameExecutor executor) {
    this.executor = executor;
  }

  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {

    String levelPath = stringParameters.get("levelPath");
    executor.selectLevel(levelPath);
  }
}
