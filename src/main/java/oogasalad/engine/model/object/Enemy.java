package oogasalad.engine.model.object;

import java.util.List;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.SpriteData;

/**
 * Represents an enemy in the game world. An Enemy is a specific type of {@link GameObject}
 * typically used to represent adversarial or opposing characters that the player interacts with or
 * avoids.
 *
 * @author Alana Zinkin
 */
public class Enemy extends GameObject {

  public Enemy(UUID uuid, int blueprintID, int hitBoxX, int hitBoxY, int hitBoxWidth, int hitBoxHeight, int layer, String name, String group, SpriteData spriteData,
      DynamicVariableCollection params, List<Event> events) {
    super(uuid, blueprintID, hitBoxX, hitBoxY, hitBoxWidth, hitBoxHeight, layer, name, group, spriteData, params, events );
  }

}

