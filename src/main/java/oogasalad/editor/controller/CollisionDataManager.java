package oogasalad.editor.controller;

import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.event.AbstractEventMapData;

/**
 * Manages the Collision Event related data of a specific object. Implements EditorEventDataManager
 * for methods pertaining to event, outcome, and condition lists and classes.
 *
 * @author Jacob You
 */
public class CollisionDataManager extends EditorEventDataManager {

  @Override
  protected AbstractEventMapData createDataIfAbsent(EditorObject object) {
    return object.getCollisionData();
  }

  /**
   * Creates a CollisionDataManager on a specific level object.
   *
   * @param level The level object to create a CollisionDataManager for
   */
  public CollisionDataManager(EditorLevelData level) {
    super(level);
  }
}
