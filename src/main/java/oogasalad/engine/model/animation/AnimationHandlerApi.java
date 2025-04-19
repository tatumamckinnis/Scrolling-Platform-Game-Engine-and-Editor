package oogasalad.engine.model.animation;

import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.FrameData;

public interface AnimationHandlerApi{

  /**
   * gets the current frame in the animation for the controller to set the current frame.
   *
   * @param gameObject the game object that contains the sprite info and Animation data.
   */
  public FrameData getCurrentFrameInAnimation(GameObject gameObject);

  /**
   *  Sets the current gameObject's currentImage to the baseImage in the spriteData.
   *
   *  @param gameObject the game object that contains the sprite info which contains the base image.
   */
  public void goToBaseImage(GameObject gameObject);


  /**
   * Adds the animation to the animation list of that game object.
   *
   * @param gameObject the game object that contains the sprite info.
   * @param AnimationName the name of the animation that needs to run.
   */
  public void addToAnimations(GameObject gameObject,String AnimationName);

  /**
   * Clears the animation list for that object and sets the animation to the specified animation.
   *
   * @param gameObject the game object that contains the sprite info.
   * @param AnimationName the name of the animation that needs to run.
   */
  public void clearAndAddToAnimationList(GameObject gameObject,String AnimationName);

  /**
   * Sets a new base image for the game object.
   *
   *
   * @param gameObject the game object that contains the sprite info.
   */
  public void setBaseImage(GameObject gameObject, String newBaseImage);

}
