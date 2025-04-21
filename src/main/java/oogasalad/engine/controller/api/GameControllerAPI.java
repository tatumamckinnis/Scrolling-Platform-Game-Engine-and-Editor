package oogasalad.engine.controller.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.camera.Camera;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.LevelData;

/**
 * Interface for interacting with GameControllers
 *
 * @author Gage Garcia
 */

public interface GameControllerAPI {

  /**
   * @return list of records of game objects only containing relevant information for the view
   */
  List<ImmutableGameObject> getImmutableObjects();

  /**
   * @return list of immutable player game objects
   */
  List<ImmutableGameObject> getImmutablePlayers();

  /**
   * returns view object data using id
   *
   * @param uuid string version of uuid
   * @return view object model data
   */
  ImmutableGameObject getViewObjectByUUID(String uuid);


  /**
   * Advances the game state by one "tick" or step, typically by: 1) Calling each phase controller
   * (input, physics, collision, etc.) 2) Resolving any post-update tasks (e.g. removing destroyed
   * objects) 3) Tracking which objects have changed for rendering
   */
  void updateGameState()
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException;

  /**
   * Loads a new level or scene, potentially calling file loaders to retrieve data and
   * re-initializing internal structures (objects, controllers, etc.).
   */
  void setLevelData(LevelData data)
      throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

  /**
   * @return the camera object which translates the game scene
   */
  Camera getCamera();

}