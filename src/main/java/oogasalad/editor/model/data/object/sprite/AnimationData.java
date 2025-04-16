package oogasalad.editor.model.data.object.sprite;

import java.util.List;

/**
 * Represents the animation data for a sprite, including the duration of each frame and the sequence
 * of frame names.
 *
 * @author Jacob You
 */
public class AnimationData {

  private double frameLength;
  private List<String> frameNames;

  /**
   * Constructs an AnimationData instance with the specified frame length and frame names.
   * @param frameLength the duration of each frame in the animation (in seconds)
   * @param frameNames  a list of frame names that compose the animation sequence
   */
  public AnimationData(double frameLength, List<String> frameNames) {
    this.frameLength = frameLength;
    this.frameNames = frameNames;
  }
  /**
   * Retrieves the duration of each frame in the animation.
   *
   * @return the frame length in seconds
   */
  public double getFrameLength() {
    return frameLength;
  }

  /**
   * Retrieves the list of frame names in the animation sequence.
   *
   * @return a list of frame name strings
   */
  public List<String> getFrameNames() {
    return frameNames;
  }

  /**
   * Sets a new duration for each frame in the animation.
   *
   * @param frameLength the new frame length in seconds
   */
  public void setFrameLength(double frameLength) {
    this.frameLength = frameLength;
  }

  /**
   * Adds a frame name to the animation sequence.
   *
   * @param frameName the frame name to add
   */
  public void addFrameName(String frameName) {
    this.frameNames.add(frameName);
  }

  /**
   * Removes a frame name from the animation sequence.
   *
   * @param frameName the frame name to remove
   */
  public void removeFrameName(String frameName) {
    this.frameNames.remove(frameName);
  }
}