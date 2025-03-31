package oogasalad.engine.model.object;

import oogasalad.file.parser.records.SpriteData;

public abstract class GameObject {
  private final String uuid;
  private String myName;
  private String myGroup;
  private SpriteData mySpriteData;

  public GameObject(String uuid) {
    this.uuid = uuid;
  }

}
