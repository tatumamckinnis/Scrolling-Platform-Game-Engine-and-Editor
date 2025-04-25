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
 * Spawns object with respect to the object containing the event
 *
 * @author Gage Garcia
 */
public class SpawnOnObjectOutcome implements Outcome {

  private final GameExecutor gameExecutor;

  public SpawnOnObjectOutcome(GameExecutor gameExecutor) {
    this.gameExecutor = gameExecutor;
  }

  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {

    UUID uniqueId = UUID.randomUUID();
    int blueprintId = (int) Math.ceil(doubleParameters.get("blueprintId"));
    int dx = (int) Math.ceil(doubleParameters.get("offset_x"));
    int dy = (int) Math.ceil(doubleParameters.get("offset_y"));
    int x = gameObject.getXPosition() + dx;
    int y = gameObject.getYPosition() + dy;
    int layer = (int) Math.ceil(doubleParameters.get("layer"));
    String layerName = stringParameters.get("layer_name");
    GameObjectData data = new GameObjectData(blueprintId, uniqueId, x, y, layer, layerName);
    gameExecutor.addGameObject(data);

  }
}
