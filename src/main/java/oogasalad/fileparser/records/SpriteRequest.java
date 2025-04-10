package oogasalad.fileparser.records;

/**
 * A container class for passing multiple parameters required to locate and parse a sprite from an
 * XML file. This class encapsulates all the necessary details such as the game name, sprite group,
 * type, sprite name, and the sprite XML file name.
 *
 * <p>
 * It is used to simplify method signatures by grouping related parameters together.
 * </p>
 *
 * @param gameName   the name of the game the sprite belongs to
 * @param group      the group folder name in which the sprite is stored
 * @param type       the type folder name (e.g., characters, obstacles)
 * @param spriteName the name of the sprite to retrieve
 * @param spriteFile the name of the XML file containing the sprite's data
 * @author Billy McCune
 */
public record SpriteRequest(String gameName, String group, String type, String spriteName,
                            String spriteFile) {

}
