package oogasalad.editor.model.data.object;

import oogasalad.editor.model.data.object.event.EditorEventData;

/**
 * A concrete implementation of {@link EditorEventData} for handling collision events. Although this
 * class does not add additional functionality beyond the base class, it serves as a specific type
 * for organizing and managing collision-related event data within the editor system.
 */
public class CollisionData extends EditorEventData {

  /**
   * Constructs a new instance of CollisionData, initializing the underlying event data map.
   */
  public CollisionData() {
    super();
  }
}
