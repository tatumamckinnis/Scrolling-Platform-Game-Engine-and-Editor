package oogasalad.editor.view;

import java.util.UUID;

/**
 * Interface for components that need to be notified about changes originating from the
 * EditorController or the underlying model state. Follows the Observer pattern. (DESIGN-11,
 * DESIGN-20)
 *
 * @author Tatum McKinnis
 */
public interface EditorViewListener {

  /**
   * Called when a new game object has been successfully added to the model. The view should
   * typically add a visual representation.
   *
   * @param objectId The ID of the newly added object.
   */
  void onObjectAdded(UUID objectId);

  /**
   * Called when a game object has been successfully removed from the model. The view should
   * typically remove the visual representation.
   *
   * @param objectId The ID of the removed object.
   */
  void onObjectRemoved(UUID objectId);

  /**
   * Called when an existing game object's data (e.g., position, properties) has changed. The view
   * should typically update the visual representation.
   *
   * @param objectId The ID of the updated object.
   */
  void onObjectUpdated(UUID objectId);

  /**
   * Called when the currently selected object changes. Views like property inspectors or input tabs
   * should update accordingly.
   *
   * @param selectedObjectId The ID of the newly selected object, or null if none is selected.
   */
  void onSelectionChanged(UUID selectedObjectId);

  /**
   * Called when the list or content of dynamic variables changes. Views using dynamic variables
   * (like the InputTab's parameter dropdown) should update.
   */
  void onDynamicVariablesChanged();

  /**
   * Called when an error occurs during a controller action that the view should be aware of.
   *
   * @param errorMessage A message describing the error.
   */
  void onErrorOccurred(String errorMessage);

  /**
   * Called when prefabs are changed
   */
  void onPrefabsChanged();

  /**
   * Called when a sprite template is changed
   */
  void onSpriteTemplateChanged();
}