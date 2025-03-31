package oogasalad.editor.model.data.object;

import java.util.UUID;

public class EditorObject {

  private IdentityData identity;
  private InputData input;
  private PhysicsData physics;
  private CollisionData collision;
  private SpriteData sprite;

  public EditorObject(IdentityData identity, InputData input, PhysicsData physics,
      CollisionData collision, SpriteData sprite) {
    this.identity = identity;
    this.input = input;
    this.physics = physics;
    this.collision = collision;
    this.sprite = sprite;
  }

  public EditorObject() {
    this.identity = new IdentityData(UUID.randomUUID(), "Untitled", "");
    this.input = null;
    this.physics = null;
    this.collision = null;
    this.sprite = null;
  }

  public IdentityData getIdentity() {
    return identity;
  }

  public InputData getInput() {
    return input;
  }

  public PhysicsData getPhysics() {
    return physics;
  }

  public CollisionData getCollision() {
    return collision;
  }

  public SpriteData getSprite() {
    return sprite;
  }
}
