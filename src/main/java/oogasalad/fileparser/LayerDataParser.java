package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.GameObjectData;
import org.w3c.dom.Element;

public class LayerDataParser {
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
