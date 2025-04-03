package old_editor_example;

import oogasalad.editor.model.data.object_data.DynamicVariableContainer;

public class EditorObject {
  private IdentityData identity;
  private CollisionData collisionData;
  private SpriteData spriteData;
  private DynamicVariableContainer dynamicVariables;
  private InputData inputData;

  public EditorObject(IdentityData identity, CollisionData collisionData, SpriteData spriteData) {
    this.identity = identity;
    this.collisionData = collisionData;
    this.spriteData = spriteData;
    this.dynamicVariables = new DynamicVariableContainer();
    this.inputData = new InputData();
  }

  // Getters and setters.
  public IdentityData getIdentity() {
    return identity;
  }

  public void setIdentity(IdentityData identity) {
    this.identity = identity;
  }
  public CollisionData getCollisionData() {
    return collisionData;
  }
  public void setCollisionData(CollisionData collisionData) {
    this.collisionData = collisionData;
  }
  public SpriteData getSpriteData() {
    return spriteData;
  }
  public void setSpriteData(SpriteData spriteData) {
    this.spriteData = spriteData;
  }
  public DynamicVariableContainer getDynamicVariables() {
    return dynamicVariables;
  }

  public InputData getInputData() {
    return inputData;
  }
}
