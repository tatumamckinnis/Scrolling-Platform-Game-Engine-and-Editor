package oogasalad.engine.model.object;

import oogasalad.fileparser.records.SpriteData;

/**
 * The player class represents a player object, which is typically controlled using key inputs (but not required to be)
 * Player's are typically how the user represents their own actions within the game
 *
 * @author Alana Zinkin
 */
public class Player extends GameObject {


  /**
   * default constructor for creating a player
   *
   * @param uuid       unique id for the object
   * @param name       display name of the player
   * @param group      that the object belongs to
   * @param spriteData the visual information for the display
   * @param params     dynamic variables associated with the entity's behavior
   */
  public Player(String uuid, String name, String group,
      SpriteData spriteData, DynamicVariableCollection params) {
    super(uuid, name, group, spriteData, params);
  }
}
