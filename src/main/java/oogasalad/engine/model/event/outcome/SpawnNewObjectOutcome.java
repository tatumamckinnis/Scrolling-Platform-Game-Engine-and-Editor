package oogasalad.engine.model.event.outcome;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
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
import oogasalad.fileparser.records.GameObjectData;

/**
 * Spawns object at specified location
 *
 * @author Gage Garcia
 */
public class SpawnNewObjectOutcome implements Outcome {

  private final GameExecutor executor;

  public SpawnNewObjectOutcome(GameExecutor gameExecutor) {
    this.executor = gameExecutor;
  }

  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    UUID uniqueId = UUID.randomUUID();
    int blueprintId = (int) Math.ceil(doubleParameters.get("blueprintId"));
    int x = (int) Math.ceil(doubleParameters.get("x"));
    int y = (int) Math.ceil(doubleParameters.get("y"));
    int layer = (int) Math.ceil(doubleParameters.get("layer"));
    String layerName = stringParameters.get("layer_name");
    GameObjectData data = new GameObjectData(blueprintId, uniqueId, x, y, layer, layerName);
    executor.addGameObject(data);
  }
  /**
   * int blueprintId,
   *     UUID uniqueId,
   *     int x,
   *     int y,
   *     int layer,
   *     String layerName
   */
}
