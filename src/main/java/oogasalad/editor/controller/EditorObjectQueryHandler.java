package oogasalad.editor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.model.data.EditorObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles querying editor objects, including retrieval by ID and finding objects at specific coordinates.
 */
public class EditorObjectQueryHandler {

  private static final Logger LOG = LogManager.getLogger(EditorObjectQueryHandler.class);
  private final EditorDataAPI editorDataAPI;

  /**
   * Constructs an EditorObjectQueryHandler.
   *
   * @param editorDataAPI The data API to access editor object information.
   */
  public EditorObjectQueryHandler(EditorDataAPI editorDataAPI) {
    this.editorDataAPI = editorDataAPI;
  }

  /**
   * Retrieves the EditorObject associated with the specified UUID from the data API.
   *
   * @param objectId The UUID of the editor object to be retrieved.
   * @return The corresponding EditorObject, or null if not found, ID is null, or an error occurs.
   */
  public EditorObject getEditorObject(UUID objectId) {
    if (objectId == null) {
      LOG.trace("getEditorObject called with null ID.");
      return null;
    }
    try {
      return editorDataAPI.getEditorObject(objectId);
    } catch (Exception e) {
      LOG.error("Error retrieving object {}: {}", objectId, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Retrieves the UUID of the highest-priority object located at the specified world coordinates.
   * Checks object hitboxes and considers layer priority for overlapping objects.
   *
   * @param worldX The world X coordinate to check.
   * @param worldY The world Y coordinate to check.
   * @return The UUID of the object at the location, or null if no object exists there or an error occurs.
   */
  public UUID getObjectIDAt(double worldX, double worldY) {
    try {
      List<UUID> hitCandidates = findHittingObjects(worldX, worldY);
      sortCandidatesByLayerPriority(hitCandidates);
      return hitCandidates.isEmpty() ? null : hitCandidates.get(0);
    } catch (Exception e) {
      LOG.error("Error checking object ID at ({}, {}): {}", worldX, worldY, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Finds all objects whose hitboxes contain the given world coordinates.
   *
   * @param worldX The world X coordinate.
   * @param worldY The world Y coordinate.
   * @return A list of UUIDs of objects hit at the specified coordinates.
   */
  private List<UUID> findHittingObjects(double worldX, double worldY) {
    List<UUID> hitCandidates = new ArrayList<>();
    Map<UUID, EditorObject> objectMap = editorDataAPI.getLevel().getObjectDataMap();
    if (objectMap == null) {
      LOG.warn("Object data map is null, cannot find hitting objects.");
      return hitCandidates;
    }

    for (Map.Entry<UUID, EditorObject> entry : objectMap.entrySet()) {
      UUID id = entry.getKey();
      EditorObject obj = entry.getValue();
      if (objectCollidesWithPoint(obj, worldX, worldY)) {
        hitCandidates.add(id);
      }
    }
    return hitCandidates;
  }

  /**
   * Sorts a list of object UUIDs based on their layer priority in descending order (highest priority first).
   *
   * @param candidates The list of UUIDs to sort.
   */
  private void sortCandidatesByLayerPriority(List<UUID> candidates) {
    try {
      candidates.sort((a, b) ->
          Integer.compare(editorDataAPI.getIdentityDataAPI().getLayerPriority(b),
              editorDataAPI.getIdentityDataAPI().getLayerPriority(a))
      );
    } catch (Exception e) {
      LOG.error("Error sorting candidates by layer priority: {}", e.getMessage(), e);
    }
  }

  /**
   * Checks if an object's hitbox contains the given world coordinates.
   * Assumes hitbox coordinates represent the top-left corner.
   *
   * @param obj    The EditorObject to check. Can be null.
   * @param worldX The world X coordinate.
   * @param worldY The world Y coordinate.
   * @return true if the point is within the object's hitbox, false otherwise or if object/hitbox is null.
   */
  private boolean objectCollidesWithPoint(EditorObject obj, double worldX, double worldY) {
    if (obj == null || obj.getHitboxData() == null) {
      return false;
    }
    try {
      double x = obj.getHitboxData().getX();
      double y = obj.getHitboxData().getY();
      int width = obj.getHitboxData().getWidth();
      int height = obj.getHitboxData().getHeight();

      return (worldX >= x && worldX < x + width) && (worldY >= y && worldY < y + height);
    } catch (Exception e) {
      LOG.error("Error checking collision for object {}: {}", obj.getId(), e.getMessage(), e);
      return false;
    }
  }
}
