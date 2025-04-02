package oogasalad.editor.controller;

import java.util.UUID;
import oogasalad.editor.controller.api.EditorDataAPIInterface;
import oogasalad.editor.model.data.EditorLevelData;

public class EditorDataAPI implements EditorDataAPIInterface {
  private final IdentityDataAPI identityAPI;
  private final HitboxDataAPI hitboxAPI;
  private final InputDataAPI inputAPI;
  private final PhysicsDataAPI physicsAPI;
  private final CollisionDataAPI collisionAPI;
  private final SpriteDataAPI spriteAPI;
  private final EditorLevelData level;


  public EditorDataAPI(){
    this.level = new EditorLevelData();
    this.identityAPI = new IdentityDataAPI(level);
    this.hitboxAPI = new HitboxDataAPI(level);
    this.inputAPI = new InputDataAPI(level);
    this.physicsAPI = new PhysicsDataAPI(level);
    this.collisionAPI = new CollisionDataAPI(level);
    this.spriteAPI = new SpriteDataAPI(level);
  }

  public UUID createEditorObject() {
    return level.createEditorObject();
  }

  @Override
  public IdentityDataAPI getIdentityDataAPI() {
    return identityAPI;
  }

  @Override
  public HitboxDataAPI getHitboxDataAPI() {
    return hitboxAPI;
  }

  @Override
  public InputDataAPI getInputDataAPI() {
    return inputAPI;
  }

  @Override
  public PhysicsDataAPI getPhysicsDataAPI() {
    return physicsAPI;
  }

  @Override
  public CollisionDataAPI getCollisionDataAPI() {
    return collisionAPI;
  }

  @Override
  public SpriteDataAPI getSpriteDataAPI() {
    return spriteAPI;
  }
}
