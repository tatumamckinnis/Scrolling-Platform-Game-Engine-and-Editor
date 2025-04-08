package oogasalad.engine.model.object;

import oogasalad.fileparser.records.FrameData;

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
