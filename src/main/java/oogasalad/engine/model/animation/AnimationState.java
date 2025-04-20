package oogasalad.engine.model.animation;

import java.util.ArrayDeque;
import java.util.Deque;
import oogasalad.engine.model.object.Sprite;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;

/**
 * Represents the current animation playback state for a single GameObject.
 * It maintains a queue of animations, frame indices, and tick counters,
 * and provides the next frame to render on each game loop iteration.
 */
public class AnimationState {
  private final Deque<String> queue = new ArrayDeque<>();
  private int frameIndex = 0;
  private int frameTick  = 0;

  /**
   * Clears all pending animations and resets the frame index and tick counter.
   * After this call, nextFrame(...) will return the sprite's base frame.
   */
  public void goToBase() {
    queue.clear();
    frameIndex = frameTick = 0;
  }

  /**
   * Enqueues an animation to play after existing ones complete.
   * @param animationName the key of the AnimationData in the Sprite's map
   */
  public void addAnimation(String animationName) {
    queue.addLast(animationName);
  }

  /**
   * Clears any currently queued animations and starts playing only the specified animation
   * from its first frame.
   * @param animationName the key of the AnimationData to play immediately
   */
  public void clearAndPlay(String animationName) {
    queue.clear();
    queue.addLast(animationName);
    frameIndex = frameTick = 0;
  }

  /**
   * Advances the animation by one game tick and returns the FrameData to render.
   * <ul>
   *   <li>If the queue is empty, the sprite's base frame is returned.</li>
   *   <li>Otherwise, it plays through the queued animations, popping each off when complete.</li>
   * </ul>
   * @param sprite the Sprite containing animation definitions and frame mappings
   * @return the FrameData for the current animation frame, or the base frame if idle
   */
  public FrameData nextFrame(Sprite sprite) {
    if (queue.isEmpty()) {
      return sprite.getBaseSprite();
    }

    String animName = queue.peekFirst();
    AnimationData data = sprite.getAnimations().get(animName);
    if (data == null) {
      // Unknown animation: drop it and retry
      queue.removeFirst();
      frameIndex = frameTick = 0;
      return nextFrame(sprite);
    }

    String frameName = data.frameNames().get(frameIndex);
    FrameData frame = sprite.getFrameMap().get(frameName);
    if (frame == null) {
      // Missing frame: drop this animation and retry
      queue.removeFirst();
      frameIndex = frameTick = 0;
      return nextFrame(sprite);
    }

    // Advance tick; cycle frames or remove animation when done
    frameTick++;
    if (frameTick >= data.frameLen()) {
      frameTick = 0;
      frameIndex++;
      if (frameIndex >= data.frameNames().size()) {
        queue.removeFirst();
        frameIndex = 0;
      }
    }

    return frame;
  }
}