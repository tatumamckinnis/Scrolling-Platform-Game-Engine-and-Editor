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
 *
 * @author Billy McCune
 */
public class GameObjectDataParser {

  /**
   * Parses a game object XML element and creates a list of GameObjectData objects.
   *
   * @param gameObjectElement the XML element representing the game object
   * @param z                 the z-index for the game object
   * @return a list of GameObjectData objects created from the element
   * @throws GameObjectParseException if there is an issue with the game object data
   */
  public List<GameObjectData> getGameObjectData(Element gameObjectElement, int z)
      throws GameObjectParseException {
    try {
      int blueprintID = Integer.parseInt(gameObjectElement.getAttribute("id"));

      String uidAttr = gameObjectElement.getAttribute("uid");
      String[] uidArray = uidAttr.split(",");
      String coordinates = gameObjectElement.getAttribute("coordinates");
      List<GameObjectData> gameObjectDataList = new ArrayList<>();

      Pattern pattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
      Matcher matcher = pattern.matcher(coordinates);

      int index = 0;
      while (matcher.find()) {
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));

        if (index < uidArray.length) {
          UUID uuid = UUID.fromString(uidArray[index].trim());
          gameObjectDataList.add(new GameObjectData(blueprintID, uuid, x, y, z));
        } else {
          break;
        }
        index++;
      }

      return gameObjectDataList;

    } catch (NumberFormatException e) {
      throw new GameObjectParseException(e.getMessage());
    }
  }
}
