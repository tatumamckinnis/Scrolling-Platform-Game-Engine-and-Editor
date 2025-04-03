package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import oogasalad.fileparser.exceptions.BlueprintParseException;
import oogasalad.fileparser.exceptions.GameObjectParseException;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.SpriteData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.HitBoxData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BlueprintDataParser {
  private String groupName = "";
  private String gameName = "";
  private SpriteDataParser mySpriteDataParser;
  private List<EventData> myEventDataList;

  public Map<Integer,BlueprintData> getBlueprintData(Element root, List<EventData> EventList) throws BlueprintParseException {
    myEventDataList = EventList;
    NodeList gameNodes = root.getElementsByTagName("game");
    List<BlueprintData> gameObjectDataList = new ArrayList<>();
    for (int i = 0; i < gameNodes.getLength(); i++) {
      Node node = gameNodes.item(i);
      if (!(node instanceof Element)) {
        throw new BlueprintParseException("error.gameNode.notElement");
      }
      Element gameElement = (Element) node;
      gameName = gameElement.getAttribute("name");
      gameObjectDataList.addAll(parseByGame(gameElement));
    }
    return createBlueprintDataMap(gameObjectDataList);
  }

  private Map<Integer,BlueprintData> createBlueprintDataMap(List<BlueprintData> blueprintDataList){
    Map<Integer,BlueprintData> blueprintDataMap = new HashMap<>();
    for(int i = 0; i < blueprintDataList.size(); i++){
      blueprintDataMap.put(blueprintDataList.get(i).blueprintId(), blueprintDataList.get(i));
    }
    return blueprintDataMap;
  }

  private List<BlueprintData> parseByGame(Element gameNode) throws BlueprintParseException {
    NodeList objectGroupNodes = gameNode.getElementsByTagName("objectGroup");
    List<BlueprintData> gameObjectsList = new ArrayList<>();
    for (int i = 0; i < objectGroupNodes.getLength(); i++) {
      Node node = objectGroupNodes.item(i);
      if (!(node instanceof Element)) {
        throw new BlueprintParseException("error.objectGroup.notElement");
      }
      Element objectGroup = (Element) node;
      groupName = objectGroup.getAttribute("name");
      gameObjectsList.addAll(parseByObjectGroup(objectGroup));
    }
    return gameObjectsList;
  }

  private List<BlueprintData> parseByObjectGroup(Element objectGroupNode) throws BlueprintParseException {
    List<BlueprintData> gameObjectsGroupList = new ArrayList<>();
    NodeList gameObjectNodes = objectGroupNode.getElementsByTagName("object");
    for (int i = 0; i < gameObjectNodes.getLength(); i++) {
      Node node = gameObjectNodes.item(i);
      // Skip text nodes that are only whitespace
      if (node.getNodeType() == Node.TEXT_NODE && node.getTextContent().trim().isEmpty()) {
        continue;
      }
      if (!(node instanceof Element)) {
        continue;
      }
      Element gameObjectNode = (Element) node;
      gameObjectsGroupList.add(parseGameObjectData(gameObjectNode));
    }
    return gameObjectsGroupList;
  }

  private BlueprintData parseGameObjectData(Element gameObjectNode) throws BlueprintParseException {
    try {
      mySpriteDataParser = new SpriteDataParser();
      // Parse basic attributes
      int id = Integer.parseInt(gameObjectNode.getAttribute("id"));
      String type = gameObjectNode.getAttribute("type");
      String spriteName = gameObjectNode.getAttribute("spriteName");
      String spriteFile = gameObjectNode.getAttribute("spriteFile");
      // Create SpriteData from spriteName and spriteFile.
      SpriteData spriteData = null;
      if (!(gameName == null || gameName.isEmpty())) {
        spriteData = mySpriteDataParser.getSpriteData(gameName, groupName, type,
            spriteName, spriteFile);
      }
      // Parse HitBoxData from hitBoxWidth, hitBoxHeight, spriteDx, spriteDy.
      String hitBoxWidthStr = gameObjectNode.getAttribute("hitBoxWidth");
      String hitBoxHeightStr = gameObjectNode.getAttribute("hitBoxHeight");
      String spriteDxStr = gameObjectNode.getAttribute("spriteDx");
      String spriteDyStr = gameObjectNode.getAttribute("spriteDy");
      String hitBoxType = gameObjectNode.getAttribute("hitBoxType");
      int hitBoxWidth = hitBoxWidthStr.isEmpty() ? 0 : Integer.parseInt(hitBoxWidthStr);
      int hitBoxHeight = hitBoxHeightStr.isEmpty() ? 0 : Integer.parseInt(hitBoxHeightStr);
      int spriteDx = spriteDxStr.isEmpty() ? 0 : Integer.parseInt(spriteDxStr);
      int spriteDy = spriteDyStr.isEmpty() ? 0 : Integer.parseInt(spriteDyStr);
      HitBoxData hitBoxData = new HitBoxData(hitBoxType, hitBoxWidth, hitBoxHeight, spriteDx, spriteDy);

      // Parse event data: if eventIDs are provided, create dummy EventData objects.
      String eventIDs = gameObjectNode.getAttribute("eventIDs");
      List<EventData> eventDataList = new ArrayList<>();
      if (eventIDs != null && !eventIDs.isEmpty()) {
        String[] eventIdArray = eventIDs.split(",");
        for (String eventId : eventIdArray) {
          // Create a dummy EventData; other fields are set to empty or default values.
          eventDataList.add(getEventByID(eventId));
        }
      }


      // Parse and flatten properties directly containing <data> elements.
      Map<String, String> objectProperties = parseProperties(gameObjectNode);

      return new BlueprintData(
          id,
          gameName,
          groupName,
          type,
          spriteData,
          hitBoxData,
          eventDataList,
          objectProperties
      );
    } catch (NumberFormatException e) {
      throw new BlueprintParseException("error.number.format");
    }
  }

  private EventData getEventByID(String id) {
    for (EventData eventData : myEventDataList) {
      if (Objects.equals(id, eventData.eventId())) {
        return eventData;
      }
    }
    return null;
  }

  // Helper method to parse and flatten the <properties> element containing <data> elements.
  private Map<String, String> parseProperties(Element blueprintDataNode) throws BlueprintParseException {
    Map<String, String> propertiesMap = new HashMap<>();
    NodeList propertiesList = blueprintDataNode.getElementsByTagName("properties");
    if (propertiesList.getLength() > 0) {
      Node node = propertiesList.item(0);
      if (!(node instanceof Element)) {
        throw new BlueprintParseException("error.properties.notElement");
      }
      Element propertiesElement = (Element) node;
      NodeList dataNodes = propertiesElement.getChildNodes();
      for (int i = 0; i < dataNodes.getLength(); i++) {
        Node dataNode = dataNodes.item(i);
        // Skip text nodes that are only whitespace
        if (dataNode.getNodeType() == Node.TEXT_NODE && dataNode.getTextContent().trim().isEmpty()) {
          continue;
        }
        if (!(dataNode instanceof Element)) {
          throw new BlueprintParseException("error.data.notElement");
        }
        Element dataElement = (Element) dataNode;
        if (!"data".equals(dataElement.getTagName())) {
          continue; // Only process <data> elements.
        }
        String name = dataElement.getAttribute("name");
        String value = dataElement.getAttribute("value");
        if (name != null && !name.isEmpty()) {
          propertiesMap.put(name, value);
        }
      }
    }
    return propertiesMap;
  }
}
