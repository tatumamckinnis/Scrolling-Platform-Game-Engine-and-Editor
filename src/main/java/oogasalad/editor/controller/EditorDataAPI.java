package oogasalad.editor.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.SpriteSheetLibrary;
import oogasalad.editor.model.data.SpriteTemplateMap;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import oogasalad.editor.model.saver.EditorFileConverter;
import oogasalad.editor.model.saver.SpriteSheetSaver;
import oogasalad.editor.model.saver.api.EditorFileConverterAPI;
import oogasalad.fileparser.DefaultFileParser;
import oogasalad.fileparser.FileParserApi;
import oogasalad.filesaver.savestrategy.SaverStrategy;
import oogasalad.filesaver.savestrategy.XmlStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides an API to manage editor data by interfacing with the various underlying data managers.
 * This class acts as a facade for performing operations on editor objects, layers, groups, and
 * dynamic variables, and delegates tasks to corresponding data managers within an
 * {@link EditorLevelData} instance.
 *
 * @author Jacob You
 */
public class EditorDataAPI {

  private static final Logger LOG = LogManager.getLogger(EditorDataAPI.class);
  private static final FileParserApi DEFAULT_FILE_PARSER = new DefaultFileParser();

  private final IdentityDataManager identityAPI;
  private final HitboxDataManager hitboxAPI;
  private final InputDataManager inputAPI;
  private final PhysicsDataManager physicsAPI;
  private final CollisionDataManager collisionAPI;
  private final SpriteDataManager spriteAPI;
  private final EditorLevelData level;
  private final DynamicVariableContainer dynamicVariableContainer;
  private final CustomEventDataManager customEventAPI;
  private final SpriteSheetDataManager spriteSheetAPI;
  private final EditorFileConverterAPI fileConverterAPI;
  private final FileParserApi fileParserAPI;
  private String currentGameDirectoryPath;

  /**
   * Constructs an EditorDataAPI instance, initializing the underlying {@link EditorLevelData} and
   * all related data managers.
   *
   * @author Jacob You
   */
  public EditorDataAPI() {
    this.level = new EditorLevelData();
    this.identityAPI = new IdentityDataManager(level);
    this.hitboxAPI = new HitboxDataManager(level);
    this.inputAPI = new InputDataManager(level);
    this.physicsAPI = new PhysicsDataManager(level);
    this.collisionAPI = new CollisionDataManager(level);
    this.spriteAPI = new SpriteDataManager(level);
    this.customEventAPI = new CustomEventDataManager(level);
    this.dynamicVariableContainer = new DynamicVariableContainer();
    this.spriteSheetAPI = new SpriteSheetDataManager(level);

    this.fileConverterAPI = new EditorFileConverter();
    this.fileParserAPI = DEFAULT_FILE_PARSER;
    LOG.info("EditorDataAPI initialized with new EditorLevelData.");
  }

  /**
   * Creates a new editor object in the underlying {@link EditorLevelData}.
   *
   * @return the UUID of the newly created editor object.
   */
  public UUID createEditorObject() {
    UUID newId = level.createEditorObject();
    LOG.debug("Created new EditorObject via EditorLevelData, ID: {}", newId);
    return newId;
  }

  /**
   * Retrieves the editor object associated with the given UUID.
   *
   * @param id the unique identifier of the editor object.
   * @return the corresponding {@link EditorObject} or null if no object is found.
   */
  public EditorObject getEditorObject(UUID id) {
    return level.getEditorObject(id);
  }

  /**
   * Removes the editor object corresponding to the provided UUID. Also attempts to remove the
   * object from its associated layer.
   *
   * @param id the unique identifier of the editor object to be removed.
   * @return whether the object was successfully removed.
   */
  public boolean removeEditorObject(UUID id) {
    Objects.requireNonNull(id, "Object ID cannot be null for removal.");
    LOG.debug("Attempting to remove object {} via EditorLevelData.", id);
    EditorObject removedObject = level.removeObjectById(id);

    if (removedObject != null) {
      Layer objectLayer = removedObject.getIdentityData().getLayer();
      if (objectLayer != null) {
        boolean removedFromLayer = level.removeObjectFromLayer(objectLayer, removedObject);
        if (!removedFromLayer) {
          LOG.warn("Object {} removed from main map but not found in expected layer {} map.", id,
              objectLayer.getName());
        }
      } else {
        LOG.warn("Removed object {} had no layer information.", id);
      }
      return true;
    }
    return false;
  }

  /**
   * Updates an existing editor object with new data.
   *
   * @param updatedObject the editor object containing the updated data.
   * @return whether the object was successfully updated
   */
  public boolean updateEditorObject(EditorObject updatedObject) {
    Objects.requireNonNull(updatedObject, "Updated object cannot be null.");
    UUID id = updatedObject.getId();
    Objects.requireNonNull(id, "Updated object must have a valid ID.");
    LOG.debug("Attempting to update object {} via EditorLevelData.", id);

    if (level.getEditorObject(id) != null) {
      boolean success = level.updateObjectInDataMap(id, updatedObject);
      if (!success) {
        LOG.error("Object {} exists but failed to update in data map (EditorLevelData issue?).",
            id);
      }
      return success;
    } else {
      LOG.warn("Object {} not found for update.", id);
      return false;
    }
  }

  /**
   * Adds a new layer to the editor level with a specified name.
   *
   * @param layerName the name of the layer to add.
   */
  public void addLayer(String layerName) {
    LOG.debug("Adding layer '{}' via EditorLevelData.", layerName);
    int newPriority = 0;
    if (!level.getLayers().isEmpty()) {
      newPriority = level.getLayers().stream().mapToInt(Layer::getPriority).max().orElse(0) + 1;
    }
    level.addLayer(new Layer(layerName, newPriority));
  }

  /**
   * Retrieves the list of Layers in the editor level.
   *
   * @return a list of {@link Layer} objects.
   */
  public List<Layer> getLayers() {
    return level.getLayers();
  }

  /**
   * Removes the layer identified by the given layer name from the editor level.
   *
   * @param layerName the name of the layer to remove.
   * @return true if the layer was removed; false otherwise
   */
  public boolean removeLayer(String layerName) {
    LOG.debug("Removing layer '{}' via EditorLevelData.", layerName);
    return level.removeLayer(layerName);
  }

  /**
   * Adds a new group to the editor level with the specified group name.
   *
   * @param groupName the name of the group to add.
   */
  public void addGroup(String groupName) {
    LOG.debug("Adding group '{}' via EditorLevelData.", groupName);
    level.addGroup(groupName);
  }

  /**
   * Retrieves the list of groups in the editor level.
   *
   * @return a list of group names.
   */
  public List<String> getGroups() {
    return level.getGroups();
  }

  /**
   * Removes the group identified by the provided group name from the editor level.
   *
   * @param groupName the name of the group to remove.
   * @return true if the group was successfully removed, false if any editor object is still
   * associated with it
   */
  public boolean removeGroup(String groupName) {
    LOG.debug("Removing group '{}' via EditorLevelData.", groupName);
    return level.removeGroup(groupName);
  }

  /**
   * Gets the {@link EditorLevelData} instance of the API.
   *
   * @return the current {@link EditorLevelData}.
   */
  public EditorLevelData getLevel() {
    return level;
  }

  /**
   * Retrieves the {@link IdentityDataManager} used by the editor.
   *
   * @return the IdentityDataManager instance.
   */
  public IdentityDataManager getIdentityDataAPI() {
    return identityAPI;
  }

  /**
   * Retrieves the {@link HitboxDataManager} used by the editor.
   *
   * @return the HitboxDataManager instance.
   */
  public HitboxDataManager getHitboxDataAPI() {
    return hitboxAPI;
  }

  /**
   * Retrieves the {@link InputDataManager} used by the editor to manage input data and events.
   *
   * @return the InputDataManager instance.
   */
  public InputDataManager getInputDataAPI() {
    return inputAPI;
  }

  /**
   * Retrieves the {@link PhysicsDataManager} used by the editor.
   *
   * @return the PhysicsDataManager instance.
   */
  public PhysicsDataManager getPhysicsDataAPI() {
    return physicsAPI;
  }

  /**
   * Retrieves the {@link CollisionDataManager} used by the editor.
   *
   * @return the CollisionDataManager instance.
   */
  public CollisionDataManager getCollisionDataAPI() {
    return collisionAPI;
  }

  /**
   * Retrieves the {@link SpriteDataManager} used by the editor.
   *
   * @return the SpriteDataManager instance.
   */
  public SpriteDataManager getSpriteDataAPI() {
    return spriteAPI;
  }

  /**
   * Gets the container holding dynamic variables for this instance of the API.
   *
   * @return the {@link DynamicVariableContainer} instance.
   */
  public DynamicVariableContainer getDynamicVariableContainer() {
    return dynamicVariableContainer;
  }

  /**
   * Sets the current game directory path.
   *
   * @param path The path to the current game directory.
   */
  public void setCurrentGameDirectoryPath(String path) {
    this.currentGameDirectoryPath = path;
  }

  /**
   * Gets the current game directory path.
   *
   * @return The path to the current game directory.
   */
  public String getCurrentGameDirectoryPath() {
    return currentGameDirectoryPath;
  }

  /**
   * Gets the CustomEventDataManager instance.
   *
   * @return The CustomEventDataManager instance.
   */
  public CustomEventDataManager getCustomEventDataAPI() {
    return customEventAPI;
  }

  /**
   * Gets the current SpriteSheetDataManager instance.
   *
   * @return The current SpriteSheetDataManager instance
   */
  public SpriteSheetDataManager getSpriteSheetDataAPI() {
    return spriteSheetAPI;
  }

  /**
   * Notifies all registered view listeners that an error has occurred, providing a descriptive
   * message.
   *
   * @param errorMessage the error message to be reported to the listeners.
   */
  public void notifyErrorOccurred(String errorMessage) {
    LOG.error("Error occurred in EditorDataAPI: {}", errorMessage);
  }

  /**
   * Returns the current sprite library for the current level.
   *
   * @return the sprite library for the current level
   */
  public SpriteSheetLibrary getSpriteLibrary() {
    return level.getSpriteLibrary();
  }

  /**
   * Returns a list of all the EditorObject instances in the current data mapping for the level
   *
   * @return the map of object UUID to EditorObject
   */
  public Map<UUID, EditorObject> getObjectDataMap() {
    return level.getObjectDataMap();
  }

  /**
   * Adds a sprite template to the sprite template mapping for the current level.
   *
   * @param spriteTemplate The sprite template to add to the level mapping
   */
  public void addSpriteTemplate(SpriteTemplate spriteTemplate) {
    level.addSpriteTemplate(spriteTemplate);
  }

  /**
   * Gets the sprite template map from the current level
   *
   * @return the {@link SpriteTemplateMap} for the current level
   */
  public SpriteTemplateMap getSpriteTemplateMap() {
    return level.getSpriteTemplateMap();
  }

  public String getGameName() {
    return level.getGameName();
  }
}