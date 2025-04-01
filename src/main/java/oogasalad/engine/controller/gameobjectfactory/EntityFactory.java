package oogasalad.engine.controller.gameobjectfactory;

import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.SpriteData;

public class EntityFactory implements GameObjectFactory {

  @Override
  public GameObject createGameObject(String uuid, String name, String group, SpriteData spriteData,
      DynamicVariableCollection params) {
    return new Entity(uuid, name, group, spriteData, params);
  }
}
