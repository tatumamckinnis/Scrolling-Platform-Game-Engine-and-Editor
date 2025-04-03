package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.GameObjectData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LayerDataParser {
  Map<Integer,List<GameObjectData>> gameObjectMap;
  GameObjectDataParser myGameObjectDataParser;

  public Map<Integer,List<GameObjectData>> getGameObjectDataMap(Element root) {
    Element layersElement = (Element) root.getElementsByTagName("layers").item(0);
    gameObjectMap = new HashMap<>();
    myGameObjectDataParser = new GameObjectDataParser();
    NodeList layers = layersElement.getElementsByTagName("layer");
    for(int i = 0;i < layers.getLength();i++) {
      Element LayerElement = (Element) layers.item(i);
      int z = Integer.parseInt(LayerElement.getAttribute("z"));
      if (gameObjectMap.containsKey(z)) {
        List<GameObjectData> gameObjects = readLayerData(LayerElement,z);
        for (GameObjectData gameObject : gameObjects) {
          gameObjectMap.get(z).add(gameObject);
        }
      }
      else {
        gameObjectMap.put(z, readLayerData(LayerElement, z));
      }
    }
    return gameObjectMap;
  }

  private List<GameObjectData> readLayerData(Element LayerElement, int z){
    List<GameObjectData> gameObjects = new ArrayList<>();
    Element dataNode = (Element) LayerElement.getElementsByTagName("data").item(0);
    NodeList gameObjectNodes = dataNode.getElementsByTagName("object");
    for(int i = 0;i < gameObjectNodes.getLength();i++) {
      if (gameObjectNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
        Element gameObjectElement = (Element) gameObjectNodes.item(i);
      gameObjects.addAll(myGameObjectDataParser.getGameObjectData(gameObjectElement, z));
    }
    }
    return gameObjects;
  }

}
