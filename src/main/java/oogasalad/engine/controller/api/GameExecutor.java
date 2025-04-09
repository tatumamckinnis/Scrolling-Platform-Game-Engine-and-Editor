/**
 * Interface that outcomes use to update game state
 *
 * @author Gage Garcia
 */
package oogasalad.engine.controller.api;

import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.mapObject;

public interface GameExecutor {

  /**
   * Remove specified game object from the level
   *
   * @param gameObject to remove
   */
  void destroyGameObject(GameObject gameObject);

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
}
