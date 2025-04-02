package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.GameObject;

import java.util.List;
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
  List<GameObjectRecord> getImmutableObjects();

  /**
   * Returns a map of all game objects currently loaded in the engine.
   *
   * <p>The map uses each object's unique UUID as the key and the corresponding
   * {@link GameObject} as the value. This allows for efficient lookup and manipulation of
   * individual game objects by their identifier.
   *
   * @return a map of UUID strings to their associated {@link GameObject} instances
   */
  Map<String, GameObject> getGameObjectMap();

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
}