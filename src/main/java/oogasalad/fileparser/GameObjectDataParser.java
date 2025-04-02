package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.w3c.dom.Element;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;

public class GameObjectDataParser {

  public List<GameObjectData> getGameObjectData(Element gameObjectElement, int z) {
      System.out.println(gameObjectElement.getAttribute("id"));
      int blueprintID = Integer.parseInt(gameObjectElement.getAttribute("uid"));
      UUID uuid = UUID.fromString(gameObjectElement.getAttribute("id"));
      int x = Integer.parseInt(gameObjectElement.getAttribute("x"));
      int y = Integer.parseInt(gameObjectElement.getAttribute("y"));
      String uidList = gameObjectElement.getAttribute("uid");
      String coordinates = gameObjectElement.getAttribute("coordinates");
      String[] coordinatesList = coordinates.split(",");
      List<GameObjectData> gameObjectDataList = new ArrayList<>();
      int count = 0;
      for (String uid : uidList.split(",")) {
        for (String coordinate : coordinatesList) {
          count = 0;
          for (char c : coordinate.toCharArray()) {
            if (Character.isDigit(c)) {
              if (count == 0) {
                x = Integer.parseInt(coordinate);
                count++;
              } else if (count == 1) {
                y = Integer.parseInt(coordinate);
                count++;
              }
            }
          }
        }
        gameObjectDataList.add(new GameObjectData(blueprintID, uuid, x, y, z));
      }
      return gameObjectDataList;
  }
}
