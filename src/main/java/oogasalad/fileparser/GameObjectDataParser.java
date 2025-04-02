package oogasalad.fileparser;

import java.util.List;
import java.util.UUID;
import org.w3c.dom.Element;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;

public class GameObjectDataParser {

  public GameObjectData getGameObjectData(Element gameObjectElement,int z) {
    int blueprintID = Integer.parseInt(gameObjectElement.getAttribute("blueprintID"));
    UUID uuid = UUID.fromString(gameObjectElement.getAttribute("id"));
    int x = Integer.parseInt(gameObjectElement.getAttribute("x"));
    int y = Integer.parseInt(gameObjectElement.getAttribute("y"));
    return new GameObjectData(blueprintID, uuid, x, y, z);
  }

}
