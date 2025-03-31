package oogasalad.editor.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.model.data.object.EditorObject;

public class EditorLevelData {

  private List<String> myGroups;

  private List<Layer> myLayers;
  private Map<Layer, List<EditorObject>> myLayerDataMap;
  private Map<UUID, EditorObject> myObjectdataMap;
  private Layer myCurrentLayer;

  public EditorLevelData() {
    myGroups = new ArrayList<>();
    myLayers = new ArrayList<>();
  }

  public List<String> getGroups() {
    return myGroups;
  }

  public void addGroup(String group) {
    myGroups.add(group);
  }

  public void removeGroup(String group) {
    myGroups.remove(group);
  }

  public List<Layer> getLayers() {
    return myLayers;
  }

  public void addLayer(Layer layer) {
    int index = 0;
    while (index < myLayers.size() && layer.getPriority() <= myLayers.get(index).getPriority()) {
      index++; // Insert the layer to maintain descending priority levels
    }
    myLayers.add(index, layer);
    myLayerDataMap.put(layer, new ArrayList<>());
  }

  public EditorObject getEditorObject(double x, double y) {
    for (List<EditorObject> objects : myLayerDataMap.values()) {

    }
  }
}
