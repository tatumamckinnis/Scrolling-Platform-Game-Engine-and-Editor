package oogasalad.engine.model.object;

import java.io.File;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import oogasalad.Main;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;

/**
 * Represents the visual rendering data for a game object, including static frames and animations.
 *
 * <p>A {@code Sprite} holds a set of named frames (images), animation data, and positioning
 * offsets
 * for how the sprite should be rendered relative to the object’s hitbox. The {@code Sprite} may
 * represent the current visual appearance using a base frame, and can retrieve other frames by ID.
 *
 * <p>This class serves as a core part of the visual model and is used during rendering to
 * determine
 * what image to show and where.
 */
public class Sprite {

  /**
   * Exception messages loaded from resource bundle for consistent error handling
   */
  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackage().getName() + "." + "Exceptions");

  private int spriteDx;
  private int spriteDy;
  private FrameData currentSprite;
  private Map<String, FrameData> frameMap;
  private Map<String, AnimationData> animations;
  private File spriteFile;
  private String currAnimation;
  private int frameNumber;
  private int animationNumber;
  private FrameData baseSprite;
  private boolean needsFlipped;
  private double rotation;


  /**
   * Constructs a new {@code Sprite} with the provided frame data and rendering offsets.
   *
   * @param frameMap      a map of named frames (images) used by this sprite
   * @param currentSprite the default or base frame to be shown initially
   * @param animations    a map of animation sequences (not currently used in this class)
   * @param spriteDx      the horizontal offset of the sprite relative to the hitbox
   * @param spriteDy      the vertical offset of the sprite relative to the hitbox
   */
  public Sprite(Map<String, FrameData> frameMap, FrameData currentSprite,
      Map<String, AnimationData> animations, int spriteDx, int spriteDy, File spriteFile, double rotation, boolean needsFlipped) {
    this.spriteDx = spriteDx;
    this.spriteDy = spriteDy;
    this.frameMap = frameMap;
    this.currentSprite = currentSprite;
    this.baseSprite = currentSprite;
    this.animations = animations;
    this.spriteFile = spriteFile;
    this.frameNumber = 0;
    this.animationNumber = 0;
    this.currAnimation = "base";
    this.needsFlipped = needsFlipped;
    this.rotation = rotation;
  }

  /**
   * Returns the horizontal offset of the sprite relative to the object's hitbox.
   *
   * @return x-offset in pixels
   */
  public int getSpriteDx() {
    return spriteDx;
  }

  /**
   * Returns the vertical offset of the sprite relative to the object's hitbox.
   *
   * @return y-offset in pixels
   */
  public int getSpriteDy() {
    return spriteDy;
  }

  /**
   * Returns the currently active frame (image) used for rendering this sprite.
   *
   * @return the current {@link FrameData}
   */
  public FrameData getCurrentSprite() {
    return currentSprite;
  }

  /**
   * Returns the full map of frame IDs to their corresponding frame data.
   *
   * @return map of frame names to {@link FrameData}
   */
  public Map<String, FrameData> getFrameMap() {
    return frameMap;
  }

  /**
   * Retrieves a frame by its ID from the frame map.
   *
   * @param id the ID/name of the frame to retrieve
   * @return the {@link FrameData} associated with the given ID
   * @throws NoSuchElementException if the frame ID does not exist
   */
  public FrameData getSprite(String id) {
    if (frameMap.containsKey(id)) {
      return frameMap.get(id);
    }
    throw new NoSuchElementException(EXCEPTIONS.getString("SpriteNotFound"));
  }

  /**
   * @return the necessary Sprite File
   */
  public File getSpriteFile() {
    return spriteFile;
  }

  /**
   *
   * @return the current animation the sprite is on
   */
  public String getCurrAnimation() {
    return currAnimation;
  }

  /**
   *
   * @return the current frame number in relation to framelen of the animation
   */
  public int getFrameNumber() {
    return frameNumber;
  }

  /**
   *
   *
   * @return the current animation number in relation to the List of frames  in the animation data
   */
  public int getAnimationNumber() {
    return animationNumber;
  }

  /**
   *
   *
   * @return all the animations that the sprite has.
   */
  public  Map<String, AnimationData> getAnimations() {
    return animations;
  }

  /**
   *
   *
   */
    public FrameData getBaseSprite() {
      return baseSprite;
    }

    public void setCurrentSprite(FrameData currentSprite) {
      this.currentSprite = currentSprite;
    }

    public void setBaseSprite(FrameData baseSprite) {
      this.baseSprite = baseSprite;
    }


    /**
     *
     *
     * @return a boolean if the object is flipped by default for most objects flipping will have the object face left.
     */
    public boolean needsFlipped() {
      return needsFlipped;
    }


    public void setNeedsFlipped(boolean needsFlipped) {
      this.needsFlipped = needsFlipped;
    }

  /**
   *
   * @return the rotation of the object about its center.
   */
  public double getRotation() {
      return rotation;
    }

  /**
   *
   *
   * @param rotation the rotation of the sprite image.
   */
  public void setRotation(double rotation) {
      this.rotation = rotation;
    }

}

