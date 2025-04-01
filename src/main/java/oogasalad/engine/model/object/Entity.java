package oogasalad.engine.model.object;

import oogasalad.fileparser.records.SpriteData;

/**
 * Represents a dynamic entity in the game world.
 * An Entity is a specific type of {@link GameObject} that can have dynamic behavior
 * and interact with other components in the game engine.
 *
 * @author Alana Zinkin
 */
public class Entity extends GameObject {

  /**
   * Constructs a new Entity with the specified properties.
   *
   * @param uuid unique identifier for this entity
   * @param name the display name of the entity
   * @param group the group or category the entity belongs to
   * @param spriteData visual data used to render the entity
   * @param params dynamic variables associated with the entity's behavior
   */
  public Entity(String uuid, String name, String group,
      SpriteData spriteData, DynamicVariableCollection params) {
    super(uuid, name, group, spriteData, params);
  }
}

