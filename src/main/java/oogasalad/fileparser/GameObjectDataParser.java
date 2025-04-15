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
   *
   * @param gameObjectElement the XML element representing the game object
   * @param z                 the z-index layer of the game object
   * @return a list of {@link GameObjectData} objects created from the element
   * @throws GameObjectParseException if the input data is malformed or parsing fails
   */
  public List<GameObjectData> getGameObjectData(Element gameObjectElement, int z)
      throws GameObjectParseException {
    try {
      int blueprintId = parseBlueprintId(gameObjectElement);
      String[] uidArray = parseUIDs(gameObjectElement);
      String coordinates = gameObjectElement.getAttribute("coordinates");
      List<int[]> parsedCoordinates = parseCoordinates(coordinates);
      return buildGameObjectData(blueprintId, uidArray, parsedCoordinates, z);

    } catch (NumberFormatException e) {
      throw new GameObjectParseException(e.getMessage(), e);
    }
  }

  /**
   * Extracts and parses the blueprint ID from the XML element.
   *
   * @param element the XML element containing the blueprint ID attribute
   * @return the parsed blueprint ID as an integer
   * @throws NumberFormatException if the ID is not a valid integer
   */
  private int parseBlueprintId(Element element) throws NumberFormatException {
    return Integer.parseInt(element.getAttribute("id"));
  }

  /**
   * Splits the UID attribute from the XML element into an array of strings.
   *
   * @param element the XML element containing the UID attribute
   * @return an array of UID strings
   */
  private String[] parseUIDs(Element element) {
    String uidAttr = element.getAttribute("uid");
    return uidAttr.split(",");
  }

  /**
   * Parses the coordinate string into a list of integer coordinate pairs. Each pair is represented
   * as an int array of size two, where index 0 is x and index 1 is y.
   *
   * @param coordinates the string containing coordinates in the form "(x1,y1),(x2,y2),..."
   * @return a list of coordinate pairs
   * @throws GameObjectParseException if no valid coordinates are found but the string is non-empty
   */
  private List<int[]> parseCoordinates(String coordinates) throws GameObjectParseException {
    List<int[]> coords = new ArrayList<>();
    Pattern pattern = Pattern.compile("\\((-?\\d+),(-?\\d+)\\)");
    Matcher matcher = pattern.matcher(coordinates);

    while (matcher.find()) {
      int x = Integer.parseInt(matcher.group(1));
      int y = Integer.parseInt(matcher.group(2));
      coords.add(new int[]{x, y});
    }

    if (coords.isEmpty() && !coordinates.trim().isEmpty()) {
      throw new GameObjectParseException("Invalid coordinate format: " + coordinates);
    }
    return coords;
  }

  /**
   * Builds a list of {@link GameObjectData} records by pairing each parsed coordinate with a
   * corresponding UUID from the UID array. If there are fewer UIDs than coordinates, processing
   * stops at the end of the UID array.
   *
   * @param blueprintId the blueprint identifier for each game object
   * @param uidArray    the array of UID strings
   * @param coordinates the list of coordinate pairs
   * @param z           the z-index layer for the game object
   * @return a list of {@link GameObjectData} objects constructed from the inputs
   */
  private List<GameObjectData> buildGameObjectData(int blueprintId, String[] uidArray,
      List<int[]> coordinates, int z) {
    List<GameObjectData> gameObjectDataList = new ArrayList<>();
    int index = 0;

    for (int[] coord : coordinates) {
      if (index < uidArray.length) {
        UUID uuid = UUID.fromString(uidArray[index].trim());
        gameObjectDataList.add(new GameObjectData(blueprintId, uuid, coord[0], coord[1], z));
      } else {
        break;
      }
      index++;
    }

    return gameObjectDataList;
  }
}
