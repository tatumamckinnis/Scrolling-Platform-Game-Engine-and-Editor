package oogasalad.editor.controller;

import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.event.AbstractEventMapData;

/**
 * Manages the custom, user-made Event related data of a specific object. Implements EditorEventDataManager
 * for methods pertaining to event, outcome, and condition lists and classes.
 *
 * @author Jacob You
 */
public class CustomEventDataManager extends EditorEventDataManager {

  @Override
  protected AbstractEventMapData createDataIfAbsent(EditorObject object) {
    return object.getCustomEventData();
  }

  /**
   * Creates a CustomEventDataManager on a specific level object.
   *
   * @param level The level object to create a CustomEventDataManager for
   */
  public CustomEventDataManager(EditorLevelData level) {
    super(level);
  }
}
