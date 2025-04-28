package oogasalad.editor.model.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the level data used by the editor, including groups, layers, and mappings between
 * editor objects and their respective layers. This class manages collections of editor objects
 * organized by layers and groups. It also loads editor configuration properties from a resource
 * file.
 *
 * @author Jacob You
 */
public class EditorLevelData {

  private static final Logger LOG = LogManager.getLogger(EditorLevelData.class);

  private String gameName;
  private String levelName;
  private Set<String> myGroups;
  private List<Layer> myLayers;
  private Map<Layer, List<EditorObject>> myLayerDataMap;
  private Map<UUID, EditorObject> myObjectDataMap;
  private SpriteSheetLibrary spriteLibrary;
  private SpriteTemplateMap spriteTemplateMap;
  private CameraData cameraData;

  private static final Properties editorConfig = new Properties();
  private static final String propertyFile = "oogasalad/config/editor/resources/editorConfig.properties";

  static {
    try (InputStream is = EditorLevelData.class.getClassLoader()
        .getResourceAsStream(propertyFile)) {
      if (is != null) {
        editorConfig.load(is);
      } else {
        LOG.error("Could not find editor configuration file: {}", propertyFile);
      }
    } catch (IOException | NullPointerException e) {
      LOG.error("Failed to load editor configuration properties from {}: {}", propertyFile,
          e.getMessage(), e);
    }
  }

  /**
   * Constructs a new EditorLevelData instance. Initializes groups, layers, layer data mapping, and
   * the object data mapping. The first layer is automatically added.
   */
  public EditorLevelData() {
    gameName = "dinosaurgame";
    levelName = "";
    myGroups = new LinkedHashSet<>();
    myLayers = new ArrayList<>();
    myLayerDataMap = new HashMap<>();
    Layer firstLayer = getFirstLayer();
    myLayerDataMap.put(firstLayer, new ArrayList<>());
    myObjectDataMap = new HashMap<>();
    spriteLibrary = new SpriteSheetLibrary();
    spriteTemplateMap = new SpriteTemplateMap();
    cameraData = new CameraData();
    LOG.debug("EditorLevelData initialized.");
  }

  /**
   * Creates a new editor object and adds it to the first layer and the object data map.
   *
   * @return the unique identifier of the newly created editor object
   */
  public UUID createEditorObject() {
    EditorObject newObject = new EditorObject(this);
    Layer firstLayer = getFirstLayer();
    myLayerDataMap.computeIfAbsent(firstLayer, k -> new ArrayList<>()).add(newObject);
    myObjectDataMap.put(newObject.getIdentityData().getId(), newObject);
    LOG.debug("Created and registered new EditorObject with ID: {}", newObject.getId());
    return newObject.getIdentityData().getId();
  }

  /**
   * Creates a new editor object using the specified prefab name. (PLACEHOLDER)
   *
   * @param prefab the prefab identifier to use for creation
   * @return the unique identifier of the created editor object, or null if not implemented
   */
  public UUID createEditorObject(String prefab) {
    LOG.warn("createEditorObject(String prefab) is not implemented.");
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
    LOG.debug("Removing EditorObject with ID: {}", uuid);
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
    boolean removed = false;
    if (objectsInLayer != null) {
      removed = objectsInLayer.remove(object);
    }
    LOG.debug("Removing object {} from layer {}: {}", object.getId(), layer.getName(),
        removed ? "Success" : "Failed (not found)");
    return removed;
  }

  /**
   * Updates the reference to an existing editor object in the main data map.
   *
   * @param id            the UUID of the editor object to update
   * @param updatedObject the updated {@link EditorObject} instance
   * @return true if the update was successful, false if no object with the given ID exists
   * @throws NullPointerException if either the id or updatedObject is null
   */
  public boolean updateObjectInDataMap(UUID id, EditorObject updatedObject) {
    Objects.requireNonNull(id, "ID cannot be null for update.");
    Objects.requireNonNull(updatedObject, "Updated object cannot be null.");
    boolean updated = false;
    if (myObjectDataMap.containsKey(id)) {
      myObjectDataMap.put(id, updatedObject);
      updated = true;
    }
    else{
      myObjectDataMap.put(id, updatedObject);
    }
    LOG.debug("Updating object {} in data map: {}", id, updated ? "Success" : "Failed (not found)");
    return updated;
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
   * Retrieves the list of group names defined in the level. Returns a new list containing the
   * elements of the internal set.
   *
   * @return a new {@link List} of group names, preserving insertion order.
   */
  public List<String> getGroups() {
    return new ArrayList<>(myGroups);
  }

  /**
   * Adds a new group name to the level. Duplicates are automatically handled by the Set.
   *
   * @param group the group name to add. If null or empty, the group is not added.
   * @return true if the group was added (i.e., it wasn't already present), false otherwise.
   */
  public boolean addGroup(String group) {
    if (group == null || group.trim().isEmpty()) {
      LOG.warn("Attempted to add null or empty group name.");
      return false;
    }
    boolean added = myGroups.add(group);
    if (added) {
      LOG.debug("Added new group: {}", group);
    } else {
      LOG.trace("Group '{}' already exists, not added again.", group);
    }
    return added;
  }

  /**
   * Gets the CameraData object from the level object
   *
   * @return the {@link CameraData} of the object
   */
  public CameraData getCameraData() {
    return cameraData;
  }

  /**
   * Removes a group from the level if no editor object is associated with it.
   *
   * @param group the group name to remove
   * @return true if the group was successfully removed, false if any editor object is still
   * associated with it or if the group didn't exist.
   */
  public boolean removeGroup(String group) {
    if (group == null || !myGroups.contains(group)) {
      LOG.warn("Attempted to remove non-existent group: {}", group);
      return false;
    }
    for (EditorObject object : myObjectDataMap.values()) {
      if (object.getIdentityData() != null && group.equals(object.getIdentityData().getType())) {
        LOG.warn("Cannot remove group '{}' because it is still associated with object {}", group,
            object.getId());
        return false;
      }
    }
    boolean removed = myGroups.remove(group);
    if (removed) {
      LOG.debug("Removed group: {}", group);
    }
    return removed;
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
      LOG.info("No layers found, creating default layer 'New Layer'.");
      addLayer(new Layer("New Layer", 0));
    }
    if (myLayers.isEmpty()) {
      LOG.error("Failed to create or retrieve the first layer!");
      return null;
    }
    return myLayers.get(0);
  }

  /**
   * Adds a new layer to the level. The layer is inserted into the list based on its priority,
   * maintaining descending order (higher priority first). Also ensures a map entry exists for the
   * new layer.
   *
   * @param layer the {@link Layer} to add. If null, the method does nothing.
   */
  public void addLayer(Layer layer) {
    if (layer == null) {
      LOG.warn("Attempted to add a null layer.");
      return;
    }

    int index = 0;
    while (index < myLayers.size() && layer.getPriority() <= myLayers.get(index).getPriority()) {
      index++;
    }
    myLayers.add(index, layer);
    myLayerDataMap.computeIfAbsent(layer, k -> new ArrayList<>());
    LOG.debug("Added layer '{}' with priority {} at index {}", layer.getName(), layer.getPriority(),
        index);
  }

  /**
   * Removes a layer from the level if it exists and is empty (contains no objects). Cannot remove
   * the last remaining layer.
   *
   * @param layerName the name of the layer to remove
   * @return true if the layer was removed; false otherwise (layer not found, not empty, or last
   * layer).
   */
  public boolean removeLayer(String layerName) {
    if (myLayers.size() <= 1) {
      LOG.warn("Cannot remove the last remaining layer '{}'.", layerName);
      return false;
    }

    var maybeLayer = myLayers.stream()
        .filter(layer -> layer.getName().equals(layerName))
        .findFirst();
    if (maybeLayer.isEmpty()) {
      LOG.warn("Layer '{}' not found for removal.", layerName);
      return false;
    }
    Layer layerToRemove = maybeLayer.get();

    var objects = myLayerDataMap.getOrDefault(layerToRemove, Collections.emptyList());
    if (!objects.isEmpty()) {
      LOG.warn("Cannot remove non-empty layer '{}'. It contains {} objects.",
          layerName, objects.size());
      return false;
    }

    boolean removed = myLayers.remove(layerToRemove);
    myLayerDataMap.remove(layerToRemove);

    if (removed) {
      LOG.debug("Removed empty layer: {}", layerName);
    } else {
      LOG.error("Failed to remove layer '{}' from list, though it was found.", layerName);
    }
    return removed;
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
   * Retrieves the current sprite library for the current level
   *
   * @return a {@link SpriteSheetLibrary} for the current level
   */
  public SpriteSheetLibrary getSpriteLibrary() {
    return spriteLibrary;
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
   * Adds a sprite template to the current sprite template mapping.
   *
   * @param spriteTemplate The sprite template to add to the mapping. If null, nothing is added.
   */
  public void addSpriteTemplate(SpriteTemplate spriteTemplate) {
    if (spriteTemplate != null) {
      spriteTemplateMap.addSpriteTemplate(spriteTemplate);
      LOG.debug("Added sprite template: {}", spriteTemplate.getName());
    } else {
      LOG.warn("Attempted to add a null sprite template.");
    }
  }

  /**
   * Gets the map holding sprite templates.
   *
   * @return The SpriteTemplateMap instance.
   */
  public SpriteTemplateMap getSpriteTemplateMap() {
    return spriteTemplateMap;
  }

  /**
   * Retrieves the minimum and maximum dimensions encompassing all object hitboxes.
   *
   * @return an integer array of [minX, minY, maxX, maxY], or [0, 0, 0, 0] if no objects exist.
   */
  public int[] getBounds() {
    if (myObjectDataMap.isEmpty()) {
      return new int[]{0, 0, 0, 0};
    }

    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;

    for (EditorObject object : myObjectDataMap.values()) {
      HitboxData hitbox = object.getHitboxData();
      if (hitbox != null) {
        double x = hitbox.getX();
        double y = hitbox.getY();
        double width = hitbox.getWidth();
        double height = hitbox.getHeight();

        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x + width);
        maxY = Math.max(maxY, y + height);
      } else {
        LOG.warn("Object {} has null HitboxData when calculating bounds.", object.getId());
      }
    }

    if (minX == Double.MAX_VALUE) {
      return new int[]{0, 0, 0, 0};
    }

    return new int[]{(int) Math.floor(minX), (int) Math.floor(minY), (int) Math.ceil(maxX),
        (int) Math.ceil(maxY)};
  }

  /**
   * sets the game name
   *
   * @param gameName the game name to set it to
   */
  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  /**
   * Get the name of the atlas given an objectId
   *
   * @param objectId the id of the object to get the atlas of
   * @return the name of the atlas
   */
  public SpriteSheetAtlas getAtlas(UUID objectId) {
    EditorObject obj = myObjectDataMap.get(objectId);
    if (obj == null) {
      LOG.warn("getAtlasIdForObject: no object found for ID {}", objectId);
      return null;
    }

    // sprite-data might be absent (e.g. prefab not yet assigned)
    var spriteData = obj.getSpriteData();
    if (spriteData == null) {
      LOG.trace("getAtlasIdForObject: object {} has no SpriteData", objectId);
      return null;
    }

    // every SpriteData built from a template keeps a reference to that template
    String templateName = spriteData.getTemplateName();
    SpriteTemplate template = spriteTemplateMap.getSpriteData(templateName);
    if (template == null) {
      LOG.trace("getAtlasIdForObject: object {} has SpriteData but no template", objectId);
      return null;
    }

    String atlasFile = template.getAtlasFile();
    if (atlasFile == null || atlasFile.isBlank()) {
      LOG.trace("getAtlasIdForObject: template for object {} has empty atlasFile", objectId);
      return null;
    }

    return spriteLibrary.getAtlas(atlasFile);
  }

  /**
   * Gets the layer priority of an editor object by its UUID.
   * This is a convenience method that gets the object, retrieves its identity data,
   * gets the layer from the identity data, and then gets the priority from the layer.
   *
   * @param uuid the UUID of the editor object
   * @return the priority of the layer the object belongs to, or 0 if the object or its layer is not found
   */
  public int getObjectLayerPriority(UUID uuid) {
    if (uuid == null) {
      LOG.warn("Attempted to get layer priority with null UUID");
      return 0;
    }

    EditorObject obj = myObjectDataMap.get(uuid);
    if (obj == null) {
      LOG.warn("Object with UUID {} not found when getting layer priority", uuid);
      return 0;
    }

    if (obj.getIdentityData() == null) {
      LOG.warn("Object with UUID {} has null identity data", uuid);
      return 0;
    }

    Layer layer = obj.getIdentityData().getLayer();
    if (layer == null) {
      LOG.warn("Object with UUID {} has null layer in its identity data", uuid);
      return 0;
    }

    return layer.getPriority();
  }

  /**
   * Returns all editor objects sorted by layer priority, from lowest to highest (back to front).
   * This is useful for rendering objects in the correct Z-order in the frontend.
   * Objects with null layers or missing identity data will be placed at the bottom (lowest priority).
   *
   * @return a list of editor objects sorted by layer priority (ascending order)
   */
  public List<EditorObject> getObjectsSortedByLayerPriority() {
    List<EditorObject> allObjects = new ArrayList<>(myObjectDataMap.values());

    // Sort objects by layer priority (ascending order for rendering from back to front)
    allObjects.sort((obj1, obj2) -> {
      int priority1 = getObjectLayerPriorityInternal(obj1);
      int priority2 = getObjectLayerPriorityInternal(obj2);
      return Integer.compare(priority1, priority2);
    });

    return allObjects;
  }

  /**
   * Internal helper method to get the layer priority of an EditorObject.
   * Returns 0 if the object has null identity data or null layer.
   *
   * @param obj the EditorObject to get the layer priority for
   * @return the layer priority, or 0 if unavailable
   */
  private int getObjectLayerPriorityInternal(EditorObject obj) {
    if (obj == null || obj.getIdentityData() == null) {
      return 0;
    }

    Layer layer = obj.getIdentityData().getLayer();
    return (layer != null) ? layer.getPriority() : 0;
  }

  /**
   * Returns all objects on a specific layer.
   * This is useful for operations that need to manipulate or select objects from a particular layer.
   *
   * @param layerName the name of the layer to get objects from
   * @return a list of editor objects on the specified layer, or an empty list if the layer doesn't exist
   */
  public List<EditorObject> getObjectsByLayer(String layerName) {
    if (layerName == null || layerName.isEmpty()) {
      LOG.warn("Attempted to get objects from null or empty layer name");
      return Collections.emptyList();
    }

    // Find the layer by name
    Layer targetLayer = myLayers.stream()
        .filter(layer -> layerName.equals(layer.getName()))
        .findFirst()
        .orElse(null);

    if (targetLayer == null) {
      LOG.warn("Layer '{}' not found when getting objects", layerName);
      return Collections.emptyList();
    }

    // Return all objects on this layer
    List<EditorObject> layerObjects = myLayerDataMap.get(targetLayer);
    return layerObjects != null ? new ArrayList<>(layerObjects) : Collections.emptyList();
  }
}