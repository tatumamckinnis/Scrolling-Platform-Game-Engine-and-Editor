package oogasalad.engine.controller.gameobjectfactory;

import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.Enemy;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.SpriteData;

public class EnemyFactory implements GameObjectFactory {

  @Override
  public GameObject createGameObject(String uuid, String name, String group, SpriteData spriteData,
      DynamicVariableCollection params) {
    return new Enemy(uuid, name, group, spriteData, params);
  }
}
