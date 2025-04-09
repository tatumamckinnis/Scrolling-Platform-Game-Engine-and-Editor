package oogasalad.engine.controller.api;

import java.util.List;
import oogasalad.engine.model.object.ViewObject;
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
  List<ViewObject> getImmutableObjects();

  /**
   * returns view object data using id
   *
   * @param uuid string version of uuid
   * @return view object model data
   */
  ViewObject getViewObjectByUUID(String uuid);


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