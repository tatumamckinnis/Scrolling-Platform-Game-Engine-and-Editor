package oogasalad.engine.controller;

import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteData;

public interface ImmutableGameObject {

  public String getUuid();

  public int getX();

  public int getY();

  public FrameData getCurrentFrame() ;

  public int getHitBoxWidth() ;

  public int getHitBoxHeight();

  public int getSpriteDx();

  public int getSpriteDy();

}
