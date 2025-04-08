package oogasalad.engine.model.object;

import oogasalad.fileparser.records.FrameData;

public class ViewObject implements ImmutableGameObject {

  private final GameObject gameObject;

  public ViewObject(GameObject gameObject) {
    this.gameObject = gameObject;
  }

  @Override
  public String getUuid() {
    return gameObject.getUuid();
  }

  @Override
  public int getX() {
    return gameObject.getX();
  }

  @Override
  public int getY() {
    return gameObject.getY();
  }

  @Override
  public FrameData getCurrentFrame() {
    return gameObject.getCurrentFrame();
  }

  @Override
  public int getHitBoxWidth() {
    return gameObject.getHitBoxWidth();
  }

  @Override
  public int getHitBoxHeight() {
    return gameObject.getHitBoxHeight();
  }

  @Override
  public int getSpriteDx() {
    return gameObject.getSpriteDx();
  }

  @Override
  public int getSpriteDy() {
    return gameObject.getSpriteDy();
  }
}
