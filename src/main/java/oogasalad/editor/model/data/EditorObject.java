package oogasalad.editor.model.data;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.IdentityData;
import oogasalad.editor.model.data.object.event.CollisionData;
import oogasalad.editor.model.data.object.event.CustomEventData;
import oogasalad.editor.model.data.object.event.EventData;
import oogasalad.editor.model.data.object.event.InputData;
import oogasalad.editor.model.data.object.event.PhysicsData;
import oogasalad.editor.model.data.object.sprite.SpriteData;

/**
 * Represents an object within the editor, encapsulating all data components such as identity, input
 * configuration, physics, collision, sprite, and hitbox data. EditorObjects are managed within an
 * {@link EditorLevelData} instance and are configured using properties loaded from the editor
 * configuration.
 *
 * @author Jacob You
 */
public class EditorObject {

  private EditorLevelData level;
  private IdentityData identity;
  private InputData input;
  private PhysicsData physics;
  private CollisionData collision;
  private SpriteData sprite;
  private HitboxData hitbox;
  private CustomEventData custom;
  private EventData event;
  private Properties editorConfig;

  /**
   * Constructs an EditorObject with all specified data components.
   *
   * @param level     the EditorLevelData instance managing this object
   * @param identity  the identity data containing the object's unique ID, name, group, and layer
   * @param input     the input configuration data for this object
   * @param physics   the physics data for this object
   * @param collision the collision data for this object
   * @param sprite    the sprite data for this object
   * @param hitbox    the hitbox data for this object
   * @param custom    the custom event data for this object
   * @param event     the event data for this object
   */
  public EditorObject(EditorLevelData level, IdentityData identity, InputData input,
      PhysicsData physics, CollisionData collision, SpriteData sprite, HitboxData hitbox,
      CustomEventData custom, EventData event) {
    this.level = level;
    this.editorConfig = level.getEditorConfig();
    this.identity = identity;
    this.input = input;
    this.physics = physics;
    this.collision = collision;
    this.sprite = sprite;
    this.hitbox = hitbox;
    this.custom = custom;
    this.event = event;
  }

  /**
   * Constructs an EditorObject with default data components. ` The identity is initialized with a
   * random UUID and a default name. The hitbox dimensions and shape are obtained from the editor
   * configuration properties. Other data components (sprite, input, physics, collision) are
   * initialized with default values.
   *
   * @param level the EditorLevelData instance managing this object
   */
  public EditorObject(EditorLevelData level) {
    this.level = level;
    this.editorConfig = level.getEditorConfig();
    this.identity = new IdentityData(UUID.randomUUID(), "Untitled", "", level.getFirstLayer());
    // TODO: Make untitled a property rather than defined
    this.hitbox = new HitboxData(0, 0,
        Integer.parseInt(editorConfig.getProperty("defaultHitboxWidth")),
        Integer.parseInt(editorConfig.getProperty("defaultHitboxHeight")),
        editorConfig.getProperty("defaultHitboxShape"));
    this.sprite = new SpriteData("", 0, 0, 0, new HashMap<>(), new HashMap<>(), "");
    this.input = new InputData();
    this.physics = new PhysicsData();
    this.collision = new CollisionData();
    this.event = new EventData();
    this.custom = new CustomEventData();
  }

  /**
   * Returns the unique identifier of this editor object.
   *
   * @return the UUID of the editor object, or null if the identity data is not set
   */
  public UUID getId() {
    if (this.identity != null) {
      return this.identity.getId();
    }
    return null;
  }

  /**
   * Retrieves the identity data of this editor object.
   *
   * @return the {@link IdentityData} instance associated with this object
   */
  public IdentityData getIdentityData() {
    return identity;
  }

  /**
   * Sets the identity data for this editor object.
   *
   * @param identity the new {@link IdentityData} to set
   */
  public void setIdentityData(IdentityData identity) {
    this.identity = identity;
  }

  /**
   * Retrieves the input data of this editor object.
   *
   * @return the {@link InputData} associated with this object
   */
  public InputData getInputData() {
    return input;
  }

  /**
   * Sets the input data for this editor object.
   *
   * @param input the new {@link InputData} to set
   */
  public void setInputData(InputData input) {
    this.input = input;
  }

  /**
   * Retrieves the physics data of this editor object.
   *
   * @return the {@link PhysicsData} associated with this object
   */
  public PhysicsData getPhysicsData() {
    return physics;
  }

  /**
   * Sets the physics data for this editor object.
   *
   * @param physics the new {@link PhysicsData} to set
   */
  public void setPhysicsData(PhysicsData physics) {
    this.physics = physics;
  }

  /**
   * Retrieves the collision data of this editor object.
   *
   * @return the {@link CollisionData} associated with this object
   */
  public CollisionData getCollisionData() {
    return collision;
  }

  /**
   * Sets the collision data for this editor object.
   *
   * @param collision the new {@link CollisionData} to set
   */
  public void setCollisionData(CollisionData collision) {
    this.collision = collision;
  }

  /**
   * Retrieves the sprite data of this editor object.
   *
   * @return the {@link SpriteData} associated with this object
   */
  public SpriteData getSpriteData() {
    return sprite;
  }

  /**
   * Sets the sprite data for this editor object.
   *
   * @param sprite the new {@link SpriteData} to set
   */
  public void setSpriteData(SpriteData sprite) {
    this.sprite = sprite;
  }

  /**
   * Retrieves the hitbox data of this editor object.
   *
   * @return the {@link HitboxData} associated with this object
   */
  public HitboxData getHitboxData() {
    return hitbox;
  }

  /**
   * Sets the hitbox data for this editor object.
   *
   * @param hitbox the new {@link HitboxData} to set
   */
  public void setHitboxData(HitboxData hitbox) {
    this.hitbox = hitbox;
  }

  /**
   * Retrieves the custom event data of this editor object.
   *
   * @return the {@link CustomEventData} associated with this object
   */
  public CustomEventData getCustomEventData() {
    return custom;
  }

  /**
   * Sets the custom data for this editor object.
   *
   * @param custom the new {@link CustomEventData} to set
   */
  public void setCustomEventData(CustomEventData custom) {
    this.custom = custom;
  }

  /**
   * Retrieves the event data of this editor object.
   *
   * @return the {@link EventData} associated with this object
   */
  public EventData getEventData() {
    return event;
  }

  /**
   * Sets the event data for this editor object.
   *
   * @param event the new {@link EventData} to set
   */
  public void setEventData(EventData event) {
    this.event = event;
  }
}
