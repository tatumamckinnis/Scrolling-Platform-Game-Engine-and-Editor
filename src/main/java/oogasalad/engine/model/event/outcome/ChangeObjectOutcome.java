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
 * Spawns specified object at current location and removes current object
 *
 * @author Gage Garcia
 */
public class ChangeObjectOutcome implements Outcome{
  private final GameExecutor gameExecutor;

  public ChangeObjectOutcome(GameExecutor executor) {
    this.gameExecutor = executor;
  }
  @Override
  public void execute(GameObject gameObject, Map<String, String> stringParameters,
      Map<String, Double> doubleParameters)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {
    gameExecutor.destroyGameObject(gameObject); //remove current object
    UUID uniqueId = UUID.fromString(gameObject.getUUID());
    int blueprintId = (int) Math.ceil(doubleParameters.get("blueprintId"));
    int x = gameObject.getXPosition();
    int y = gameObject.getYPosition();
    int layer = (int) Math.ceil(doubleParameters.get("layer"));
    String layerName = stringParameters.get("layer_name");
    GameObjectData data = new GameObjectData(blueprintId, uniqueId, x, y, layer, layerName);
    gameExecutor.addGameObject(data); //add new one

  }
}
