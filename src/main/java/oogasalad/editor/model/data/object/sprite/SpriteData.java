
package oogasalad.editor.model.data.object.sprite;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates sprite data for an editor object. This class stores sprite properties such as
 * position, frame data, animations, and the path to the sprite's image file. It provides methods to
 * access and modify these properties including frame and animation management.
 *
 * @author Jacob You
 */
public class SpriteData {

  private String name;
  private int x;
  private int y;
  private double rotation;
  private Map<String, FrameData> frames;
  private Map<String, AnimationData> animations;
  private String spritePath;
  private String baseFrame; // Added field
  private boolean isFlipped;

  /**
   * Constructs a new SpriteData instance with the specified properties.
   *
   * @param name       the unique name of the sprite
   * @param x          the x-coordinate of the sprite's position
   * @param y          the y-coordinate of the sprite's position
   * @param rotation   the rotation of the sprite
   * @param isFlipped  a boolean of whether an object is flipped or not.
   * @param frames     a map of frame names to their corresponding {@link FrameData}
   * @param animations a map of animation names to their corresponding {@link AnimationData}
   * @param spritePath the file path to the sprite image
   */
  public SpriteData(String name, int x, int y, double rotation, boolean isFlipped, Map<String, FrameData> frames,
      Map<String, AnimationData> animations, String spritePath) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.isFlipped = isFlipped;
    this.rotation = rotation;
    this.frames = frames;
    this.animations = animations;
    this.spritePath = spritePath;
  }

  /**
   * Adds a new animation to the sprite.
   *
   * @param name The name of the animation.
   * @param animation The AnimationData to add.
   */
  public void addAnimation(String name, AnimationData animation) {
    this.animations.put(name, animation);
  }

  /**
   * Retrieves the name of the sprite.
   *
   * @return the name of the sprite
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves the x-coordinate of the sprite's position.
   *
   * @return the x-coordinate
   */
  public int getX() {
    return x;
  }

  /**
   * Retrieves the y-coordinate of the sprite's position.
   *
   * @return the y-coordinate
   */
  public int getY() {
    return y;
  }

  /**
   * Retrieves the rotation of the sprite.
   *
   * @return the rotation in angles
   */
  public double getRotation() {
    return rotation;
  }

  /**
   * Retrieves the map of frame data for the sprite.
   *
   * @return a map where keys are frame names and values are {@link FrameData} objects
   */
  public Map<String, FrameData> getFrames() {
    return frames;
  }

  /**
   * Retrieves the map of animation data for the sprite.
   *
   * @return a map where keys are animation names and values are {@link AnimationData} objects
   */
  public Map<String, AnimationData> getAnimations() {
    return animations;
  }

  /**
   * Retrieves the file path to the sprite image.
   *
   * @return the sprite image file path as a String
   */
  public String getSpritePath() {
    return spritePath;
  }

  /**
   * Retrieves frame data for a specific frame by name.
   *
   * @param frameName the name of the frame to retrieve
   * @return the corresponding {@link FrameData}, or null if not found
   */
  public FrameData getFrame(String frameName) {
    return frames.get(frameName);
  }

  /**
   * Retrieves the list of frame names for a specified animation.
   *
   * @param animationName the name of the animation
   * @return a {@link List} of frame names that compose the animation
   */
  public List<String> getAnimationFrameNames(String animationName) {
    return animations.get(animationName).getFrameNames();
  }

  /**
   * Retrieves animation data for a specified animation.
   *
   * @param animationName the name of the animation to retrieve
   * @return the corresponding {@link AnimationData}, or null if not found
   */
  public AnimationData getAnimation(String animationName) {
    return animations.get(animationName);
  }

  /**
   * Retrieves the frame length (duration) for a specified animation.
   *
   * @param animationName the name of the animation
   * @return the frame length in seconds
   */
  public double getAnimationFrameLength(String animationName) {
    return animations.get(animationName).getFrameLength();
  }

  /**
   * Sets the name of the sprite.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the x-coordinate of the sprite's position.
   *
   * @param x the new x-coordinate
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Sets the y-coordinate of the sprite's position.
   *
   * @param y the new y-coordinate
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * Sets the rotation of the sprite.
   *
   * @param rotation the new rotation
   */
  public void setRotation(double rotation) {
    this.rotation = rotation;
  }

  /**
   * Replaces the current frame data map with a new map.
   *
   * @param frames a map where keys are frame names and values are {@link FrameData} objects
   */
  public void setFrames(Map<String, FrameData> frames) {
    this.frames = frames;
  }

  /**
   * Replaces the current animation data map with a new map.
   *
   * @param animations a map where keys are animation names and values are {@link AnimationData}
   * objects
   */
  public void setAnimations(Map<String, AnimationData> animations) {
    this.animations = animations;
  }

  /**
   * Sets a new frame length for a specified animation.
   *
   * @param animationName the name of the animation to update
   * @param length        the new frame length in seconds
   */
  public void setAnimationFrameLength(String animationName, double length) {
    animations.get(animationName).setFrameLength(length);
  }

  /**
   * Adds a new frame to the sprite.
   *
   * @param frameName the name for the new frame
   * @param frame     the corresponding {@link FrameData} object
   */
  public void addFrame(String frameName, FrameData frame) {
    this.frames.put(frameName, frame);
  }

  /**
   * Removes a frame from the sprite.
   *
   * @param frameName the name of the frame to remove
   * @param frame     the {@link FrameData} associated with the frame
   */
  public void removeFrame(String frameName, FrameData frame) {
    this.frames.remove(frameName);
  }

  /**
   * Renames an existing frame.
   *
   * @param oldName the current name of the frame
   * @param newName the new name to assign to the frame
   */
  public void renameFrame(String oldName, String newName) {
    this.frames.put(newName, frames.get(oldName));
    this.frames.remove(oldName);
  }

  /**
   * Adds a frame name to a specified animation.
   *
   * @param animationName the name of the animation to update
   * @param frameName     the frame name to add to the animation sequence
   */
  public void addAnimationFrame(String animationName, String frameName) {
    this.animations.get(animationName).addFrameName(frameName);
  }

  /**
   * Removes a frame name from a specified animation.
   *
   * @param animationName the name of the animation to update
   * @param frameName     the frame name to remove from the animation sequence
   */
  public void removeAnimationFrame(String animationName, String frameName) {
    this.animations.get(animationName).removeFrameName(frameName);
  }

  /**
   * Renames an existing animation.
   *
   * @param oldName the current name of the animation
   * @param newName the new name to assign to the animation
   */
  public void renameAnimation(String oldName, String newName) {
    this.animations.put(newName, animations.get(oldName));
    this.animations.remove(oldName);
  }

  /**
   * Sets a new frame length (duration) for a specified animation.
   *
   * @param animationName the name of the animation to update
   * @param frameLength   the new frame length in seconds
   */
  public void setFrameLength(String animationName, double frameLength) {
    this.animations.get(animationName).setFrameLength(frameLength);
  }

  /**
   * Sets a new file path for the sprite image.
   *
   * @param spritePath the new sprite image file path
   */
  public void setSpritePath(String spritePath) {
    this.spritePath = spritePath;
  }

  /**
   * Sets the base frame of the sprite.
   *
   * @param baseFrame the new base frame
   */
  public void setBaseFrame(String baseFrame) {
    this.baseFrame = baseFrame;
  }

  /**
   *
   *
   */
  public boolean getIsFlipped() {
    return isFlipped;
  }

  public void setIsFlipped(boolean isFlipped) {
    this.isFlipped = isFlipped;
  }

}