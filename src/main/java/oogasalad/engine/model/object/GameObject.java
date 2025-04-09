package oogasalad.engine.model.object;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.FrameData;

public abstract class GameObject {

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

  public GameObject(UUID uuid, String type, int layer, double xVelocity, double yVelocity,
      HitBox hitBox, Sprite spriteInfo, List<Event> events, Map<String, String> stringParams, Map<String, Double> doubleParams) {
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
   * Updates the object's position based on current velocity, and applies bounds clamping to prevent
   * falling below ground or exiting frame.
   */
  public void updatePosition() {
    setXPosition((int) (getXPosition() + xVelocity));
    setYPosition((int) (getYPosition() + yVelocity));

    // Clamp object to the ground
    if (getYPosition() >= 500 - getHitBoxHeight()) {
      isGrounded = true;
      setYPosition(500 - getHitBoxHeight());
    }

    // Clamp object to the right boundary
    if (getXPosition() >= 500 - getHitBoxWidth()) {
      setXPosition(500 - getHitBoxWidth());
    }

//     Optionally clamp left boundary
//     if (getXPosition() < 0) {
//       setXPosition(0);
//       xVelocity *= -1;
//     }
  }

  public String getUUID() {
    return uuid.toString();
  }

  public String getType() {
    return type;
  }

  public int getLayer() {
    return layer;
  }

  public double getXVelocity() {
    return xVelocity;
  }

  public double getYVelocity() {
    return yVelocity;
  }

  // HitBox getters

  public int getXPosition() {
    return hitBox.getX();
  }

  public int getYPosition() {
    return hitBox.getY();
  }

  public int getHitBoxWidth() {
    return hitBox.getWidth();
  }

  public int getHitBoxHeight() {
    return hitBox.getHeight();
  }

  // Sprite Getters
  public int getSpriteDx() {
    return spriteInfo.getSpriteDx();
  }

  public int getSpriteDy() {
    return spriteInfo.getSpriteDy();
  }

  public FrameData getCurrentFrame() {
    return spriteInfo.getCurrentSprite();
  }

  public List<Event> getEvents() {
    return events;
  }

  public Map<String, String> getStringParams() {
    return stringParams;
  }

  public Map<String, Double> getDoubleParams() {
    return doubleParams;
  }

  public void setEvents(List<Event> events) {
    this.events = events;
  }

  public void setXVelocity(double xVelocity) {
    this.xVelocity = xVelocity;
  }

  public void setYVelocity(double yVelocity) {
    this.yVelocity = yVelocity;
  }

  public void setXPosition(int x) {
    hitBox.setX(x);
  }

  public void setYPosition(int y) {
    hitBox.setY(y);
  }

  /**
   * @param grounded updates the grounded status of the object
   */
  public void setGrounded(boolean grounded) {
    isGrounded = grounded;
  }

  public boolean isGrounded() {
    return isGrounded;
  }
}
