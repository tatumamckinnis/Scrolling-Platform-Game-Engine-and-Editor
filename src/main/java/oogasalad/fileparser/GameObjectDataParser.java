package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.fileparser.records.GameObjectData;
import org.w3c.dom.Element;

/**
 * Responsible for parsing an XML element representing game objects into a list of
 * {@link GameObjectData} records.
 * <p>
 * This parser extracts blueprint IDs, coordinates, and UUIDs for each game object instance
 * described in the XML element. It is used to reconstruct level objects from structured XML data.
 * </p>
 *
 * @author Billy McCune
 */
public class GameObjectDataParser {

  /**
   * Parses a game object XML element and creates a list of {@link GameObjectData} records.
   * <p>
   * The method reads the blueprint ID, a list of UUIDs, and a coordinate string of the form
   * {@code "(x1,y1),(x2,y2),..."}. Each UUID is paired with one coordinate set to form a
   * {@code GameObjectData} entry.
   * </p>
   *
   * @param gameObjectElement the XML element representing the game object
   * @param z                 the z-index layer of the game object
   * @return a list of {@link GameObjectData} objects created from the element
   * @throws GameObjectParseException if the input data is malformed or parsing fails
   */
  public List<GameObjectData> getGameObjectData(Element gameObjectElement, int z)
      throws GameObjectParseException {
    try {
      int blueprintId = Integer.parseInt(gameObjectElement.getAttribute("id"));

      String uidAttr = gameObjectElement.getAttribute("uid");
      String[] uidArray = uidAttr.split(",");
      String coordinates = gameObjectElement.getAttribute("coordinates");
      List<GameObjectData> gameObjectDataList = new ArrayList<>();

      Pattern pattern = Pattern.compile("\\((-?\\d+),(-?\\d+)\\)");
      Matcher matcher = pattern.matcher(coordinates);

      int index = 0;
      while (matcher.find()) {
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));

        if (index < uidArray.length) {
          UUID uuid = UUID.fromString(uidArray[index].trim());
          gameObjectDataList.add(new GameObjectData(blueprintId, uuid, x, y, z));
        } else {
          break;
        }
        index++;
      }
      if (gameObjectDataList.isEmpty() && !coordinates.trim().isEmpty()) {
        throw new GameObjectParseException("Invalid coordinate format: " + coordinates);
      }

      return gameObjectDataList;

    } catch (NumberFormatException e) {
      throw new GameObjectParseException(e.getMessage(), e);
    }
  }
}
