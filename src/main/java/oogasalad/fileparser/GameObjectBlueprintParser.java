package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.exceptions.GameObjectParseException;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.SpriteData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GameObjectBlueprintParser {
  private String groupName = "";
  private String gameName = "";

  public List<GameObjectData> getLevelGameObjectData(Element root) throws GameObjectParseException {
    NodeList gameNodes = root.getElementsByTagName("game");
    List<GameObjectData> gameObjectDataList = new ArrayList<>();
    for (int i = 0; i < gameNodes.getLength(); i++) {
      Node node = gameNodes.item(i);
      if (!(node instanceof Element)) {
        throw new GameObjectParseException("error.gameNode.notElement");
      }
      Element gameElement = (Element) node;
      gameName = gameElement.getAttribute("name");
      gameObjectDataList.addAll(parseByGame(gameElement));
    }
    return gameObjectDataList;
  }

  private List<GameObjectData> parseByGame(Element gameNode) throws GameObjectParseException {
    NodeList objectGroupNodes = gameNode.getElementsByTagName("objectGroup");
    List<GameObjectData> gameObjectsList = new ArrayList<>();
    for (int i = 0; i < objectGroupNodes.getLength(); i++) {
      Node node = objectGroupNodes.item(i);
      if (!(node instanceof Element)) {
        throw new GameObjectParseException("error.objectGroup.notElement");
      }
      Element objectGroup = (Element) node;
      groupName = objectGroup.getAttribute("name");
      gameObjectsList.addAll(parseByObjectGroup(objectGroup));
    }
    return gameObjectsList;
  }

  private List<GameObjectData> parseByObjectGroup(Element objectGroupNode) throws GameObjectParseException {
    List<GameObjectData> gameObjectsGroupList = new ArrayList<>();
    NodeList gameObjectNodes = objectGroupNode.getChildNodes();
    for (int i = 0; i < gameObjectNodes.getLength(); i++) {
      Node node = gameObjectNodes.item(i);
      // Skip text nodes that are only whitespace
      if (node.getNodeType() == Node.TEXT_NODE && node.getTextContent().trim().isEmpty()) {
        continue;
      }
      if (!(node instanceof Element)) {
        throw new GameObjectParseException("error.gameObject.notElement");
      }
      Element gameObjectNode = (Element) node;
      gameObjectsGroupList.add(parseGameObjectData(gameObjectNode));
    }
    return gameObjectsGroupList;
  }


  private GameObjectData parseGameObjectData(Element gameObjectNode) throws GameObjectParseException {
    try {
      int id = Integer.parseInt(gameObjectNode.getAttribute("id"));
      String type = gameObjectNode.getAttribute("type");
      String spriteName = gameObjectNode.getAttribute("spriteName");
      String spriteFile = gameObjectNode.getAttribute("spriteFile");

      // Default values for x, y, and layer
      int x = 0;
      int y = 0;
      int layer = 0;

      // Parse properties defined in the <properties> tag
      Map<String, Map<String, String>> propertiesMap = parseProperties(gameObjectNode);

      return new GameObjectData(
          id,
          0, //gameName,
          type,
          groupName,
          spriteName,
          new SpriteData(null,0,0,0,0,null,null,null), //spriteFile,
          x,
          y,
          layer,
          new ArrayList<>() //propertiesMap
      );
    } catch (NumberFormatException e) {
      throw new GameObjectParseException("error.number.format");
    }
  }

  // Helper method to parse the <properties> element and its child handlers
  private Map<String, Map<String, String>> parseProperties(Element gameObjectNode) throws GameObjectParseException {
    Map<String, Map<String, String>> propertiesMap = new HashMap<>();
    NodeList propertiesList = gameObjectNode.getElementsByTagName("properties");
    if (propertiesList.getLength() > 0) {
      Node node = propertiesList.item(0);
      if (!(node instanceof Element)) {
        throw new GameObjectParseException("error.properties.notElement");
      }
      Element propertiesElement = (Element) node;
      NodeList handlerNodes = propertiesElement.getChildNodes();
      for (int i = 0; i < handlerNodes.getLength(); i++) {
        Node handlerNode = handlerNodes.item(i);
        if (!(handlerNode instanceof Element)) {
          throw new GameObjectParseException("error.handler.notElement");
        }
        Element handlerElement = (Element) handlerNode;
        String handlerName = handlerElement.getTagName();
        Map<String, String> handlerData = parseHandlerProperties(handlerElement);
        propertiesMap.put(handlerName, handlerData);
      }
    }
    return propertiesMap;
  }

  // Helper method to parse all data elements (e.g., <data> or <variable>) within a property handler
  private Map<String, String> parseHandlerProperties(Element handlerElement) throws GameObjectParseException {
    Map<String, String> handlerData = new HashMap<>();
    NodeList dataNodes = handlerElement.getChildNodes();
    for (int j = 0; j < dataNodes.getLength(); j++) {
      Node dataNode = dataNodes.item(j);
      if (!(dataNode instanceof Element)) {
        throw new GameObjectParseException("error.data.notElement");
      }
      Element dataElement = (Element) dataNode;
      for (int k = 0; k < dataElement.getAttributes().getLength(); k++) {
        Node attr = dataElement.getAttributes().item(k);
        String attrName = attr.getNodeName();
        String attrValue = attr.getNodeValue();
        // Concatenate attribute values if the same key already exists
        if (handlerData.containsKey(attrName)) {
          handlerData.put(attrName, handlerData.get(attrName) + "," + attrValue);
        } else {
          handlerData.put(attrName, attrValue);
        }
      }
    }
    return handlerData;
  }
}
