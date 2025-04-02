package oogasalad.engine.model.object;

import java.util.List;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.SpriteData;

/**
 * Represents a dynamic entity in the game world.
 * An Entity is a specific type of {@link GameObject} that can have dynamic behavior
 * and interact with other components in the game engine.
 *
 * @author Alana Zinkin
 */
public class Entity extends GameObject {

  public Entity(UUID uuid, int blueprintID, int hitBoxX, int hitBoxY, int hitBoxWidth, int hitBoxHeight, int layer, String name, String group, SpriteData spriteData,
      DynamicVariableCollection params, List<Event> events) {
    super(uuid, blueprintID, hitBoxX, hitBoxY, hitBoxWidth, hitBoxHeight, layer, name, group, spriteData, params, events );
  }
}

