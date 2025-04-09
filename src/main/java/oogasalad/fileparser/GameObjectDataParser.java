package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oogasalad.fileparser.records.GameObjectData;
import org.w3c.dom.Element;

public class GameObjectDataParser {

  /**
   * Parses a game object XML element and creates a list of GameObjectData objects.
   *
   * @param gameObjectElement the XML element representing the game object
   * @param z                 the z-index for the game object
   * @return a list of GameObjectData objects created from the element
   */
  public List<GameObjectData> getGameObjectData(Element gameObjectElement, int z) {
    // Parse the blueprint id from the "id" attribute.
    int blueprintID = Integer.parseInt(gameObjectElement.getAttribute("id"));

    // Get the uid attribute and split by comma.
    String uidAttr = gameObjectElement.getAttribute("uid");
    String[] uidArray = uidAttr.split(",");

    // Get the coordinates attribute.
    String coordinates = gameObjectElement.getAttribute("coordinates");

    // Create a list to hold the resulting game object data.
    List<GameObjectData> gameObjectDataList = new ArrayList<>();

    // Use a regex pattern to match coordinate pairs of the form (x,y).
    Pattern pattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
    Matcher matcher = pattern.matcher(coordinates);

    int index = 0;
    while (matcher.find()) {
      // Parse x and y as integers.
      int x = Integer.parseInt(matcher.group(1));
      int y = Integer.parseInt(matcher.group(2));

      // Ensure we have a corresponding uid.
      if (index < uidArray.length) {
        UUID uuid = UUID.fromString(uidArray[index].trim());
        gameObjectDataList.add(new GameObjectData(blueprintID, uuid, x, y, z));
      } else {
        // Optionally handle the mismatch (e.g., log an error).
        break;
      }
      index++;
    }

    return gameObjectDataList;
  }
}
