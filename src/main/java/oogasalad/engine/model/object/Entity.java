package oogasalad.engine.model.object;


import oogasalad.fileparser.records.SpriteData;

public class Entity extends GameObject {


  public Entity(String uuid, String name, String group,
      SpriteData spriteData, DynamicVariableCollection params) {
    super(uuid, name, group, spriteData, params);
  }
}
