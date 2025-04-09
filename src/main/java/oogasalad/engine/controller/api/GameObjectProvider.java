/**
 * Interface that provides access to all the game objects in a current level
 *
 * @author Gage Garcia
 */
package oogasalad.engine.controller.api;

import java.util.List;
import oogasalad.engine.model.object.GameObject;

public interface GameObjectProvider {

  /**
   * Return list of all game objects in backend scene
   *
   * @return
   */
  List<GameObject> getGameObjects();
}
