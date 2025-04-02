package oogasalad.engine.controller.gameobjectfactory;

import java.util.List;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.Player;
import oogasalad.fileparser.records.SpriteData;

public class PlayerFactory implements GameObjectFactory {

  @Override
  public GameObject createGameObject(UUID uuid, int blueprintID, int hitBoxX, int hitBoxY,
      int hitBoxWidth, int hitBoxHeight, int layer, String name, String group,
      SpriteData spriteData,
      DynamicVariableCollection params, List<Event> events) {
    return new Player(uuid, blueprintID, hitBoxX, hitBoxY, hitBoxWidth, hitBoxHeight, layer, name,
        group, spriteData, params, events);
  }
}
