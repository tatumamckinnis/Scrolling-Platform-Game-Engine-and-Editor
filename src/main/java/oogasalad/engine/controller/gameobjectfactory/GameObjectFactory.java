package oogasalad.engine.controller.gameobjectfactory;

import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.SpriteData;

/**
 * GameObjectFactory interface is tasked with creating new game objects based on the type of object
 * specified in the configuration file
 */
public interface GameObjectFactory {


  /**
   * creates a new game object with specified parameters based on the type of game object
   *
   * @param uuid       unique id for a given object
   * @param name       display name of the object
   * @param group      group the object belongs to (block, background etc.)
   * @param spriteData Sprite information for the object
   * @param params     parameters needed for event handling
   * @return a new GameObject instance
   */
  public GameObject createGameObject(String uuid, String name, String group, SpriteData spriteData,
      DynamicVariableCollection params);
}
