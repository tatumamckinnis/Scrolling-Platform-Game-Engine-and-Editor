package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.exceptions.GameObjectParseException;
import oogasalad.fileparser.records.GameObjectData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import oogasalad.fileparser.records.GameObjectData;

public class LayerParser {
  Map<Integer,List<GameObjectData>> gameObjectMap;
  List<GameObjectData> gameObjectBlueprints;
  public Map<Integer,List<GameObjectData>> getGameObjectDataMap(Element layersElement, List<GameObjectData> gameObjectBlueprints ) {

    return gameObjectMap;
  }

  private List<GameObjectData> readLayerData(Element LayerNode){
    List<GameObjectData> gameObjects = new ArrayList<>();
    
    return gameObjects;
  }

  }
