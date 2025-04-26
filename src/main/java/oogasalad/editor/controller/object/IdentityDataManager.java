package oogasalad.editor.controller.object;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.object.EditorObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages identity-related data and custom parameters for EditorObjects, including name, group,
 * layer information, and user-defined string/double key-value pairs. This manager provides a
 * simplified API to interact with these aspects of EditorObjects stored in the underlying
 * {@link EditorLevelData}.
 *
 * @author Jacob You, Tatum McKinnis
 */
public class IdentityDataManager {

  private EditorLevelData level;
  private static final Logger LOG = LogManager.getLogger(IdentityDataManager.class);


  /**
   * Constructs an IdentityDataManager with the specified EditorLevelData.
   *
   * @param level the EditorLevelData that stores the EditorObjects. Must not be null.
   */
  public IdentityDataManager(EditorLevelData level) {
    this.level = Objects.requireNonNull(level, "EditorLevelData cannot be null");
  }

  /**
   * Retrieves the name of the EditorObject associated with the given UUID.
   * Returns an empty string if the object or its identity data is not found.
   *
   * @param id the UUID of the EditorObject. Can be null.
   * @return the name from the object's identity data, or an empty string.
   */
  public String getName(UUID id) {
    EditorObject obj = safeGetObject(id);
    return (obj != null && obj.getIdentityData() != null) ? obj.getIdentityData().getName() : "";
  }

  /**
   * Retrieves the group of the EditorObject associated with the given UUID.
   * Returns an empty string if the object or its identity data is not found.
   *
   * @param id the UUID of the EditorObject. Can be null.
   * @return the group from the object's identity data, or an empty string.
   */
  public String getGroup(UUID id) {
    EditorObject obj = safeGetObject(id);
    return (obj != null && obj.getIdentityData() != null) ? obj.getIdentityData().getType() : "";
  }

  /**
   * Sets the name for the EditorObject associated with the given UUID. Logs an error if the object is not found.
   *
   * @param id   the UUID of the EditorObject.
   * @param name the new name to be set in the object's identity data.
   */
  public void setName(UUID id, String name) {
    EditorObject obj = safeGetObject(id);
    if (obj != null && obj.getIdentityData() != null) {
      obj.getIdentityData().setName(name);
    } else {
      LOG.error("Could not set name for non-existent object or object with null identity: {}", id);
    }
  }

  /**
   * Sets the group for the EditorObject associated with the given UUID. Logs an error if the object is not found.
   *
   * @param id    the UUID of the EditorObject.
   * @param group the new group to be set in the object's identity data.
   */
  public void setGroup(UUID id, String group) {
    EditorObject obj = safeGetObject(id);
    if (obj != null && obj.getIdentityData() != null) {
      obj.getIdentityData().setType(group);
    } else {
      LOG.error("Could not set group for non-existent object or object with null identity: {}", id);
    }
  }

  /**
   * Retrieves the layer priority of the EditorObject associated with the given UUID.
   * Returns 0 if the object, its identity, or its layer is not found.
   *
   * @param id the UUID of the EditorObject. Can be null.
   * @return the priority of the layer that the object belongs to, or 0.
   */
  public int getLayerPriority(UUID id) {
    EditorObject obj = safeGetObject(id);
    if (obj != null && obj.getIdentityData() != null && obj.getIdentityData().getLayer() != null) {
      return obj.getIdentityData().getLayer().getPriority();
    }
    return 0;
  }

  /**
   * Sets the type (often synonymous with group) of the EditorObject.
   * Logs an error if the object is not found.
   *
   * @param id The UUID of the object.
   * @param type The type to set.
   */
  public void setType(UUID id, String type) {
    EditorObject obj = safeGetObject(id);
    if (obj != null && obj.getIdentityData() != null) {
      obj.getIdentityData().setType(type);
    } else {
      LOG.error("Could not set type for non-existent object or object with null identity: {}", id);
    }
  }

  /**
   * Sets the layer of the EditorObject by looking up the layer name in the level data.
   * Logs an error if the object or the specified layer name is not found.
   *
   * @param id The UUID of the object.
   * @param layerName The name of the layer to set.
   */
  public void setLayer(UUID id, String layerName) {
    EditorObject obj = safeGetObject(id);
    if (obj == null || obj.getIdentityData() == null) {
      LOG.error("Could not set layer for non-existent object or object with null identity: {}", id);
      return;
    }

    Layer foundLayer = null;
    for (Layer l : level.getLayers()) {
      if (l.getName().equals(layerName)) {
        foundLayer = l;
        break;
      }
    }
    if (foundLayer != null) {
      obj.getIdentityData().setLayer(foundLayer);
    } else {
      LOG.error("Layer '{}' not found! Could not set layer for object {}", layerName, id);
    }
  }

  /**
   * Retrieves the map of custom string parameters for the specified object.
   * Returns an empty map if the object is not found or has no parameters.
   *
   * @param id the UUID of the EditorObject. Can be null.
   * @return an unmodifiable map of string parameters.
   */
  public Map<String, String> getStringParameters(UUID id) {
    EditorObject obj = safeGetObject(id);
    if (obj != null && obj.getStringParameters() != null) {
      return Collections.unmodifiableMap(obj.getStringParameters());
    }
    return Collections.emptyMap();
  }

  /**
   * Retrieves the map of custom double parameters for the specified object.
   * Returns an empty map if the object is not found or has no parameters.
   *
   * @param id the UUID of the EditorObject. Can be null.
   * @return an unmodifiable map of double parameters.
   */
  public Map<String, Double> getDoubleParameters(UUID id) {
    EditorObject obj = safeGetObject(id);
    if (obj != null && obj.getDoubleParameters() != null) {
      return Collections.unmodifiableMap(obj.getDoubleParameters());
    }
    return Collections.emptyMap();
  }

  /**
   * Sets or updates a custom string parameter for the specified object.
   * Logs an error if the object is not found.
   *
   * @param id the UUID of the EditorObject.
   * @param key the parameter key. Must not be null or empty.
   * @param value the string value to set. Can be null (effectively removing if key exists, though removeParameter is preferred).
   */
  public void setStringParameter(UUID id, String key, String value) {
    if (key == null || key.trim().isEmpty()) {
      LOG.warn("Cannot set string parameter with null or empty key for object {}", id);
      return;
    }
    EditorObject obj = safeGetObject(id);
    if (obj != null) {
      obj.getStringParameters().put(key, value);
      LOG.debug("Set string parameter '{}'='{}' for object {}", key, value, id);
    } else {
      LOG.error("Could not set string parameter for non-existent object: {}", id);
    }
  }

  /**
   * Sets or updates a custom double parameter for the specified object.
   * Logs an error if the object is not found.
   *
   * @param id the UUID of the EditorObject.
   * @param key the parameter key. Must not be null or empty.
   * @param value the double value to set. Can be null (effectively removing if key exists, though removeParameter is preferred).
   */
  public void setDoubleParameter(UUID id, String key, Double value) {
    if (key == null || key.trim().isEmpty()) {
      LOG.warn("Cannot set double parameter with null or empty key for object {}", id);
      return;
    }
    EditorObject obj = safeGetObject(id);
    if (obj != null) {
      obj.getDoubleParameters().put(key, value);
      LOG.debug("Set double parameter '{}'={} for object {}", key, value, id);
    } else {
      LOG.error("Could not set double parameter for non-existent object: {}", id);
    }
  }

  /**
   * Removes a custom parameter (either string or double) with the specified key from the object.
   * Logs an error if the object is not found. Logs a debug message if the key was removed.
   *
   * @param id the UUID of the EditorObject.
   * @param key the key of the parameter to remove. Must not be null.
   */
  public void removeParameter(UUID id, String key) {
    Objects.requireNonNull(key, "Parameter key cannot be null for removal");
    EditorObject obj = safeGetObject(id);
    if (obj != null) {
      boolean removedString = obj.getStringParameters().remove(key) != null;
      boolean removedDouble = obj.getDoubleParameters().remove(key) != null;
      if (removedString || removedDouble) {
        LOG.debug("Removed parameter '{}' (String: {}, Double: {}) from object {}", key, removedString, removedDouble, id);
      }
    } else {
      LOG.error("Could not remove parameter for non-existent object: {}", id);
    }
  }

  /**
   * Safely retrieves an EditorObject by its ID, returning null and logging a warning if not found.
   *
   * @param id The UUID of the object to retrieve.
   * @return The EditorObject, or null if not found.
   */
  private EditorObject safeGetObject(UUID id) {
    if (id == null) {
      return null;
    }
    EditorObject obj = level.getEditorObject(id);
    if (obj == null) {
      LOG.warn("EditorObject with ID {} not found.", id);
    }
    return obj;
  }
}