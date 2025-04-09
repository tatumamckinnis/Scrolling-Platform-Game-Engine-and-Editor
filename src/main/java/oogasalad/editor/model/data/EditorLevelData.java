package oogasalad.editor.model.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Represents the level data used by the editor, including groups, layers, and mappings between
 * editor objects and their respective layers. This class manages collections of editor objects
 * organized by layers and groups. It also loads editor configuration properties from a resource
 * file.
 *
 * @author Jacob You
 */
public class EditorLevelData {

  private static Logger LOG = Logger.getLogger(EditorLevelData.class.getName());

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
      LOG.info(e.getMessage());
    }
  }

  /**
   * Constructs a new EditorLevelData instance. Initializes groups, layers, layer data mapping, and
   * the object data mapping. The first layer is automatically added.
   */
  public EditorLevelData() {
    myGroups = new ArrayList<>();
    myLayers = new ArrayList<>();
    myLayerDataMap = new HashMap<>();
    myLayers.add(getFirstLayer());
    myLayerDataMap.put(getFirstLayer(), new ArrayList<>());
    myObjectDataMap = new HashMap<>();
  }

  /**
   * Creates a new editor object and adds it to the first layer and the object data map.
   *
   * @return the unique identifier of the newly created editor object
   */
  public UUID createEditorObject() {
    EditorObject newObject = new EditorObject(this);
    myLayerDataMap.getOrDefault(getFirstLayer(), new ArrayList<>()).add(newObject);
    myObjectDataMap.put(newObject.getIdentityData().getId(), newObject);
    return newObject.getIdentityData().getId();
  }

  /**
   * Creates a new editor object using the specified prefab name. (PLACEHOLDER)
   *
   * @param prefab the prefab identifier to use for creation
   * @return the unique identifier of the created editor object, or null if not implemented
   */
  public UUID createEditorObject(String prefab) {
    return null; // Will eventually implement a Prefab API of sorts
  }

  /**
   * Removes an editor object from the main object map using its UUID.
   *
   * @param uuid the unique identifier of the editor object to remove
   * @return the removed {@link EditorObject}, or null if no object was found with the given UUID
   * @throws NullPointerException if the provided uuid is @code null
   */
  public EditorObject removeObjectById(UUID uuid) {
    Objects.requireNonNull(uuid, "UUID cannot be null for removal.");
    return myObjectDataMap.remove(uuid);
  }

  /**
   * Removes a specific editor object from the list associated with a given layer.
   *
   * @param layer  the {@link Layer} from which the object should be removed
   * @param object the {@link EditorObject} instance to remove
   * @return true if the object was successfully removed from the layer, false otherwise
   * @throws NullPointerException if either the layer or object is null
   */
  public boolean removeObjectFromLayer(Layer layer, EditorObject object) {
    Objects.requireNonNull(layer, "Layer cannot be null for object removal.");
    Objects.requireNonNull(object, "Object cannot be null for removal.");
    List<EditorObject> objectsInLayer = myLayerDataMap.get(layer);
    if (objectsInLayer != null) {
      return objectsInLayer.remove(object);
    }
    return false;
  }

  /**
   * Updates the reference to an existing editor object in the main data map.
   *
   * @param id            the UUID of the editor object to update
   * @param updatedObject the updated {@link EditorObject} instance
   * @return if the update was successful, false if no object with the given ID exists
   * @throws NullPointerException if either the id or updatedObject is null
   */
  public boolean updateObjectInDataMap(UUID id, EditorObject updatedObject) {
    Objects.requireNonNull(id, "ID cannot be null for update.");
    Objects.requireNonNull(updatedObject, "Updated object cannot be null.");
    if (myObjectDataMap.containsKey(id)) {
      myObjectDataMap.put(id, updatedObject);
      return true;
    }
    return false;
  }

  /**
   * Retrieves the list of group names defined in the level.
   *
   * @return a {@link List} of group names
   */
  public List<String> getGroups() {
    return myGroups;
  }

  /**
   * Adds a new group name to the level.
   *
   * @param group the group name to add
   */
  public void addGroup(String group) {
    myGroups.add(group);
  }

  /**
   * Removes a group from the level if no editor object is associated with it.
   *
   * @param group the group name to remove
   * @return true if the group was successfully removed, false if any editor object is still
   * associated with it
   */
  public boolean removeGroup(String group) {
    for (EditorObject object : myObjectDataMap.values()) {
      if (group.equals(object.getIdentityData().getGroup())) {
        return false;
      }
    }
    myGroups.remove(group);
    return true;
  }

  /**
   * Retrieves the list of {@link Layer} objects defined in the level.
   *
   * @return a {@link List} of layers
   */
  public List<Layer> getLayers() {
    return myLayers;
  }

  /**
   * Retrieves the first layer in the level. If the level contains no layers, a new default layer is
   * added.
   *
   * @return the first {@link Layer} in the level
   */
  public Layer getFirstLayer() {
    if (myLayers.isEmpty()) {
      addLayer(new Layer("New Layer", 0)); // TODO: Make this to a default
    }
    return myLayers.get(0);
  }

  /**
   * Adds a new layer to the level. The layer is inserted into the list based on its priority,
   * maintaining descending order.
   *
   * @param layer the {@link Layer} to add
   */
  public void addLayer(Layer layer) {
    int index = 0;
    while (index < myLayers.size() && layer.getPriority() <= myLayers.get(index).getPriority()) {
      index++; // Insert the layer to maintain descending priority levels
    }
    myLayers.add(index, layer);
    myLayerDataMap.put(layer, new ArrayList<>());
  }

  /**
   * Removes a layer from the level if it exists and its associated editor object list is empty.
   *
   * @param layerName the name of the layer to remove
   * @return true if the layer was removed; false otherwise
   */
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

  /**
   * Retrieves the editor object corresponding to the specified UUID.
   *
   * @param uuid the unique identifier of the editor object
   * @return the corresponding {@link EditorObject}, or null if not found
   */
  public EditorObject getEditorObject(UUID uuid) {
    return myObjectDataMap.get(uuid);
  }

  /**
   * Retrieves the editor configuration properties.
   *
   * @return a {@link Properties} object containing configuration settings for the editor
   */
  public Properties getEditorConfig() {
    return editorConfig;
  }

  /**
   * Retrieves the mapping of editor objects by their UUIDs.
   *
   * @return a {@link Map} where keys are UUIDs and values are {@link EditorObject} instances
   */
  public Map<UUID, EditorObject> getObjectDataMap() {
    return myObjectDataMap;
  }
}