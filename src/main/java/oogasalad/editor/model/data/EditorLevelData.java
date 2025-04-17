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
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.object.HitboxData;

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

  private String gameName;
  private String levelName;
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
    gameName = "";
    levelName = "";
    myGroups = new ArrayList<>();
    myLayers = new ArrayList<>();
    myLayerDataMap = new HashMap<>();
    getFirstLayer(); // Instantiates the first layer if it does not exist.
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
    return null;
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
   * Retrieves the name of the game
   *
   * @return a {@link String} of the game name
   */
  public String getGameName() {
    return gameName;
  }

  /**
   * Retrieves the name of the level
   *
   * @return a {@link String} of the level name
   */
  public String getLevelName() {
    return levelName;
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
      if (group.equals(object.getIdentityData().getType())) {
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

  /**
   * Retrieves a list of editor objects by the Layer.
   *
   * @return a {@link Map} where keys are the Layer object and values are a {@link List} of
   * {@link EditorObject} instances
   */
  public Map<Layer, List<EditorObject>> getObjectLayerDataMap() {
    return myLayerDataMap;
  }

  /**
   * Retrieves the minimum and maximum dimensions of all objects.
   *
   * @return an integer array of minX, minY, maxX, maxY
   */
  public int[] getBounds() {
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;

    for (EditorObject object : myObjectDataMap.values()) {
      HitboxData hitbox = object.getHitboxData();
      double x = hitbox.getX();
      double y = hitbox.getY();
      double width = hitbox.getWidth();
      double height = hitbox.getHeight();

      minX = Math.min(minX, x);
      minY = Math.min(minY, y);
      maxX = Math.max(maxX, x + width);
      maxY = Math.max(maxY, y + height);
    }
    return new int[]{(int) minX, (int) minY, (int) maxX, (int) maxY};
  }
}