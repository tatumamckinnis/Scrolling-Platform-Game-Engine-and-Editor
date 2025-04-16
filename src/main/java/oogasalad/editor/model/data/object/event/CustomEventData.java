package oogasalad.editor.model.data.object.event;

import oogasalad.editor.model.data.object.event.AbstractEventMapData;

/**
 * A concrete implementation of {@link AbstractEventMapData} for handling custom, user-made events. Although this
 * class does not add additional functionality beyond the base class, it serves as a specific type
 * for organizing and managing custom event data within the editor system.
 *
 * @author Jacob You
 */
public class CustomEventData extends AbstractEventMapData {

  /**
   * Constructs a new instance of CustomEventData, initializing the underlying event data map.
   */
  public CustomEventData() {
    super();
  }
}