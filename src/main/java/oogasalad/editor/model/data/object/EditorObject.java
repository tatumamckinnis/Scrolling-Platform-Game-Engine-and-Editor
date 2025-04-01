package oogasalad.editor.model.data.object;

import java.util.Properties;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;

public class EditorObject {

  private EditorLevelData level;
  private IdentityData identity;
  private InputData input;
  private PhysicsData physics;
  private CollisionData collision;
  private SpriteData sprite;
  private HitboxData hitbox;
  private Properties editorConfig;

  public EditorObject(EditorLevelData level, IdentityData identity, InputData input,
      PhysicsData physics, CollisionData collision, SpriteData sprite, HitboxData hitbox) {
    this.level = level;
    this.editorConfig = level.getEditorConfig();

    this.identity = identity;
    this.input = input;
    this.physics = physics;
    this.collision = collision;
    this.sprite = sprite;
    this.hitbox = hitbox;
  }

  public EditorObject(EditorLevelData level) {
    this.level = level;
    this.editorConfig = level.getEditorConfig();

    this.identity = new IdentityData(UUID.randomUUID(), "Untitled", "");
    this.hitbox = new HitboxData(0, 0,
        Double.parseDouble(editorConfig.getProperty("defaultHitboxWidth")),
        Double.parseDouble(editorConfig.getProperty("defaultHitboxHeight")),
        editorConfig.getProperty("defaultHitboxShape"));
    this.sprite = new SpriteData(0,0,"");
    this.input = null;
    this.physics = null;
    this.collision = null;
  }

  public IdentityData getIdentityData() {
    return identity;
  }

  public void setIdentityData(IdentityData identity) {
    this.identity = identity;
  }

  public InputData getInputData() {
    return input;
  }

  public void setInputData(InputData input) {
    this.input = input;
  }

  public PhysicsData getPhysicsData() {
    return physics;
  }

  public void setPhysicsData(PhysicsData physics) {
    this.physics = physics;
  }

  public CollisionData getCollisionData() {
    return collision;
  }

  public void setCollisionData(CollisionData collision) {
    this.collision = collision;
  }

  public SpriteData getSpriteData() {
    return sprite;
  }

  public void setSpriteData(SpriteData sprite) {
    this.sprite = sprite;
  }

  public HitboxData getHitboxData() {
    return hitbox;
  }

  public void setHitboxData(HitboxData hitbox) {
    this.hitbox = hitbox;
  }
}
