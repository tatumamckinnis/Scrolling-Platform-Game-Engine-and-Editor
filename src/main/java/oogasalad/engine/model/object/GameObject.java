package oogasalad.engine.model.object;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;

/**
 * Abstract representation of a game object within the engine.
 *
 * <p>Each {@code GameObject} has a unique identifier (UUID), blueprint metadata,
 * hitbox dimensions, sprite rendering data, and runtime parameters. It can also contain a list of
 * {@link Event} objects that define how the object behaves during gameplay.
 *
 * <p>This class serves as a base for specific object types such as players, enemies, or terrain.
 * Subclasses should implement behavior and game-specific logic as needed.
 *
 * <p>GameObjects also handle basic movement logic including gravity and bounds clamping.
 *
 * @author Alana Zinkin
 */
public abstract class GameObject {

  private final UUID uuid;
  private int myBlueprintID;
  private String myType;
  private int myHitBoxX;
  private int myHitBoxY;
  private int myHitBoxWidth;
  private int myHitBoxHeight;
  private int myLayer;
  private String myName;
  private String myGroup;
  private FrameData myCurrentFrame;
  private SpriteData mySpriteData;
  private Map<String, FrameData> myFrameMap;
  private Map<String, AnimationData> myAnimationMap;
  private Map<String, String> myParams;
  private List<Event> myEvents;
  private HitBoxData myHitBoxData;
  private double xVelocity;
  private double yVelocity;
  private boolean isGrounded;

  /**
   * Constructs a new GameObject with the provided attributes.
   *
   * @param uuid         unique identifier for the object
   * @param blueprintID  ID associated with the object's blueprint
   * @param type         the type/category of the object
   * @param hitBoxX      the x-position of the hitbox
   * @param hitBoxY      the y-position of the hitbox
   * @param hitBoxWidth  width of the hitbox
   * @param hitBoxHeight height of the hitbox
   * @param layer        the rendering layer of the object
   * @param name         the display name of the object
   * @param group        the group or category the object belongs to
   * @param spriteData   image and animation details for the object
   * @param currentFrame the current frame to render
   * @param frameMap     map of named frames
   * @param animationMap map of animations
   * @param params       map of runtime parameters
   * @param events       list of game-triggered events associated with the object
   * @param hitBoxData   hitbox offset and size information
   */
  public GameObject(UUID uuid, int blueprintID, String type, int hitBoxX, int hitBoxY,
      int hitBoxWidth, int hitBoxHeight, int layer, String name, String group,
      SpriteData spriteData, FrameData currentFrame, Map<String, FrameData> frameMap,
      Map<String, AnimationData> animationMap, Map<String, String> params,
      List<Event> events, HitBoxData hitBoxData) {
    this.uuid = uuid;
    this.myBlueprintID = blueprintID;
    this.myType = type;
    this.myHitBoxX = hitBoxX;
    this.myHitBoxY = hitBoxY;
    this.myHitBoxWidth = hitBoxWidth;
    this.myHitBoxHeight = hitBoxHeight;
    this.myHitBoxData = hitBoxData;
    this.myLayer = layer;
    this.myName = name;
    this.myGroup = group;
    this.mySpriteData = spriteData;
    this.myCurrentFrame = currentFrame;
    this.myFrameMap = frameMap;
    this.myAnimationMap = animationMap;
    this.myParams = params;
    this.myEvents = events;
    this.isGrounded = true;
  }

  /**
   * Updates the object's position based on current velocity, and applies bounds clamping to prevent
   * falling below ground or exiting frame.
   */
  public void updatePosition() {
    myHitBoxX += xVelocity;
    myHitBoxY += yVelocity;

    // Clamp object to the ground
    if (myHitBoxY >= 500 - myHitBoxHeight) {
      isGrounded = true;
      myHitBoxY = 500 - myHitBoxHeight;
    }

    // Clamp object to the right boundary
    if (myHitBoxX >= 500 - myHitBoxWidth) {
      myHitBoxX = 500 - myHitBoxWidth;
    }

    // Optionally clamp left boundary
    // if (myHitBoxX < 0) {
    //   myHitBoxX = 0;
    //   xVelocity *= -1;
    // }
  }

  /**
   * @return the UUID of the object as a string
   */
  public String getUuid() {
    return uuid.toString();
  }

  /**
   * @return the object's blueprint ID
   */
  public int getBlueprintID() {
    return myBlueprintID;
  }

  /**
   * @return the object type/category
   */
  public String getType() {
    return myType;
  }

  /**
   * @return the object's x-position
   */
  public int getX() {
    return myHitBoxX;
  }

  /**
   * @return the object's y-position
   */
  public int getY() {
    return myHitBoxY;
  }

  /**
   * @return the object's hitbox width
   */
  public int getWidth() {
    return myHitBoxWidth;
  }

  /**
   * @return the object's hitbox height
   */
  public int getHeight() {
    return myHitBoxHeight;
  }

  /**
   * @return the current frame data used for rendering
   */
  public FrameData getCurrentFrame() {
    return myCurrentFrame;
  }

  /**
   * @return hitbox width
   */
  public int getHitBoxWidth() {
    return myHitBoxWidth;
  }

  /**
   * @return hitbox height
   */
  public int getHitBoxHeight() {
    return myHitBoxHeight;
  }

  /**
   * @return object’s hitbox metadata
   */
  public HitBoxData getmyHitBoxData() {
    return myHitBoxData;
  }

  /**
   * @return sprite rendering data
   */
  public SpriteData getSpriteData() {
    return mySpriteData;
  }

  /**
   * @return sprite's horizontal offset from hitbox
   */
  public int getSpriteDx() {
    return myHitBoxData.spriteDx();
  }

  /**
   * @return sprite's vertical offset from hitbox
   */
  public int getSpriteDy() {
    return myHitBoxData.spriteDy();
  }

  /**
   * @return the render layer index for drawing order
   */
  public int getLayer() {
    return myLayer;
  }

  /**
   * @return the display name of the object
   */
  public String getName() {
    return myName;
  }

  /**
   * @return the group or category the object belongs to
   */
  public String getGroup() {
    return myGroup;
  }

  /**
   * @return the object’s runtime parameters
   */
  public Map<String, String> getParams() {
    return myParams;
  }

  /**
   * @return list of events associated with this object
   */
  public List<Event> getEvents() {
    return myEvents;
  }

  /**
   * @param xPos new x-position for the object
   */
  public void setX(int xPos) {
    this.myHitBoxX = xPos;
  }

  /**
   * @param yPos new y-position for the object
   */
  public void setY(int yPos) {
    this.myHitBoxY = yPos;
  }

  /**
   * @param width new hitbox width
   */
  public void setWidth(int width) {
    this.myHitBoxWidth = width;
  }

  /**
   * @param height new hitbox height
   */
  public void setHeight(int height) {
    this.myHitBoxHeight = height;
  }

  /**
   * @param group the new group/category name
   */
  public void setGroup(String group) {
    this.myGroup = group;
  }

  /**
   * @param events new list of events to attach to this object
   */
  public void setEvents(List<Event> events) {
    myEvents = events;
  }

  /**
   * Sets the current animation frame based on the frame name.
   *
   * @param frameName the name of the frame to display
   */
  public void setCurrentFrame(String frameName) {
    myCurrentFrame = myFrameMap.getOrDefault(frameName, null);
  }

  /**
   * @return current x-axis velocity
   */
  public double getXVelocity() {
    return xVelocity;
  }

  /**
   * @return current y-axis velocity
   */
  public double getYVelocity() {
    return yVelocity;
  }

  /**
   * @param xVelocity new horizontal velocity
   */
  public void setXVelocity(double xVelocity) {
    this.xVelocity = xVelocity;
  }

  /**
   * @param yVelocity new vertical velocity
   */
  public void setYVelocity(double yVelocity) {
    this.yVelocity = yVelocity;
  }

  /**
   * @return whether the object is currently grounded (i.e. touching ground)
   */
  public boolean isGrounded() {
    return isGrounded;
  }

  /**
   * @param grounded updates the grounded status of the object
   */
  public void setGrounded(boolean grounded) {
    isGrounded = grounded;
  }
}
