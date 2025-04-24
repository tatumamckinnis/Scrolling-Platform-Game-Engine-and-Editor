package oogasalad.editor.controller.object_data;

import java.util.UUID;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;

/**
 * Manages sprite data for EditorObjects, providing methods to access and modify the x and y
 * coordinates of an object's sprite.
 *
 * @author Jacob You
 */
public class SpriteDataManager {

  private final EditorListenerNotifier listenerNotifier;
  private EditorLevelData level;

  /**
   * Constructs a SpriteDataManager with the specified EditorLevelData.
   *
   * @param level            the EditorLevelData instance that manages the EditorObjects.
   * @param listenerNotifier
   */
  public SpriteDataManager(EditorLevelData level, EditorListenerNotifier listenerNotifier) {
    this.level = level;
    this.listenerNotifier = listenerNotifier;
  }

  public String getName(UUID id) {
    return level.getEditorObject(id).getSpriteData().getName();
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

  public double getRotation(UUID id) {
    return level.getEditorObject(id).getSpriteData().getRotation();
  }

  public boolean getFlip(UUID id) {
    return level.getEditorObject(id).getSpriteData().getIsFlipped();
  }

  public String getTemplateName(UUID id) {
    return level.getEditorObject(id).getSpriteData().getTemplateName();
  }

  public String getBaseFrameName(UUID id) {
    return level.getEditorObject(id).getSpriteData().getBaseFrameName();
  }

  public boolean getIsFlipped(UUID id) {
    return level.getEditorObject(id).getSpriteData().getIsFlipped();
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
   *
   * @param id   The UUID of the object.
   * @param name The name to set.
   */
  public void setName(UUID id, String name) {
    level.getEditorObject(id).getSpriteData().setName(name);
  }

  /**
   * Sets the sprite path for the specified EditorObject.
   *
   * @param id         The UUID of the object.
   * @param spritePath The sprite path to set.
   */
  public void setSpritePath(UUID id, String spritePath) {
    level.getEditorObject(id).getSpriteData().setSpritePath(spritePath);
  }

  /**
   * Sets the rotation of the sprite for the specified EditorObject.
   *
   * @param id       The UUID of the object.
   * @param rotation The rotation to set.
   */
  public void setRotation(UUID id, double rotation) {
    level.getEditorObject(id).getSpriteData().setRotation(rotation);
  }

  /**
   * Sets the flip of a sprite for the specified EditorObject.
   *
   * @param id      The UUID of the object
   * @param flipped The flip status to set
   */
  public void setFlip(UUID id, boolean flipped) {
    level.getEditorObject(id).getSpriteData().setIsFlipped(flipped);
  }

  /**
   * Sets the base frame of the sprite for the specified EditorObject.
   *
   * @param id        The UUID of the object.
   * @param baseFrame The base frame to set.
   */
  public void setBaseFrameName(UUID id, String baseFrame) {
    level.getEditorObject(id).getSpriteData().setBaseFrameName(baseFrame);
  }

  public void applyTemplateToSprite(UUID currentObjectId, SpriteTemplate template) {
    SpriteData spriteData = level.getEditorObject(currentObjectId).getSpriteData();
    spriteData.setFrames(template.getFrames());
    spriteData.setAnimations(template.getAnimations());
    spriteData.setBaseFrameName(template.getBaseFrame().name());
    spriteData.setTemplateName(template.getName());
    spriteData.setSpritePath(template.getSpriteFile());

    listenerNotifier.notifyObjectUpdated(currentObjectId);
  }
}