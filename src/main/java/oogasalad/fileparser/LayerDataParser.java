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
  List<GameObjectData> gameObjectBlueprints;
  GameObjectDataParser myGameObjectDataParser;

  public Map<Integer,List<GameObjectData>> getGameObjectDataMap(Element layersElement ) {
    gameObjectMap = new HashMap<Integer,List<GameObjectData>>();
    myGameObjectDataParser = new GameObjectDataParser();
    NodeList layers = layersElement.getElementsByTagName("layer");
    for(int i = 0;i < layers.getLength();i++) {
      Element LayerElement = (Element) layers.item(i);
      int z = Integer.parseInt(LayerElement.getAttribute("z"));
      gameObjectMap.put(z,readLayerData(LayerElement,z));
    }
    return gameObjectMap;
  }

  private List<GameObjectData> readLayerData(Element LayerElement, int z){
    List<GameObjectData> gameObjects = new ArrayList<>();
    NodeList gameObjectNodes = LayerElement.getElementsByTagName("data").item(0).getChildNodes();
    for(int i = 0;i < gameObjectNodes.getLength();i++) {
      if (gameObjectNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
        Element gameObjectElement = (Element) gameObjectNodes.item(i);
      gameObjects.addAll(myGameObjectDataParser.getGameObjectData(gameObjectElement, z));
    }
    }
    return gameObjects;
  }

}
