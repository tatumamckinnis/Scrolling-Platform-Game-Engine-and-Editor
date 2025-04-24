package oogasalad.engine.model.object;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.model.event.Event;
import oogasalad.fileparser.records.FrameData;

/**
 * Abstract representation of a dynamic object in the game world.
 *
 * <p>Each {@code GameObject} has a unique identifier (UUID), type, position, velocity,
 * hitbox for collisions, sprite data for rendering, and associated events and parameters.
 *
 * <p>It provides basic functionality like movement based on velocity and grounding logic,
 * which can be extended by specific subclasses like {@link Player}, enemies, or environment
 * objects.
 *
 * @author Alana Zinkin
 */
public abstract class GameObject implements ImmutableGameObject{

  private UUID uuid;
  private String type;
  private int layer;
  private double xVelocity;
  private double yVelocity;
  private HitBox hitBox;
  private Sprite spriteInfo;
  private List<Event> events;
  private Map<String, String> stringParams;
  private Map<String, Double> doubleParams;
  private boolean isGrounded;

  /**
   * Constructs a new {@code GameObject} with all necessary components.
   *
   * @param uuid         unique identifier
   * @param type         the category/type of the object (e.g., "Player", "Enemy")
   * @param layer        rendering layer (used for draw order)
   * @param xVelocity    initial horizontal velocity
   * @param yVelocity    initial vertical velocity
   * @param hitBox       spatial boundaries and collision area
   * @param spriteInfo   visual rendering data for this object
   * @param events       list of events associated with the object
   * @param stringParams string-based runtime parameters
   * @param doubleParams numeric runtime parameters
   */
  public GameObject(UUID uuid, String type, int layer, double xVelocity, double yVelocity,
      HitBox hitBox, Sprite spriteInfo, List<Event> events,
      Map<String, String> stringParams, Map<String, Double> doubleParams) {
    this.uuid = uuid;
    this.type = type;
    this.layer = layer;
    this.xVelocity = xVelocity;
    this.yVelocity = yVelocity;
    this.hitBox = hitBox;
    this.spriteInfo = spriteInfo;
    this.events = events;
    this.stringParams = stringParams;
    this.doubleParams = doubleParams;
    this.isGrounded = true;
  }

  /**
   * Updates the object's position based on current velocity and clamps position to within screen
   * bounds (default 500x500).
   */
  public void updatePosition() {
    setXPosition((int) (getXPosition() + xVelocity));
    setYPosition((int) (getYPosition() + yVelocity));

    //hardcoded floor, should be refactored later
    if (getYPosition() >= 500 - getHitBoxHeight()) {
      isGrounded = true;
      setYPosition(500 - getHitBoxHeight());
    }

  }

  /**
   * @return the UUID of the object as a string
   */
  @Override
  public String getUUID() {
    return uuid.toString();
  }

  /**
   * @return the object type/category
   */
  public String getType() {
    return type;
  }

  /**
   * @return the render layer this object belongs to
   */
  @Override
  public int getLayer() {
    return layer;
  }

  /**
   * @return current horizontal velocity
   */
  public double getXVelocity() {
    return xVelocity;
  }

  /**
   * @return current vertical velocity
   */
  public double getYVelocity() {
    return yVelocity;
  }

  // --- HitBox Getters ---

  /**
   * @return x-coordinate of the hitbox
   */
  @Override
  public int getXPosition() {
    return hitBox.getX();
  }

  /**
   * @return y-coordinate of the hitbox
   */
  @Override
  public int getYPosition() {
    return hitBox.getY();
  }

  /**
   * @return width of the hitbox
   */
  @Override
  public int getHitBoxWidth() {
    return hitBox.getWidth();
  }

  /**
   * @return height of the hitbox
   */
  @Override
  public int getHitBoxHeight() {
    return hitBox.getHeight();
  }

  // --- Sprite Getters ---

  /**
   * @return x-offset of the sprite relative to the hitbox
   */
  @Override
  public int getSpriteDx() {
    return spriteInfo.getSpriteDx();
  }

  /**
   * @return y-offset of the sprite relative to the hitbox
   */
  @Override
  public int getSpriteDy() {
    return spriteInfo.getSpriteDy();
  }

  /**
   * @return the current frame of the sprite for rendering
   */
  @Override
  public FrameData getCurrentFrame() {
    return spriteInfo.getCurrentSprite();
  }

  /**
   * @return list of events attached to this object
   */
  public List<Event> getEvents() {
    return events;
  }

  /**
   * @return map of string parameters used by this object
   */
  public Map<String, String> getStringParams() {
    return stringParams;
  }

  /**
   * @return map of double parameters used by this object
   */
  public Map<String, Double> getDoubleParams() {
    return doubleParams;
  }

  /**
   * Sets the object's event list.
   *
   * @param events the new list of events
   */
  public void setEvents(List<Event> events) {
    this.events = events;
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
   * Sets the x-coordinate of the object's hitbox.
   *
   * @param x new x-position
   */
  public void setXPosition(int x) {
    hitBox.setX(x);
  }

  /**
   * Sets the y-coordinate of the object's hitbox.
   *
   * @param y new y-position
   */
  public void setYPosition(int y) {
    hitBox.setY(y);
  }

  /**
   * Updates whether the object is grounded (i.e., touching a surface).
   *
   * @param grounded true if grounded, false otherwise
   */
  public void setGrounded(boolean grounded) {
    isGrounded = grounded;
  }

  /**
   * @return true if the object is grounded (on a surface), false otherwise
   */
  public boolean isGrounded() {
    return isGrounded;
  }

  /**
   * @return File for the sprite
   */
  @Override
  public File getSpriteFile() {
    return spriteInfo.getSpriteFile();
  }

  @Override
  public boolean getNeedsFlipped() {
    return spriteInfo.needsFlipped();
  }

  @Override
  public void setNeedsFlipped(boolean didFlip) {
    this.spriteInfo.setNeedsFlipped(didFlip);
  }

  @Override
  public double getRotation() {
    return spriteInfo.getRotation();
  }

  /**
   * @return the sprite data for the game object.
   */
  public Sprite getSpriteInfo() {
    return spriteInfo;
  }

  /**
   * sets the current frame of the animation
   * @param currentFrame the frame to set the sprite to
   */
  public void setCurrentFrame(FrameData currentFrame) {
    spriteInfo.setCurrentSprite(currentFrame);
  }
}

