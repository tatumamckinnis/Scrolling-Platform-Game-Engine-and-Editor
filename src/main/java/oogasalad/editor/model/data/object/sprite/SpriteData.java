package oogasalad.editor.model.data.object.sprite;

import java.util.List;
import java.util.Map;

public class SpriteData {
  private int x;
  private int y;
  private Map<String, FrameData> frames;
  private Map<String, AnimationData> animations;
  private String spritePath;

  public SpriteData(int x, int y, Map<String, FrameData> frames, Map<String, AnimationData> animations, String spritePath) {
    this.x = x;
    this.y = y;
    this.frames = frames;
    this.animations = animations;
    this.spritePath = spritePath;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Map<String, FrameData> getFrames() {
    return frames;
  }

  public Map<String, AnimationData> getAnimations() {
    return animations;
  }

  public String getSpritePath() {
    return spritePath;
  }

  public FrameData getFrame(String frameName) {
    return frames.get(frameName);
  }

  public List<String> getAnimationFrameNames(String animationName) {
    return animations.get(animationName).getFrameNames();
  }

  public AnimationData getAnimation(String animationName) {
    return animations.get(animationName);
  }

  public double getAnimationFrameLength(String animationName) {
    return animations.get(animationName).getFrameLength();
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void setFrames(Map<String, FrameData> frames) {
    this.frames = frames;
  }

  public void setAnimations(Map<String, AnimationData> animations) {
    this.animations = animations;
  }

  public void setAnimationFrameLength(String animationName, double length) {
    animations.get(animationName).setFrameLength(length);
  }

  public void addFrame(String frameName, FrameData frame) {
    this.frames.put(frameName, frame);
  }

  public void removeFrame(String frameName, FrameData frame) {
    this.frames.remove(frameName);
  }

  public void renameFrame(String oldName, String newName) {
    this.frames.put(newName, frames.get(oldName));
    this.frames.remove(oldName);
  }

  public void addAnimationFrame(String animationName, String frameName) {
    this.animations.get(animationName).addFrameName(frameName);
  }

  public void removeAnimationFrame(String animationName, String frameName) {
    this.animations.get(animationName).removeFrameName(frameName);
  }

  public void renameAnimation(String oldName, String newName) {
    this.animations.put(newName, animations.get(oldName));
    this.animations.remove(oldName);
  }

  public void setFrameLength(String animationName, double frameLength) {
    this.animations.get(animationName).setFrameLength(frameLength);
  }

  public void setSpritePath(String spritePath) {
    this.spritePath = spritePath;
  }
}
