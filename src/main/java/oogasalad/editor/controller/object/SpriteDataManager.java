package oogasalad.editor.controller.object;

import java.util.UUID;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;

/**
 * Manages sprite data for EditorObjects, providing methods to access and modify the x and y
 * coordinates of an object's sprite. Provides methods to get and set sprite attributes such as
 * position, name, rotation, and flip status, and allows applying a sprite template to an object.
 *
 * @author Jacob You
 */
public class SpriteDataManager {

  private final EditorListenerNotifier listenerNotifier;
  private EditorLevelData level;

  /**
   * Constructs a {@code SpriteDataManager} with the specified EditorLevelData.
   *
   * @param level            the EditorLevelData instance that manages the EditorObjects
   * @param listenerNotifier the notifier used to broadcast sprite updates
   */
  public SpriteDataManager(EditorLevelData level, EditorListenerNotifier listenerNotifier) {
    this.level = level;
    this.listenerNotifier = listenerNotifier;
  }

  /**
   * Returns the name of the sprite associated with the specified object.
   *
   * @param id the UUID of the object
   * @return the name of the sprite
   */
  public String getName(UUID id) {
    return level.getEditorObject(id).getSpriteData().getName();
  }

  /**
   * Retrieves the x-coordinate of the sprite associated with the given EditorObject.
   *
   * @param id the unique identifier of the EditorObject
   * @return the x-coordinate of the object's sprite
   */
  public int getX(UUID id) {
    return level.getEditorObject(id).getSpriteData().getX();
  }

  /**
   * Retrieves the y-coordinate of the sprite associated with the given EditorObject.
   *
   * @param id the unique identifier of the EditorObject
   * @return the y-coordinate of the object's sprite
   */
  public int getY(UUID id) {
    return level.getEditorObject(id).getSpriteData().getY();
  }

  /**
   * Returns the current rotation of the sprite.
   *
   * @param id the UUID of the object
   * @return the rotation angle in degrees
   */
  public double getRotation(UUID id) {
    return level.getEditorObject(id).getSpriteData().getRotation();
  }

  /**
   * Returns whether the sprite is flipped horizontally.
   *
   * @param id the UUID of the object
   * @return {@code true} if flipped; {@code false} otherwise
   */
  public boolean getFlip(UUID id) {
    return level.getEditorObject(id).getSpriteData().getIsFlipped();
  }

  /**
   * Returns the template name used by the sprite.
   *
   * @param id the UUID of the object
   * @return the name of the sprite template
   */
  public String getTemplateName(UUID id) {
    return level.getEditorObject(id).getSpriteData().getTemplateName();
  }

  /**
   * Returns the base frame name of the sprite.
   *
   * @param id the UUID of the object
   * @return the base frame name
   */
  public String getBaseFrameName(UUID id) {
    return level.getEditorObject(id).getSpriteData().getBaseFrameName();
  }

  /**
   * Returns whether the sprite is flipped horizontally (alternate method name for clarity).
   *
   * @param id the UUID of the object
   * @return {@code true} if flipped; {@code false} otherwise
   */
  public boolean getIsFlipped(UUID id) {
    return level.getEditorObject(id).getSpriteData().getIsFlipped();
  }

  /**
   * Sets the x-coordinate of the sprite for the specified EditorObject.
   *
   * @param id the UUID of the object
   * @param x  the new x-coordinate to set
   */
  public void setX(UUID id, int x) {
    level.getEditorObject(id).getSpriteData().setX(x);
  }

  /**
   * Sets the y-coordinate of the sprite for the specified EditorObject.
   *
   * @param id the UUID of the object
   * @param y  the new y-coordinate to set
   */
  public void setY(UUID id, int y) {
    level.getEditorObject(id).getSpriteData().setY(y);
  }

  /**
   * Sets the name of the sprite for the specified EditorObject.
   *
   * @param id   the UUID of the object
   * @param name the new sprite name to set
   */
  public void setName(UUID id, String name) {
    level.getEditorObject(id).getSpriteData().setName(name);
  }

  /**
   * Sets the sprite path (file reference) for the specified EditorObject.
   *
   * @param id         the UUID of the object
   * @param spritePath the new sprite path to set
   */
  public void setSpritePath(UUID id, String spritePath) {
    level.getEditorObject(id).getSpriteData().setSpritePath(spritePath);
  }

  /**
   * Sets the rotation angle of the sprite for the specified EditorObject.
   *
   * @param id       the UUID of the object
   * @param rotation the new rotation angle in degrees
   */
  public void setRotation(UUID id, double rotation) {
    level.getEditorObject(id).getSpriteData().setRotation(rotation);
  }

  /**
   * Sets whether the sprite should be flipped horizontally.
   *
   * @param id      the UUID of the object
   * @param flipped {@code true} to flip the sprite; {@code false} otherwise
   */
  public void setFlip(UUID id, boolean flipped) {
    level.getEditorObject(id).getSpriteData().setIsFlipped(flipped);
  }

  /**
   * Sets the base frame name for the sprite of the specified EditorObject.
   *
   * @param id        the UUID of the object
   * @param baseFrame the base frame name to set
   */
  public void setBaseFrameName(UUID id, String baseFrame) {
    level.getEditorObject(id).getSpriteData().setBaseFrameName(baseFrame);
  }

  /**
   * Applies a {@link SpriteTemplate} to the specified object's sprite data, updating its frames,
   * animations, base frame, template name, and sprite path. Notifies listeners after the update.
   *
   * @param currentObjectId the UUID of the object to apply the template to
   * @param template        the {@link SpriteTemplate} to apply
   */
  public void applyTemplateToSprite(UUID currentObjectId, SpriteTemplate template) {
    SpriteData spriteData = level.getEditorObject(currentObjectId).getSpriteData();
    spriteData.setFrames(template.getFrames());
    spriteData.setAnimations(template.getAnimations());
    spriteData.setBaseFrameName(template.getBaseFrame().name());
    spriteData.setTemplateName(template.getName());
    spriteData.setSpritePath(template.getSpriteFile());
    HitboxData hitboxData = level.getEditorObject(currentObjectId).getHitboxData();
    FrameData base = template.getBaseFrame();
    hitboxData.setWidth(base.width());
    hitboxData.setHeight(base.height());


    listenerNotifier.notifyObjectUpdated(currentObjectId);
  }
}