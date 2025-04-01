package oogasalad.engine.controller.gameobjectfactory;

import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.Player;
import oogasalad.fileparser.records.SpriteData;

public class PlayerFactory implements GameObjectFactory {

  @Override
  public GameObject createGameObject(String uuid, String name, String group, SpriteData spriteData,
      DynamicVariableCollection params) {
    return new Player(uuid, name, group, spriteData, params);
  }
}
