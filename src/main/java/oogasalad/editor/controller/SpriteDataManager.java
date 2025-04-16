package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;

/**
 * Manages sprite data for EditorObjects, providing methods to access and modify the x and y
 * coordinates of an object's sprite.
 *
 * @author Jacob You
 */
public class SpriteDataManager {

  private EditorLevelData level;

  /**
   * Constructs a SpriteDataManager with the specified EditorLevelData.
   *
   * @param level the EditorLevelData instance that manages the EditorObjects.
   */
  public SpriteDataManager(EditorLevelData level) {
    this.level = level;
  }

  /**
   * Retrieves the x-coordinate of the sprite associated with the given EditorObject.
   *
   * @param id the unique identifier of the EditorObject.
   * @return the x-coordinate of the object's sprite.
   */
  public int getX(UUID id) {
    return level.getEditorObject(id).getSpriteData().getX();
  }

  /**
   * Retrieves the y-coordinate of the sprite associated with the given EditorObject.
   *
   * @param id the unique identifier of the EditorObject.
   * @return the y-coordinate of the object's sprite.
   */
  public int getY(UUID id) {
    return level.getEditorObject(id).getSpriteData().getY();
  }

  /**
   * Sets the x-coordinate of the sprite for the specified EditorObject.
   *
   * @param id the unique identifier of the EditorObject.
   * @param x  the new x-coordinate to set for the object's sprite.
   */
  public void setX(UUID id, int x) {
    level.getEditorObject(id).getSpriteData().setX(x);
  }

  /**
   * Sets the y-coordinate of the sprite for the specified EditorObject.
   *
   * @param id the unique identifier of the EditorObject.
   * @param y  the new y-coordinate to set for the object's sprite.
   */
  public void setY(UUID id, int y) {
    level.getEditorObject(id).getSpriteData().setY(y);
  }

  /**
   * Sets the name of the sprite for the specified EditorObject.
   * @param id The UUID of the object.
   * @param name The name to set.
   */
  public void setName(UUID id, String name) {
    level.getEditorObject(id).getSpriteData().setName(name);
  }

  /**
   * Sets the sprite path for the specified EditorObject.
   * @param id The UUID of the object.
   * @param spritePath The sprite path to set.
   */
  public void setSpritePath(UUID id, String spritePath) {
    level.getEditorObject(id).getSpriteData().setSpritePath(spritePath);
  }

  /**
   * Sets the rotation of the sprite for the specified EditorObject.
   * @param id The UUID of the object.
   * @param rotation The rotation to set.
   */
  public void setRotation(UUID id, double rotation) {
    level.getEditorObject(id).getSpriteData().setRotation(rotation);
  }

  /**
   * Sets the base frame of the sprite for the specified EditorObject.
   * @param id The UUID of the object.
   * @param baseFrame The base frame to set.
   */
  public void setBaseFrame(UUID id, String baseFrame) {
    level.getEditorObject(id).getSpriteData().setBaseFrame(baseFrame);
  }
}