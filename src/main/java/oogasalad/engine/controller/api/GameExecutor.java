
package oogasalad.engine.controller.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.mapObject;
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
 * Interface that outcomes use to update game state
 *
 * @author Gage Garcia
 */
public interface GameExecutor {

  /**
   * Remove specified game object from the level
   *
   * @param gameObject to remove
   */
  void destroyGameObject(GameObject gameObject);

  /**
   * Adds game object to the level
   *
   */
  void addGameObject(GameObjectData gameObjectData);

  /**
   * returns level dimensions through a map object
   *
   * @return map object with width, height
   */
  mapObject getMapObject();

  /**
   * get a game object using id
   *
   * @param id String of uuid
   * @return the game object model
   */
  GameObject getGameObjectByUUID(String id);

  /**
   * Ends the game using game manager methods
   */
  void endGame();

  /**
   * Restarts using current level
   */
  void restartLevel()
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException;

  /**
   * Selects a level using a specified file path
   */
  void selectLevel(String filePath)
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException;
}
