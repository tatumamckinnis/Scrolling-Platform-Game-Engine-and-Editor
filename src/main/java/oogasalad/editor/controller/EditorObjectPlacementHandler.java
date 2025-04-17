package oogasalad.editor.controller;

import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.EditorObjectPopulator;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the placement of new objects (both standard and from prefabs) in the editor.
 */
public class EditorObjectPlacementHandler {

  private static final Logger LOG = LogManager.getLogger(EditorObjectPlacementHandler.class);
  private final EditorDataAPI editorDataAPI;
  private final EditorListenerNotifier notifier;

  /**
   * Constructs an EditorObjectPlacementHandler.
   *
   * @param editorDataAPI The data API to create and modify editor objects.
   * @param notifier      The notifier to signal object additions and errors.
   */
  public EditorObjectPlacementHandler(EditorDataAPI editorDataAPI, EditorListenerNotifier notifier) {
    this.editorDataAPI = editorDataAPI;
    this.notifier = notifier;
  }

  /**
   * Places a new object based on a prefab blueprint at the specified world coordinates.
   * Creates the object, populates it, sets coordinates and name, and notifies listeners.
   *
   * @param prefabData The blueprint data for the prefab.
   * @param worldX     The world x-coordinate for placement.
   * @param worldY     The world y-coordinate for placement.
   * @return The UUID of the newly placed object, or null if placement failed.
   */
  public UUID placePrefab(BlueprintData prefabData, double worldX, double worldY) {
    Objects.requireNonNull(prefabData, "PrefabData cannot be null for placement request");
    LOG.info("Processing prefab placement request: Type='{}', Pos=({},{})", prefabData.type(), worldX, worldY);

    if (!Double.isFinite(worldX) || !Double.isFinite(worldY)) {
      notifier.notifyErrorOccurred("Invalid placement coordinates: (" + worldX + ", " + worldY + ")");
      LOG.warn("Invalid placement coordinates received: ({}, {})", worldX, worldY);
      return null;
    }

    UUID newObjectId = null;
    try {
      newObjectId = createAndPopulateObjectFromPrefab(prefabData);
      setObjectCoordinatesFromPrefab(newObjectId, prefabData, worldX, worldY);
      setUniqueObjectName(newObjectId, prefabData, worldX, worldY);

      LOG.debug("Object {} created and populated from prefab.", newObjectId);
      notifier.notifyObjectAdded(newObjectId);
      return newObjectId;

    } catch (Exception e) {
      LOG.error("Failed during prefab placement: {}", e.getMessage(), e);
      cleanupFailedPlacement(newObjectId);
      notifier.notifyErrorOccurred("Failed to place prefab: " + e.getMessage());
      return null;
    }
  }

  /**
   * Places a new standard object with basic properties at the specified world coordinates.
   * Creates the object, sets initial properties (name, group, position, size), and notifies listeners.
   *
   * @param objectGroup      The group name for the new object.
   * @param objectNamePrefix The prefix for generating the object's name.
   * @param worldX           The world x-coordinate for placement.
   * @param worldY           The world y-coordinate for placement.
   * @param cellSize         The default size (width/height) for the object's hitbox.
   * @return The UUID of the newly placed object, or null if placement failed.
   */
  public UUID placeStandardObject(String objectGroup, String objectNamePrefix, double worldX,
      double worldY, int cellSize) {
    LOG.info("Processing object placement request: Group='{}', Prefix='{}', Pos=({},{})",
        objectGroup, objectNamePrefix, worldX, worldY);
    UUID newObjectId = null;
    try {
      newObjectId = editorDataAPI.createEditorObject();
      setInitialObjectProperties(newObjectId, objectGroup, objectNamePrefix, worldX, worldY, cellSize);
      LOG.debug("Object {} data set via EditorDataAPI.", newObjectId);
      notifier.notifyObjectAdded(newObjectId);
      return newObjectId;
    } catch (Exception e) {
      LOG.error("Failed during object placement: {}", e.getMessage(), e);
      cleanupFailedPlacement(newObjectId);
      notifier.notifyErrorOccurred("Failed to place object: " + e.getMessage());
      return null;
    }
  }

  private UUID createAndPopulateObjectFromPrefab(BlueprintData prefabData) throws IllegalStateException {
    UUID newObjectId = editorDataAPI.createEditorObject();
    EditorObject newObject = editorDataAPI.getEditorObject(newObjectId);

    if (newObject == null) {
      throw new IllegalStateException("Failed to create or retrieve new EditorObject with ID: " + newObjectId);
    }
    EditorObjectPopulator.populateFromBlueprint(newObject, prefabData, editorDataAPI);
    return newObjectId;
  }

  private void setObjectCoordinatesFromPrefab(UUID objectId, BlueprintData prefabData, double worldX, double worldY) {
    int floorWorldX = (int) Math.floor(worldX);
    int floorWorldY = (int) Math.floor(worldY);

    editorDataAPI.getSpriteDataAPI().setX(objectId, floorWorldX);
    editorDataAPI.getSpriteDataAPI().setY(objectId, floorWorldY);

    int hitboxX = floorWorldX;
    int hitboxY = floorWorldY;
    if (prefabData.hitBoxData() != null) {
      hitboxX += prefabData.hitBoxData().spriteDx();
      hitboxY += prefabData.hitBoxData().spriteDy();
    }
    editorDataAPI.getHitboxDataAPI().setX(objectId, hitboxX);
    editorDataAPI.getHitboxDataAPI().setY(objectId, hitboxY);
  }

  private void setUniqueObjectName(UUID objectId, BlueprintData prefabData, double worldX, double worldY) {
    int floorWorldX = (int) Math.floor(worldX);
    int floorWorldY = (int) Math.floor(worldY);
    String uniqueName = String.format("%s_%d_%d_%s",
        prefabData.type(), floorWorldX, floorWorldY,
        UUID.randomUUID().toString().substring(0, 4));
    editorDataAPI.getIdentityDataAPI().setName(objectId, uniqueName);
  }

  private void setInitialObjectProperties(UUID objectId, String group, String namePrefix, double worldX, double worldY, int cellSize) {
    int floorWorldX = (int) Math.floor(worldX);
    int floorWorldY = (int) Math.floor(worldY);
    String objectName = String.format("%s%d_%d", namePrefix, floorWorldX, floorWorldY);

    editorDataAPI.getIdentityDataAPI().setName(objectId, objectName);
    editorDataAPI.getIdentityDataAPI().setGroup(objectId, group);
    editorDataAPI.getSpriteDataAPI().setX(objectId, floorWorldX);
    editorDataAPI.getSpriteDataAPI().setY(objectId, floorWorldY);
    editorDataAPI.getHitboxDataAPI().setX(objectId, floorWorldX);
    editorDataAPI.getHitboxDataAPI().setY(objectId, floorWorldY);
    editorDataAPI.getHitboxDataAPI().setWidth(objectId, cellSize);
    editorDataAPI.getHitboxDataAPI().setHeight(objectId, cellSize);
    editorDataAPI.getHitboxDataAPI().setShape(objectId, "RECTANGLE");
  }

  private void cleanupFailedPlacement(UUID objectId) {
    if (objectId != null) {
      try {
        editorDataAPI.removeEditorObject(objectId);
        LOG.info("Cleaned up object {} after placement failure.", objectId);
      } catch (Exception removeEx) {
        LOG.error("Failed to clean up object {} after placement error: {}", objectId, removeEx.getMessage(), removeEx);
      }
    }
  }
}
