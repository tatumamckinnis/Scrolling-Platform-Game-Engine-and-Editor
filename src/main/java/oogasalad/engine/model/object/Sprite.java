package oogasalad.engine.model.object;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import oogasalad.Main;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;

public class Sprite {
  private static ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(Main.class.getPackage().getName() + "." + "Exceptions");

  private int spriteDx;
  private int spriteDy;
  private FrameData currentSprite;
  private Map<String, FrameData> frameMap;
  private Map<String, AnimationData> animations;

  public Sprite(Map<String, FrameData> frameMap, FrameData currentSprite, Map<String, AnimationData> animations, int spriteDx, int spriteDy) {
    this.spriteDx = spriteDx;
    this.spriteDy = spriteDy;
    this.frameMap = frameMap;
    // current sprite is typically to be the base image of the SpriteData from the file
    this.currentSprite = currentSprite;
    this.animations = animations;
  }

  public int getSpriteDx() {
    return spriteDx;
  }

  public int getSpriteDy() {
    return spriteDy;
  }

  public FrameData getCurrentSprite() {
    return currentSprite;
  }

  public Map<String, FrameData> getFrameMap() {
    return frameMap;
  }

  public FrameData getSprite(String id) {
    if (frameMap.containsKey(id)) {
      return frameMap.get(id);
    }
    throw new NoSuchElementException(EXCEPTIONS.getString("SpriteNotFound"));
  }


}
