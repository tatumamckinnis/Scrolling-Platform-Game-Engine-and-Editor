package oogasalad.editor.controller;

import java.util.HashMap;
import java.util.UUID;
import oogasalad.editor.controller.api.EditorDataAPIInterface;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.object.CollisionData;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.InputData;
import oogasalad.editor.model.data.object.PhysicsData;

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

  public void addInputData(UUID id) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getInputData() == null) {
      object.setInputData(new InputData(new HashMap<>()));
    }
  }

  public void addPhysicsData(UUID id) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getPhysicsData() == null) {
      object.setPhysicsData(new PhysicsData(new HashMap<>()));
    }
  }

  public void addCollisionData(UUID id) {
    EditorObject object = level.getEditorObject(id);
    if (object == null) { return; }
    if (object.getCollisionData() == null) {
      object.setCollisionData(new CollisionData(new HashMap<>()));
    }
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
