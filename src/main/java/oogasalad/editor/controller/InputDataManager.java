package oogasalad.editor.controller;

import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.event.EditorEventData;

/**
 * Manages the Input Event related data of a specific object. Implements EditorEventDataManager for
 * methods pertaining to event, outcome, and condition lists and classes.
 *
 * @author Jacob You
 */
public class InputDataManager extends EditorEventDataManager {

  @Override
  protected EditorEventData createDataIfAbsent(EditorObject object) {
    return object.getInputData();
  }

  /**
   * Creates a CollisionDataManager on a specific level object.
   *
   * @param level The level object to create a CollisionDataManager for
   */
  public InputDataManager(EditorLevelData level) {
    super(level);
  }
}
