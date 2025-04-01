package oogasalad.engine.model.object;

import oogasalad.fileparser.records.SpriteData;

/**
 * Represents an enemy in the game world. An Enemy is a specific type of {@link GameObject}
 * typically used to represent adversarial or opposing characters that the player interacts with or
 * avoids.
 *
 * @author Alana Zinkin
 */
public class Enemy extends GameObject {

  /**
   * Constructs a new Enemy with the specified properties.
   *
   * @param uuid       unique identifier for this enemy
   * @param name       the display name of the enemy
   * @param group      the group or category the enemy belongs to
   * @param spriteData visual data used to render the enemy
   * @param params     dynamic variables associated with the enemy's behavior
   */
  public Enemy(String uuid, String name, String group,
      SpriteData spriteData, DynamicVariableCollection params) {
    super(uuid, name, group, spriteData, params);
  }
}

