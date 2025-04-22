package oogasalad.editor.controller;

import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.EditorObjectPopulator;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.fileparser.records.BlueprintData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the placement of new objects (both standard and from prefabs) in the editor.
 *
 * @author Tatum McKinnis
 */
public class EditorObjectPlacementHandler {

  private static final Logger LOG = LogManager.getLogger(EditorObjectPlacementHandler.class);
  private final EditorDataAPI editorDataAPI;
  private final EditorListenerNotifier notifier;
  private final EditorObjectPopulator objectPopulator;

  /**
   * Constructs an EditorObjectPlacementHandler.
   *
   * @param editorDataAPI The data API providing access to level data and sub-APIs.
   * @param notifier      The notifier to signal object additions and errors.
   */
  public EditorObjectPlacementHandler(EditorDataAPI editorDataAPI, EditorListenerNotifier notifier) {
    this.editorDataAPI = Objects.requireNonNull(editorDataAPI, "EditorDataAPI cannot be null");
    this.notifier = Objects.requireNonNull(notifier, "Notifier cannot be null");

    this.objectPopulator = new EditorObjectPopulator(editorDataAPI.getLevel());
    LOG.info("EditorObjectPlacementHandler initialized.");
  }

  /**
   * Places a new object based on a prefab blueprint at the specified world coordinates.
   * Uses EditorObjectPopulator to create and populate the object.
   */
  public UUID placePrefab(BlueprintData prefabData, double worldX, double worldY) {
    Objects.requireNonNull(prefabData, "PrefabData cannot be null for placement");
    LOG.info("Processing prefab placement request: Type='{}', Pos=({},{})", prefabData.type(), worldX, worldY);

    if (!Double.isFinite(worldX) || !Double.isFinite(worldY)) {
      notifier.notifyErrorOccurred("Invalid placement coordinates: (" + worldX + ", " + worldY + ")");
      LOG.warn("Invalid placement coordinates received: ({}, {})", worldX, worldY);
      return null;
    }

    EditorObject newObject = null;
    UUID newObjectId = null;
    try {

      newObject = objectPopulator.populateFromBlueprint(prefabData, worldX, worldY);
      newObjectId = newObject.getId();



      setObjectCoordinatesFromPrefab(newObjectId, prefabData, worldX, worldY);
      setUniqueObjectName(newObjectId, prefabData, worldX, worldY);

      LOG.debug("Object {} created and populated from prefab.", newObjectId);
      notifier.notifyObjectAdded(newObjectId);
      return newObjectId;

    } catch (Exception e) {
      LOG.error("Failed during prefab placement (Object attempting to create: {}): {}", prefabData.type(), e.getMessage(), e);


      notifier.notifyErrorOccurred("Failed to place prefab '" + prefabData.type() + "': " + e.getMessage());
      return null;
    }
  }

  /**
   * Places a new standard object with basic properties at the specified world coordinates.
   */
  public UUID placeStandardObject(String objectGroup, String objectNamePrefix, double worldX, double worldY, int cellSize) {
    LOG.info("Processing standard object placement: Group='{}', Prefix='{}', Pos=({},{})", objectGroup, objectNamePrefix, worldX, worldY);
    UUID newObjectId = null;
    try {

      EditorObject defaultObject = objectPopulator.createDefaultObject();
      newObjectId = defaultObject.getId();


      setInitialObjectProperties(newObjectId, objectGroup, objectNamePrefix, worldX, worldY, cellSize);

      LOG.debug("Standard object {} created and properties set.", newObjectId);
      notifier.notifyObjectAdded(newObjectId);
      return newObjectId;
    } catch (Exception e) {
      LOG.error("Failed during standard object placement: {}", e.getMessage(), e);

      notifier.notifyErrorOccurred("Failed to place standard object: " + e.getMessage());
      return null;
    }
  }


  private void setObjectCoordinatesFromPrefab(UUID objectId, BlueprintData prefabData, double worldX, double worldY) {
    int floorWorldX = (int) Math.floor(worldX);
    int floorWorldY = (int) Math.floor(worldY);



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
    editorDataAPI.getIdentityDataAPI().setType(objectId, group);


    editorDataAPI.getSpriteDataAPI().setX(objectId, floorWorldX);
    editorDataAPI.getSpriteDataAPI().setY(objectId, floorWorldY);

    editorDataAPI.getHitboxDataAPI().setX(objectId, floorWorldX);
    editorDataAPI.getHitboxDataAPI().setY(objectId, floorWorldY);
    editorDataAPI.getHitboxDataAPI().setWidth(objectId, cellSize);
    editorDataAPI.getHitboxDataAPI().setHeight(objectId, cellSize);
    editorDataAPI.getHitboxDataAPI().setShape(objectId, "RECTANGLE");
  }


}