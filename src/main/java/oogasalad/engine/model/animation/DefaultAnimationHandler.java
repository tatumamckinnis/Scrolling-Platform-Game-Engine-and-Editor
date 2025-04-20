
package oogasalad.engine.model.animation;

import java.util.HashMap;
import java.util.Map;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.Sprite;
import oogasalad.fileparser.records.FrameData;

/**
 * Default implementation of {@link AnimationHandlerApi}, managing an {@link AnimationState}
 * for each GameObject by its UUID.
 * It advances and applies frame updates on each call to getCurrentFrameInAnimation().
 */
public class DefaultAnimationHandler implements AnimationHandlerApi {
  private final Map<String, AnimationState> stateMap = new HashMap<>();
  private Sprite currentSprite;
  
  /**
   * Advances the animation state for the given GameObject and updates its current frame.
   * If no animations are queued, the sprite's base frame is used.
   * @param gameObject the object whose animation to advance
   * @return the FrameData that was set as the current frame
   */
  @Override
  public FrameData getCurrentFrameInAnimation(GameObject gameObject) {
    String id = gameObject.getUUID();
    Sprite sprite = gameObject.getSpriteInfo();
    AnimationState state = stateMap.computeIfAbsent(id, k -> new AnimationState());
    FrameData next = state.nextFrame(sprite);
    return next;
  }

  /**
   * Clears any queued animations for this GameObject and immediately sets its frame to the base image.
   * @param gameObject the object whose animations should be reset
   */
  @Override
  public void goToBaseImage(GameObject gameObject) {
    AnimationState state = stateMap.get(gameObject.getUUID());
    if (state != null) {
      state.goToBase();
    }
    gameObject.setCurrentFrame(gameObject.getSpriteInfo().getBaseSprite());
  }

  /**
   * Adds an animation to the end of the GameObject's play queue.
   * @param gameObject the object to animate
   * @param animationName the key of the animation to enqueue
   */
  @Override
  public void addToAnimations(GameObject gameObject, String animationName) {
    AnimationState state = stateMap.computeIfAbsent(gameObject.getUUID(), k -> new AnimationState());
    state.addAnimation(animationName);
  }

  /**
   * Clears the GameObject's animation queue and starts the specified animation immediately.
   * @param gameObject the object to animate
   * @param animationName the key of the animation to play
   */
  @Override
  public void clearAndAddToAnimationList(GameObject gameObject, String animationName) {
    AnimationState state = stateMap.computeIfAbsent(gameObject.getUUID(), k -> new AnimationState());
    state.clearAndPlay(animationName);
  }

  /**
   * Overrides the sprite's base frame to a different named frame,
   * clears any queued animations, and resets the current frame.
   *
   * @param gameObject    the GameObject whose base you’re changing
   * @param newBaseName   the key of the new base frame in the Sprite’s frameMap
   * @throws IllegalArgumentException if no such frame exists
   */
  @Override
  public void setBaseImage(GameObject gameObject, String newBaseName) {
    Sprite spriteInfo = gameObject.getSpriteInfo();
    FrameData frameData = spriteInfo.getFrameMap().get(newBaseName);
    if (frameData == null) {
      throw new IllegalArgumentException("Frame '" + newBaseName + "' not found");
    }

    spriteInfo.setBaseSprite(frameData);
  }
}
