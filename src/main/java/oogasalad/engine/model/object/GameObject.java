package oogasalad.engine.model.object;

import oogasalad.fileparser.records.SpriteData;

public abstract class GameObject {
  private final String uuid;
  private String myName;
  private String myGroup;
  private SpriteData mySpriteData;
  private DynamicVariableCollection params;

  public GameObject(String uuid, String name, String group, SpriteData spriteData, DynamicVariableCollection params) {
    this.uuid = uuid;
    this.myName = name;
    this.myGroup = group;
    this.mySpriteData = spriteData;
    this.params = params;
  }

  public DynamicVariableCollection getParams() {
    return params;
  }

  public String getUuid() {
    return uuid;
  }

  public String getName() {
    return myName;
  }

  public String getGroup() {
    return myGroup;
  }

  public SpriteData getSpriteData() {
    return mySpriteData;
  }
}
