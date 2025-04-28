package oogasalad.editor.controller.level;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import oogasalad.editor.controller.asset.SpriteSheetDataManager;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.controller.object.CollisionDataManager;
import oogasalad.editor.controller.object.CustomEventDataManager;
import oogasalad.editor.controller.object.HitboxDataManager;
import oogasalad.editor.controller.object.IdentityDataManager;
import oogasalad.editor.controller.object.InputDataManager;
import oogasalad.editor.controller.object.PhysicsDataManager;
import oogasalad.editor.controller.object.SpriteDataManager;
import oogasalad.editor.model.EditorFileConverter;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.SpriteSheetLibrary;
import oogasalad.editor.model.data.SpriteTemplateMap;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import oogasalad.editor.model.loader.LevelDataConverter;
import oogasalad.editor.model.saver.api.EditorFileConverterAPI;
import oogasalad.exceptions.EditorLoadException;
import oogasalad.exceptions.EditorSaveException;
import oogasalad.fileparser.DefaultFileParser;
import oogasalad.fileparser.FileParserApi;
import oogasalad.filesaver.savestrategy.SaverStrategy;
import oogasalad.filesaver.savestrategy.XmlStrategy;

/**
 * Provides a comprehensive API to manage editor data, acting as a facade for various underlying
 * data managers and the core {@link EditorLevelData}. It handles operations related to editor
 * objects (creation, retrieval, update, removal), layers, groups, sprite assets (sheets,
 * templates), camera settings, custom object parameters, and level saving/loading integration. This
 * API simplifies interaction with the editor's data model for the controller layer.
 *
 * @author Jacob You
 */
public class EditorDataAPI {

  private static final Logger LOG = LogManager.getLogger(EditorDataAPI.class);
  private static final FileParserApi DEFAULT_FILE_PARSER = new DefaultFileParser();
  private static final SaverStrategy DEFAULT_SAVER_STRATEGY = new XmlStrategy();
  private static final String GAME_PATH = "data/gameData";


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
  private final CameraDataManager cameraAPI;
  private final FileParserApi fileParserAPI;
  private final SaverStrategy saverStrategy;
  private final LevelDataConverter levelDataConverter;
  private final EditorListenerNotifier listenerNotifier;
  private String currentGameName;

  /**
   * Constructs an EditorDataAPI instance, initializing the underlying {@link EditorLevelData} and
   * all related data managers, including the modified {@link IdentityDataManager} which now handles
   * per-object parameters.
   *
   * @param listenerNotifier The notifier for broadcasting changes to view listeners. Must not be
   *                         null.
   * @author Jacob You, Tatum McKinnis
   */
  public EditorDataAPI(EditorListenerNotifier listenerNotifier) {
    this.fileParserAPI = DEFAULT_FILE_PARSER;
    this.saverStrategy = DEFAULT_SAVER_STRATEGY;

    this.listenerNotifier = Objects.requireNonNull(listenerNotifier,
        "ListenerNotifier cannot be null");
    this.level = new EditorLevelData();
    this.identityAPI = new IdentityDataManager(level); // Handles identity + parameters
    this.hitboxAPI = new HitboxDataManager(level);
    this.inputAPI = new InputDataManager(level);
    this.physicsAPI = new PhysicsDataManager(level);
    this.collisionAPI = new CollisionDataManager(level);
    this.spriteAPI = new SpriteDataManager(level, listenerNotifier);
    this.customEventAPI = new CustomEventDataManager(level);
    this.cameraAPI = new CameraDataManager(level);
    this.dynamicVariableContainer = new DynamicVariableContainer(); // Still exists but not used for object params
    this.spriteSheetAPI = new SpriteSheetDataManager(level, saverStrategy, fileParserAPI);

    this.fileConverterAPI = new EditorFileConverter();
    this.levelDataConverter = new LevelDataConverter();
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
   * @param id the unique identifier of the editor object. Can be null.
   * @return the corresponding {@link EditorObject} or null if no object is found or ID is null.
   */
  public EditorObject getEditorObject(UUID id) {
    if (id == null) {
      return null;
    }
    return level.getEditorObject(id);
  }

  /**
   * Removes the editor object corresponding to the provided UUID. Also attempts to remove the
   * object from its associated layer.
   *
   * @param id the unique identifier of the editor object to be removed. Must not be null.
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
   * Updates an existing editor object with new data provided in the {@code updatedObject}. The
   * object is identified by the ID within {@code updatedObject}.
   *
   * @param updatedObject the editor object containing the updated data. Must not be null and must
   *                      have a valid ID.
   * @return whether the object was successfully found and updated in the underlying data map.
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
   * Adds a new layer to the editor level with a specified name and the next available priority.
   *
   * @param layerName the name of the layer to add. Must not be null or empty.
   */
  public void addLayer(String layerName) {
    Objects.requireNonNull(layerName, "Layer name cannot be null");
    if (layerName.trim().isEmpty()) {
      LOG.warn("Attempted to add a layer with an empty name.");
      return;
    }
    LOG.debug("Adding layer '{}' via EditorLevelData.", layerName);
    int newPriority = 0;
    if (!level.getLayers().isEmpty()) {
      newPriority = level.getLayers().stream().mapToInt(Layer::getPriority).max().orElse(0) + 1;
    }
    level.addLayer(new Layer(layerName, newPriority));
  }

  /**
   * Retrieves the list of Layers currently defined in the editor level.
   *
   * @return an unmodifiable list of {@link Layer} objects.
   */
  public List<Layer> getLayers() {
    return level.getLayers();
  }

  /**
   * Removes the layer identified by the given layer name from the editor level.
   *
   * @param layerName the name of the layer to remove.
   * @return true if the layer was found and removed; false otherwise.
   */
  public boolean removeLayer(String layerName) {
    LOG.debug("Removing layer '{}' via EditorLevelData.", layerName);
    return level.removeLayer(layerName);
  }

  /**
   * Adds a new group (category/type name) to the editor level's list of known groups.
   *
   * @param groupName the name of the group to add. Must not be null or empty.
   */
  public void addGroup(String groupName) {
    Objects.requireNonNull(groupName, "Group name cannot be null");
    if (groupName.trim().isEmpty()) {
      LOG.warn("Attempted to add a group with an empty name.");
      return;
    }
    LOG.debug("Adding group '{}' via EditorLevelData.", groupName);
    level.addGroup(groupName);
  }

  /**
   * Retrieves the list of all currently defined groups in the editor level.
   *
   * @return an unmodifiable list of group names.
   */
  public List<String> getGroups() {
    return level.getGroups();
  }

  /**
   * Removes the group identified by the provided group name from the editor level's list. Fails if
   * any existing editor object is still assigned to this group.
   *
   * @param groupName the name of the group to remove.
   * @return true if the group was successfully removed (and no objects used it), false otherwise.
   */
  public boolean removeGroup(String groupName) {
    LOG.debug("Removing group '{}' via EditorLevelData.", groupName);
    return level.removeGroup(groupName);
  }

  /**
   * Gets the underlying {@link EditorLevelData} instance managed by this API. Use with caution,
   * prefer using specific API methods when possible.
   *
   * @return the current {@link EditorLevelData}.
   */
  public EditorLevelData getLevel() {
    return level;
  }

  /**
   * Retrieves the {@link IdentityDataManager} used by the editor, which handles object identity
   * (name, group, layer) and custom parameters.
   *
   * @return the IdentityDataManager instance.
   */
  public IdentityDataManager getIdentityDataAPI() {
    return identityAPI;
  }

  /**
   * Retrieves the {@link HitboxDataManager} used by the editor to manage object hitbox properties.
   *
   * @return the HitboxDataManager instance.
   */
  public HitboxDataManager getHitboxDataAPI() {
    return hitboxAPI;
  }

  /**
   * Retrieves the {@link InputDataManager} used by the editor to manage input-related events and
   * data.
   *
   * @return the InputDataManager instance.
   */
  public InputDataManager getInputDataAPI() {
    return inputAPI;
  }

  /**
   * Retrieves the {@link PhysicsDataManager} used by the editor to manage object physics
   * properties.
   *
   * @return the PhysicsDataManager instance.
   */
  public PhysicsDataManager getPhysicsDataAPI() {
    return physicsAPI;
  }

  /**
   * Retrieves the {@link CollisionDataManager} used by the editor to manage collision-related
   * events and data.
   *
   * @return the CollisionDataManager instance.
   */
  public CollisionDataManager getCollisionDataAPI() {
    return collisionAPI;
  }

  /**
   * Retrieves the {@link SpriteDataManager} used by the editor to manage object sprite properties
   * and templates.
   *
   * @return the SpriteDataManager instance.
   */
  public SpriteDataManager getSpriteDataAPI() {
    return spriteAPI;
  }

  /**
   * Gets the container holding global dynamic variables for this instance of the API. Note: This is
   * separate from the per-object parameters managed by IdentityDataManager.
   *
   * @return the {@link DynamicVariableContainer} instance.
   */
  public DynamicVariableContainer getDynamicVariableContainer() {
    return dynamicVariableContainer;
  }

  /**
   * Sets the file system path to the root directory of the currently loaded game project. Also
   * updates the game name in the underlying {@link EditorLevelData} based on the directory name
   * extracted from the path.
   *
   * @param name The name of the current game
   */
  public void setCurrentGameName(String name) {
    this.currentGameName = name;
    level.setGameName(name); // Use the existing setter
    LOG.info("Game name set to: {}", name);
  }

  /**
   * Returns the on-disk path to the current game directory:
   * <pre>data/gameData/&lt;gameName&gt;</pre>
   * If the game name has not been set yet, the method returns {@code null}.
   *
   * @return full path string including the game name, or {@code null} when undefined
   */
  public String getGamePath() {
    String gameName = level.getGameName();
    if (gameName == null || gameName.isBlank()) {
      LOG.warn("Game name has not been set – cannot build game path.");
      return null;
    }
    return GAME_PATH + File.separator + gameName;
  }

  /**
   * Gets the {@link CustomEventDataManager} instance for managing custom event definitions.
   *
   * @return The CustomEventDataManager instance.
   */
  public CustomEventDataManager getCustomEventDataAPI() {
    return customEventAPI;
  }

  /**
   * Gets the {@link SpriteSheetDataManager} instance for managing sprite sheet assets (loading,
   * saving).
   *
   * @return The current SpriteSheetDataManager instance.
   */
  public SpriteSheetDataManager getSpriteSheetDataAPI() {
    return spriteSheetAPI;
  }

  /**
   * Notifies all registered view listeners that an error has occurred, providing a descriptive
   * message. This is typically called by data managers when operations fail.
   *
   * @param errorMessage the error message to be reported to the listeners.
   */
  public void notifyErrorOccurred(String errorMessage) {
    LOG.error("Error reported via EditorDataAPI: {}", errorMessage);
    listenerNotifier.notifyErrorOccurred(errorMessage);
  }

  /**
   * Returns the current {@link SpriteSheetLibrary} containing loaded sprite sheet data for the
   * level.
   *
   * @return the sprite library for the current level.
   */
  public SpriteSheetLibrary getSpriteLibrary() {
    return level.getSpriteLibrary();
  }

  /**
   * Returns an unmodifiable view of the map containing all {@link EditorObject} instances in the
   * current level, keyed by their UUID.
   *
   * @return an unmodifiable map of object UUID to EditorObject.
   */
  public Map<UUID, EditorObject> getObjectDataMap() {
    return level.getObjectDataMap();
  }

  /**
   * Adds a {@link SpriteTemplate} to the sprite template mapping for the current level. Notifies
   * listeners that the sprite templates have changed.
   *
   * @param spriteTemplate The sprite template to add to the level mapping. Must not be null.
   */
  public void addSpriteTemplate(SpriteTemplate spriteTemplate) {
    Objects.requireNonNull(spriteTemplate, "SpriteTemplate cannot be null");
    level.addSpriteTemplate(spriteTemplate);
    listenerNotifier.notifySpriteTemplateChanged();
  }

  /**
   * Gets the {@link SpriteTemplateMap} containing all defined sprite templates for the current
   * level.
   *
   * @return the {@link SpriteTemplateMap} for the current level.
   */
  public SpriteTemplateMap getSpriteTemplateMap() {
    return level.getSpriteTemplateMap();
  }

  /**
   * Returns the name of the game associated with the current level, typically derived from the game
   * directory path.
   *
   * @return the game name as a {@code String}, or null if not set.
   */
  public String getGameName() {
    return level.getGameName();
  }

  /**
   * Saves the current editor level data (including objects, layers, groups, camera, parameters,
   * etc.) to a file using the configured file converter and saver strategy.
   *
   * @param fileName the name (or path) of the file to save to.
   * @throws EditorSaveException if an error occurs during the saving process.
   */
  public void saveLevelData(String fileName) throws EditorSaveException {
    LOG.info("Saving level data to file: {}", fileName);
    fileConverterAPI.saveEditorDataToFile(level, fileName, saverStrategy);
  }

  /**
   * Returns the {@link CameraDataManager} for interacting with the level's camera data. The
   * {@code camType} parameter is currently ignored but kept for potential future use with multiple
   * camera types.
   *
   * @param camType the camera type requested (currently ignored).
   * @return the {@link CameraDataManager} instance.
   */
  public CameraDataManager getCameraAPI(String camType) {
    // Currently ignores camType, always returns the single camera manager
    return cameraAPI;
  }

  /**
   * Loads level data from the specified file and updates the editor's object map.
   *
   * <p>This method parses the level file using the {@code levelDataConverter},
   * repopulates the level with game objects, and notifies listeners of each newly added
   * object.</p>
   *
   * @param fileName the path to the level file to load
   * @throws EditorLoadException if an error occurs during file loading or parsing
   */
  public void loadLevelData(String fileName) throws EditorLoadException {
    levelDataConverter.loadLevelData(this, level, fileConverterAPI, fileName);
    getObjectDataMap().forEach((key, value) -> {
      listenerNotifier.notifyObjectAdded(key);
    });
  }

  /**
   * Gets a list of available game names by examining directories.
   * 
   * @return a List of game names found in the game directories
   */
  public List<String> getGames() {
    Set<String> gameNames = new HashSet<>();
    
    // Check in levels directory
    String levelDirPath = System.getProperty("user.dir") + "/data/gameData/levels";
    File levelDir = new File(levelDirPath);
    if (levelDir.exists() && levelDir.isDirectory()) {
      File[] gameDirs = levelDir.listFiles(File::isDirectory);
      if (gameDirs != null) {
        for (File gameDir : gameDirs) {
          gameNames.add(gameDir.getName());
        }
      }
    }
    
    // Check in graphics directory
    String graphicsDirPath = System.getProperty("user.dir") + "/data/graphicsData";
    File graphicsDir = new File(graphicsDirPath);
    if (graphicsDir.exists() && graphicsDir.isDirectory()) {
      File[] gameDirs = graphicsDir.listFiles(File::isDirectory);
      if (gameDirs != null) {
        for (File gameDir : gameDirs) {
          // Skip default/problematic directories
          if (!gameDir.getName().equals("unknown_game") && !gameDir.getName().equals("dinosaurgame")) {
            gameNames.add(gameDir.getName());
          }
        }
      }
    }
    
    LOG.info("Found {} game names across all directories", gameNames.size());
    List<String> result = new ArrayList<>(gameNames);
    Collections.sort(result);
    return result;
  }

  /**
   * Gets the layer priority of an editor object by its UUID.
   * Delegates to the corresponding method in {@link EditorLevelData}.
   *
   * @param id the UUID of the editor object
   * @return the priority of the layer the object belongs to, or 0 if the object or its layer is not found
   */
  public int getObjectLayerPriority(UUID id) {
    LOG.debug("Getting layer priority for object with ID: {}", id);
    return level.getObjectLayerPriority(id);
  }

  /**
   * Returns all editor objects sorted by layer priority, from lowest to highest (back to front).
   * This is useful for rendering objects in the correct Z-order in the frontend.
   *
   * @return a list of editor objects sorted by layer priority (ascending)
   */
  public List<EditorObject> getObjectsSortedByLayerPriority() {
    return level.getObjectsSortedByLayerPriority();
  }

  /**
   * Returns all objects on a specific layer.
   * This is useful for operations that need to manipulate or select objects from a particular layer.
   *
   * @param layerName the name of the layer to get objects from
   * @return a list of editor objects on the specified layer, or an empty list if the layer doesn't exist
   */
  public List<EditorObject> getObjectsByLayer(String layerName) {
    return level.getObjectsByLayer(layerName);
  }

  /**
   * Determines if one object is rendered on top of another based on layer priority.
   * Objects with higher layer priority are rendered on top.
   *
   * @param topObjectId UUID of the object to check if it's on top
   * @param bottomObjectId UUID of the object to check if it's on the bottom
   * @return true if topObjectId has higher or equal layer priority than bottomObjectId, false otherwise
   */
  public boolean isObjectOnTopOf(UUID topObjectId, UUID bottomObjectId) {
    if (topObjectId == null || bottomObjectId == null) {
      return false;
    }
    
    int topPriority = getObjectLayerPriority(topObjectId);
    int bottomPriority = getObjectLayerPriority(bottomObjectId);
    
    // Higher priority means the object is rendered later (on top)
    return topPriority >= bottomPriority;
  }
}