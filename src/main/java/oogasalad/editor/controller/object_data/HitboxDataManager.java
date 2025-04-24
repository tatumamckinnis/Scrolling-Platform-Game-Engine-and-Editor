package oogasalad.editor.controller.object_data;

import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;

/**
 * Manages hitbox data for editor objects by providing methods to access and modify various hitbox
 * properties such as position, dimensions, and shape. This class uses an {@link EditorLevelData}
 * instance to retrieve an editor object by its UUID and then operates on the object's hitbox data.
 *
 * @author Jacob You
 */
public class HitboxDataManager {

  private EditorLevelData level;

  /**
   * Constructs a HitboxDataManager with the specified EditorLevelData instance.
   *
   * @param level the EditorLevelData instance that manages the editor objects
   */
  public HitboxDataManager(EditorLevelData level) {
    this.level = level;
  }

  /**
   * Retrieves the x-coordinate of the hitbox for the specified editor object.
   *
   * @param id the unique identifier of the editor object
   * @return the x-coordinate of the object's hitbox
   */
  public int getX(UUID id) {
    return level.getEditorObject(id).getHitboxData().getX();
  }

  /**
   * Retrieves the y-coordinate of the hitbox for the specified editor object.
   *
   * @param id the unique identifier of the editor object
   * @return the y-coordinate of the object's hitbox
   */
  public int getY(UUID id) {
    return level.getEditorObject(id).getHitboxData().getY();
  }

  /**
   * Retrieves the width of the hitbox for the specified editor object.
   *
   * @param id the unique identifier of the editor object
   * @return the width of the object's hitbox
   */
  public int getWidth(UUID id) {
    return level.getEditorObject(id).getHitboxData().getWidth();
  }

  /**
   * Retrieves the height of the hitbox for the specified editor object.
   *
   * @param id the unique identifier of the editor object
   * @return the height of the object's hitbox
   */
  public int getHeight(UUID id) {
    return level.getEditorObject(id).getHitboxData().getHeight();
  }

  /**
   * Retrieves the shape of the hitbox for the specified editor object.
   *
   * @param id the unique identifier of the editor object
   * @return a String representing the shape of the hitbox (e.g., "rectangle", "circle")
   */
  public String getShape(UUID id) {
    return level.getEditorObject(id).getHitboxData().getShape();
  }

  /**
   * Sets the x-coordinate of the hitbox for the specified editor object.
   *
   * @param id the unique identifier of the editor object
   * @param x  the new x-coordinate to set
   */
  public void setX(UUID id, int x) {
    level.getEditorObject(id).getHitboxData().setX(x);
  }

  /**
   * Sets the y-coordinate of the hitbox for the specified editor object.
   *
   * @param id the unique identifier of the editor object
   * @param y  the new y-coordinate to set
   */
  public void setY(UUID id, int y) {
    level.getEditorObject(id).getHitboxData().setY(y);
  }

  /**
   * Sets the width of the hitbox for the specified editor object.
   *
   * @param id    the unique identifier of the editor object
   * @param width the new width of the hitbox to set
   */
  public void setWidth(UUID id, int width) {
    level.getEditorObject(id).getHitboxData().setWidth(width);
  }

  /**
   * Sets the height of the hitbox for the specified editor object.
   *
   * @param id     the unique identifier of the editor object
   * @param height the new height of the hitbox to set
   */
  public void setHeight(UUID id, int height) {
    level.getEditorObject(id).getHitboxData().setHeight(height);
  }

  /**
   * Sets the shape of the hitbox for the specified editor object.
   *
   * @param id    the unique identifier of the editor object
   * @param shape the new shape of the hitbox (e.g., "rectangle", "circle")
   */
  public void setShape(UUID id, String shape) {
    level.getEditorObject(id).getHitboxData().setShape(shape);
  }
}
