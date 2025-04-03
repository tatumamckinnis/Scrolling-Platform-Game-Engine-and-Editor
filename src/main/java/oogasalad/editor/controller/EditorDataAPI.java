package oogasalad.editor.controller;

import java.util.List;
import java.util.UUID;
import oogasalad.editor.controller.api.EditorDataAPIInterface;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.Layer;

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

  public EditorObject getEditorObject(UUID id) {
    return level.getEditorObject(id);
  }

  public void addLayer(String layerName) {
    level.addLayer(new Layer(layerName, level.getLayers().get(0).getPriority() + 1));
  }

  public List<Layer> getLayers() {
    return level.getLayers();
  }

  public void removeLayer(String layerName) {
    level.removeLayer(layerName);
  }

  public void addGroup(String groupName) {
    level.addGroup(groupName);
  }

  public List<String> getGroups() {
    return level.getGroups();
  }

  public void removeGroup(String groupName) {
    level.removeGroup(groupName);
  }

  public EditorLevelData getLevel() { return level; }

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
