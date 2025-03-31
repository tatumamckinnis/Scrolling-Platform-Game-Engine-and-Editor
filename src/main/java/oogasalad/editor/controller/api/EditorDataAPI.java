package oogasalad.editor.controller.api;

import java.util.List;
import java.util.Map;

/**
 * Defines methods to synchronize editor data between the front end (UI)
 * and the back end (engine model).
 *
 * This API ensures that when the user edits an object’s name, position, or
 * dynamic variables in the UI, those changes are reflected in the engine’s
 * data model, and vice versa.
 */
public interface EditorDataAPI {

  /**
   * Returns a list of identifiers (UUIDs or internal IDs) for all GameObjects
   * currently in the editor’s active scene or level.
   */
  List<String> getAllObjectIDs();

  /**
   * Retrieves basic metadata for a GameObject (e.g., name, group, sprite path).
   *
   * @param objectID unique identifier for the game object
   * @return a Map of property name -> value for display or editing in the UI
   */
  Map<String, Object> getObjectMetadata(String objectID);

  /**
   * Updates a single property in the specified game object’s metadata
   * (e.g., changing the name, group, sprite path, etc.).
   *
   * @param objectID The unique identifier for the object to update
   * @param propertyName The name of the property (e.g., "name", "group", "spritePath")
   * @param newValue The new value for that property
   */
  void setObjectMetadata(String objectID, String propertyName, Object newValue);

  /**
   * Retrieves all dynamic variables for a GameObject, typically from
   * its DynamicVariableContainer (e.g., "pos.x", "game.lives").
   *
   * @param objectID unique identifier for the game object
   * @return a Map of variableName -> (type, value, description) or
   *         variableName -> value
   */
  Map<String, Object> getDynamicVariables(String objectID);

  /**
   * Updates a specific dynamic variable in the object’s DynamicVariableContainer.
   *
   * @param objectID The unique identifier for the object
   * @param variableName The name of the dynamic variable (e.g., "pos.x")
   * @param newValue The new value (already parsed from the UI)
   */
  void setDynamicVariable(String objectID, String variableName, Object newValue);

  /**
   * Creates a new game object in the editor’s data model, returning its
   * generated ID. The front end can then set additional properties or
   * dynamic variables via other methods.
   *
   * @param initialName A human-readable name (optional)
   * @param groupName A group/collision category
   * @param spritePath The initial sprite path
   * @return the unique identifier (UUID) of the newly created object
   */
  String createGameObject(String initialName, String groupName, String spritePath);

  /**
   * Removes a game object from the scene entirely.
   *
   * @param objectID The ID of the object to remove
   */
  void deleteGameObject(String objectID);

  /**
   * (Optional) Retrieves collision-related data for the object (box coords, shape).
   * Alternatively, you can store these in getObjectMetadata or as dynamic variables.
   */
  Map<String, Object> getCollisionData(String objectID);

  /**
   * (Optional) Updates collision data for the object.
   * E.g., collision width, height, shape, etc.
   */
  void setCollisionData(String objectID, Map<String, Object> collisionProperties);

  /**
   * (Optional) Retrieves input mappings for the object (e.g., InputHandler data).
   */
  Map<String, String> getInputMappings(String objectID);

  /**
   * (Optional) Updates input mappings for the object (key -> eventChainID).
   */
  void setInputMappings(String objectID, Map<String, String> mappings);
}
