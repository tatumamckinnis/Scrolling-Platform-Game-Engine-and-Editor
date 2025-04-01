package oogasalad.engine.model.object;

import oogasalad.fileparser.records.SpriteData;

public abstract class GameObject {
  private final String uuid;
  private String myName;
  private String myGroup;
  private SpriteData mySpriteData;
  private DynamicVariableCollection params;

  public GameObject(String uuid, DynamicVariableCollection params) {
    this.uuid = uuid;
    this.params = params;
  }

  public DynamicVariableCollection getParams() {
    return params;
  }

}
