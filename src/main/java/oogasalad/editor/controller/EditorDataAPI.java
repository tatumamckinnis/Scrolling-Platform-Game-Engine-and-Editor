package oogasalad.editor.controller;

import java.util.List;
import java.util.UUID;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.Layer;

public class EditorDataAPI {
  private final IdentityDataManager identityAPI;
  private final HitboxDataManager hitboxAPI;
  private final InputDataManager inputAPI;
  private final PhysicsDataManager physicsAPI;
  private final CollisionDataManager collisionAPI;
  private final SpriteDataManager spriteAPI;
  private final EditorLevelData level;


  public EditorDataAPI(){
    this.level = new EditorLevelData();
    this.identityAPI = new IdentityDataManager(level);
    this.hitboxAPI = new HitboxDataManager(level);
    this.inputAPI = new InputDataManager(level);
    this.physicsAPI = new PhysicsDataManager(level);
    this.collisionAPI = new CollisionDataManager(level);
    this.spriteAPI = new SpriteDataManager(level);
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
  
  public IdentityDataManager getIdentityDataAPI() {
    return identityAPI;
  }
  
  public HitboxDataManager getHitboxDataAPI() {
    return hitboxAPI;
  }
  
  public InputDataManager getInputDataAPI() {
    return inputAPI;
  }
  
  public PhysicsDataManager getPhysicsDataAPI() {
    return physicsAPI;
  }
  
  public CollisionDataManager getCollisionDataAPI() {
    return collisionAPI;
  }

  public SpriteDataManager getSpriteDataAPI() {
    return spriteAPI;
  }
}
