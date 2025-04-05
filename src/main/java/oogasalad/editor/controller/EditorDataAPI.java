package oogasalad.editor.controller;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.object.DynamicVariableContainer; // Import needed
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EditorDataAPI {

  private static final Logger LOG = LogManager.getLogger(EditorDataAPI.class);

  private final IdentityDataManager identityAPI;
  private final HitboxDataManager hitboxAPI;
  private final InputDataManager inputAPI;
  private final PhysicsDataManager physicsAPI;
  private final CollisionDataManager collisionAPI;
  private final SpriteDataManager spriteAPI;
  private final EditorLevelData level;
  private final DynamicVariableContainer dynamicVariableContainer; // Field already exists


  public EditorDataAPI(){
    this.level = new EditorLevelData();
    this.identityAPI = new IdentityDataManager(level);
    this.hitboxAPI = new HitboxDataManager(level);
    this.inputAPI = new InputDataManager(level); // InputDataManager manages InputData/EditorEvents
    this.physicsAPI = new PhysicsDataManager(level);
    this.collisionAPI = new CollisionDataManager(level);
    this.spriteAPI = new SpriteDataManager(level);
    this.dynamicVariableContainer = new DynamicVariableContainer(); // Field already initialized
    LOG.info("EditorDataAPI initialized with new EditorLevelData.");
  }

  // --- Core Object Methods (Including previous fixes) ---
  public UUID createEditorObject() {
    UUID newId = level.createEditorObject();
    LOG.debug("Created new EditorObject via EditorLevelData, ID: {}", newId);
    return newId;
  }

  public EditorObject getEditorObject(UUID id) {
    return level.getEditorObject(id);
  }

  public boolean removeEditorObject(UUID id) {
    Objects.requireNonNull(id, "Object ID cannot be null for removal.");
    LOG.debug("Attempting to remove object {} via EditorLevelData.", id);
    EditorObject removedObject = level.removeObjectById(id);

    if (removedObject != null) {
      Layer objectLayer = removedObject.getIdentityData().getLayer();
      if (objectLayer != null) {
        boolean removedFromLayer = level.removeObjectFromLayer(objectLayer, removedObject);
        if (!removedFromLayer) {
          LOG.warn("Object {} removed from main map but not found in expected layer {} map.", id, objectLayer.getName());
        }
      } else {
        LOG.warn("Removed object {} had no layer information.", id);
      }
      return true;
    }
    return false;
  }

  public boolean updateEditorObject(EditorObject updatedObject) {
    Objects.requireNonNull(updatedObject, "Updated object cannot be null.");
    UUID id = updatedObject.getId();
    Objects.requireNonNull(id, "Updated object must have a valid ID.");
    LOG.debug("Attempting to update object {} via EditorLevelData.", id);

    if (level.getEditorObject(id) != null) {
      boolean success = level.updateObjectInDataMap(id, updatedObject);
      if (!success) {
        LOG.error("Object {} exists but failed to update in data map (EditorLevelData issue?).", id);
      }
      return success;
    } else {
      LOG.warn("Object {} not found for update.", id);
      return false;
    }
  }


  public void addLayer(String layerName) {
    LOG.debug("Adding layer '{}' via EditorLevelData.", layerName);
    int newPriority = 0;
    if (!level.getLayers().isEmpty()) {
      newPriority = level.getLayers().stream().mapToInt(Layer::getPriority).max().orElse(-1) + 1;
    }
    level.addLayer(new Layer(layerName, newPriority));
  }

  public List<Layer> getLayers() {
    return level.getLayers();
  }

  public void removeLayer(String layerName) {
    LOG.debug("Removing layer '{}' via EditorLevelData.", layerName);
    level.removeLayer(layerName);
  }

  public void addGroup(String groupName) {
    LOG.debug("Adding group '{}' via EditorLevelData.", groupName);
    level.addGroup(groupName);
  }

  public List<String> getGroups() {
    return level.getGroups();
  }

  public void removeGroup(String groupName) {
    LOG.debug("Removing group '{}' via EditorLevelData.", groupName);
    level.removeGroup(groupName);
  }

  public EditorLevelData getLevel() { return level; }
  public IdentityDataManager getIdentityDataAPI() { return identityAPI; }
  public HitboxDataManager getHitboxDataAPI() { return hitboxAPI; }
  public InputDataManager getInputDataAPI() { return inputAPI; }
  public PhysicsDataManager getPhysicsDataAPI() { return physicsAPI; }
  public CollisionDataManager getCollisionDataAPI() { return collisionAPI; }
  public SpriteDataManager getSpriteDataAPI() { return spriteAPI; }
  /**
   * Gets the container holding dynamic variables for the editor session.
   * @return The DynamicVariableContainer instance.
   */
  public DynamicVariableContainer getDynamicVariableContainer() {
    return dynamicVariableContainer;
  }
}
