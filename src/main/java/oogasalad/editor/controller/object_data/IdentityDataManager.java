package oogasalad.editor.controller.object_data;

import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.Layer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages identity-related data for EditorObjects, including name, group, and layer information.
 * This manager provides a simplified API to interact with the identity aspects of EditorObjects
 * stored in the underlying {@link EditorLevelData}.
 *
 * @author Jacob You
 */
public class IdentityDataManager {

  private EditorLevelData level;
  private static final Logger LOG = LogManager.getLogger(IdentityDataManager.class);


  /**
   * Constructs an IdentityDataManager with the specified EditorLevelData.
   *
   * @param level the EditorLevelData that stores the EditorObjects.
   */
  public IdentityDataManager(EditorLevelData level) {
    this.level = level;
  }

  /**
   * Retrieves the name of the EditorObject associated with the given UUID.
   *
   * @param id the UUID of the EditorObject.
   * @return the name from the object's identity data.
   */
  public String getName(UUID id) {
    return level.getEditorObject(id).getIdentityData().getName();
  }

  /**
   * Retrieves the group of the EditorObject associated with the given UUID.
   *
   * @param id the UUID of the EditorObject.
   * @return the group from the object's identity data.
   */
  public String getGroup(UUID id) {
    return level.getEditorObject(id).getIdentityData().getType();
  }

  /**
   * Sets the name for the EditorObject associated with the given UUID.
   *
   * @param id   the UUID of the EditorObject.
   * @param name the new name to be set in the object's identity data.
   */
  public void setName(UUID id, String name) {
    level.getEditorObject(id).getIdentityData().setName(name);
  }

  /**
   * Sets the group for the EditorObject associated with the given UUID.
   *
   * @param id    the UUID of the EditorObject.
   * @param group the new group to be set in the object's identity data.
   */
  public void setGroup(UUID id, String group) {
    level.getEditorObject(id).getIdentityData().setType(group);
  }

  /**
   * Retrieves the layer priority of the EditorObject associated with the given UUID.
   *
   * @param id the UUID of the EditorObject.
   * @return the priority of the layer that the object belongs to.
   */
  public int getLayerPriority(UUID id) {
    return level.getEditorObject(id).getIdentityData().getLayer().getPriority();
  }

  /**
   * Sets the type of the EditorObject.
   * @param id The UUID of the object.
   * @param type The type to set.
   */
  public void setType(UUID id, String type) {
    level.getEditorObject(id).getIdentityData().setType(type);
  }

  /**
   * Sets the layer of the EditorObject.
   * @param id The UUID of the object.
   * @param layer The layer to set.
   */
  public void setLayer(UUID id, String layer) {
    Layer foundLayer = null;
    for (Layer l : level.getLayers()) {
      if (l.getName().equals(layer)) {
        foundLayer = l;
        break;
      }
    }
    if (foundLayer != null) {
      level.getEditorObject(id).getIdentityData().setLayer(foundLayer);
    } else {
      LOG.error("Layer '{}' not found!", layer);
    }
  }
}