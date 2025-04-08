package oogasalad.engine.controller.api;

import java.util.Map;

import oogasalad.engine.controller.ViewObject;
import oogasalad.engine.event.CollisionHandler;
import oogasalad.engine.model.object.GameObject;

import java.util.List;
import oogasalad.engine.model.object.mapObject;
import oogasalad.fileparser.records.LevelData;

/**
 * Interface for interacting with GameControllers
 *
 * @author Gage Garcia
 */

public interface GameControllerAPI {

  /**
   * Returns a list of game objects that have been updated or changed since the last call (e.g.,
   * positions, states, or visual representation). Often used by the rendering system to determine
   * which objects to draw.
   *
   * @return a List of changed/updated GameObjects
   */
  List<GameObject> getObjects();

  /**
   * @return list of records of game objects only containing relevant information for the view
   */
  List<ViewObject> getImmutableObjects();

  /**
   * returns data associated with current level's dimensions
   * @return mapObject model data
   */
  mapObject getMapObject();

  /**
   * removes game object from the level
   * @param gameObject to remove
   */
  void destroyGameObject(GameObject gameObject);

  /**
   * returns view object data using id
   * @param uuid string version of uuid
   * @return view object model data
   */
  ViewObject getViewObjectByUUID(String uuid);

  /**
   * returns backend object data using id
   * @param uuid string version of uuid
   * @return game object model data
   */
  GameObject getGameObjectByUUID(String uuid);
  /**
   * Advances the game state by one "tick" or step, typically by: 1) Calling each phase controller
   * (input, physics, collision, etc.) 2) Resolving any post-update tasks (e.g. removing destroyed
   * objects) 3) Tracking which objects have changed for rendering
   */
  void updateGameState();

  /**
   * Loads a new level or scene, potentially calling file loaders to retrieve data and
   * re-initializing internal structures (objects, controllers, etc.).
   */
  void setLevelData(LevelData data);

  /**
   * returns collision handler class tied to game controller's game objects
   * @return CollisionHandler class
   */
  CollisionHandler getCollisionHandler();
}