package oogasalad.editor.model.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import oogasalad.editor.model.data.object.EditorObject;

public class EditorLevelData {

  private List<String> myGroups;

  private List<Layer> myLayers;
  private Map<Layer, List<EditorObject>> myLayerDataMap;
  private Map<UUID, EditorObject> myObjectDataMap;
  private Layer myCurrentLayer;

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
    myObjectDataMap = new HashMap<>();
    myCurrentLayer = new Layer("default");
  }

  public UUID createEditorObject() {
    EditorObject newObject = new EditorObject(this);
    myLayerDataMap.getOrDefault(myCurrentLayer, new ArrayList<>()).add(newObject);
    myObjectDataMap.put(newObject.getIdentityData().getId(), newObject);
    return newObject.getIdentityData().getId();
  }

  public UUID createEditorObject(String prefab) {
    return null; // Will eventually implement a Prefab API of sorts
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

  public EditorObject getEditorObject(UUID uuid) {
    return myObjectDataMap.get(uuid);
  }

  public Properties getEditorConfig() {
    return editorConfig;
  }
}
