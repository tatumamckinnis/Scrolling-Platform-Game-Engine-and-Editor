package oogasalad.editor.model.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class EditorLevelData {

  private List<String> myGroups;

  private List<Layer> myLayers;
  private Map<Layer, List<EditorObject>> myLayerDataMap;
  private Map<UUID, EditorObject> myObjectDataMap;

  private static final Properties editorConfig = new Properties();
  private static final String propertyFile = "oogasalad/config/editorConfig.properties";

  static {
    try (InputStream is = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(propertyFile)) {
      editorConfig.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public EditorLevelData() {
    myGroups = new ArrayList<>();
    myLayers = new ArrayList<>();
    myLayerDataMap = new HashMap<>();
    myLayers.add(getFirstLayer());
    myLayerDataMap.put(getFirstLayer(), new ArrayList<>());
    myObjectDataMap = new HashMap<>();
  }

  public UUID createEditorObject() {
    EditorObject newObject = new EditorObject(this);
    myLayerDataMap.getOrDefault(getFirstLayer(), new ArrayList<>()).add(newObject);
    myObjectDataMap.put(newObject.getIdentityData().getId(), newObject);
    return newObject.getIdentityData().getId();
  }

  public List<String> getGroups() {
    return myGroups;
  }

  public void addGroup(String group) {
    myGroups.add(group);
  }

  public boolean removeGroup(String group) {
    for (EditorObject object : myObjectDataMap.values()) {
      if (group.equals(object.getIdentityData().getGroup())) {
        return false;
      }
    }
    myGroups.remove(group);
    return true;
  }

  public List<Layer> getLayers() {
    return myLayers;
  }

  public Layer getFirstLayer() {
    if (myLayers.isEmpty()) {
      addLayer(new Layer("New Layer", 0)); // TODO: Make this to a default
    }
    return myLayers.get(0);
  }

  public void addLayer(Layer layer) {
    int index = 0;
    while (index < myLayers.size() && layer.getPriority() <= myLayers.get(index).getPriority()) {
      index++; // Insert the layer to maintain descending priority levels
    }
    myLayers.add(index, layer);
    myLayerDataMap.put(layer, new ArrayList<>());
  }

  public boolean removeLayer(String layerName) {
    for (Layer layer : myLayers) {
      if (layer.getName().equals(layerName)) {
        if (myLayerDataMap.containsKey(layer) && myLayerDataMap.get(layer).isEmpty()) {
          myLayerDataMap.remove(layer);
          return true;
        }
      }
    }
    return false;
  }

  public EditorObject getEditorObject(UUID uuid) {
    return myObjectDataMap.get(uuid);
  }

  public Properties getEditorConfig() {
    return editorConfig;
  }
}
