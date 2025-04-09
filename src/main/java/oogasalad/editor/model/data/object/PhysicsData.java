package oogasalad.editor.model.data.object;

import oogasalad.editor.model.data.object.event.EditorEventData;

/**
 * A concrete implementation of {@link EditorEventData} for handling physics events. Although this
 * class does not add additional functionality beyond the base class, it serves as a specific type
 * for organizing and managing physics-related event data within the editor system.
 *
 * @author Jacob You
 */
public class PhysicsData extends EditorEventData {

  /**
   * Constructs a new instance of PhysicsData, initializing the underlying event data map.
   */
  public PhysicsData() {
    super();
  }
}
