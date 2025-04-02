package oogasalad.engine.model.object;

import java.util.List;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.SpriteData;

/**
 * The player class represents a player object, which is typically controlled using key inputs (but not required to be)
 * Player's are typically how the user represents their own actions within the game
 *
 * @author Alana Zinkin
 */
public class DefaultGameObject extends GameObject {


  public DefaultGameObject(UUID uuid, int blueprintID, String type, int hitBoxX, int hitBoxY, int hitBoxWidth, int hitBoxHeight, int layer, String name, String group, SpriteData spriteData,
      DynamicVariableCollection params, List<Event> events) {
    super(uuid, blueprintID, type, hitBoxX, hitBoxY, hitBoxWidth, hitBoxHeight, layer, name, group,
        spriteData, params, events);
  }

}
