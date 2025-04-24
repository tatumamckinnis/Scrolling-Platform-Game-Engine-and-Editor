package oogasalad.editor.model.data.object.event;

/**
 * Data container for physics-related properties and events of an editor object. Includes velocity,
 * gravity, jump force, and potentially physics-related event bindings. This class extends
 * {@link AbstractEventMapData}, enabling event binding and management.
 * <p>
 * It is primarily used to configure physical behavior for game objects in the editor.
 *
 * @author Jacob You
 */
public class PhysicsData extends AbstractEventMapData {

  /**
   * Horizontal velocity of the object.
   */
  private double velocityX;

  /**
   * Vertical velocity of the object.
   */
  private double velocityY;

  /**
   * Gravitational acceleration applied to the object.
   */
  private double gravity;

  /**
   * Force applied to the object when it jumps.
   */
  private double jumpForce;

  /**
   * Default constructor initializing with an empty event map and default physics values. All
   * numeric values are initialized to 0.
   */
  public PhysicsData() {
    super();
    this.velocityX = 0.0;
    this.velocityY = 0.0;
    this.gravity = 0.0;   // Default gravity
    this.jumpForce = 0.0; // Default jump force
  }

  /**
   * Returns the horizontal velocity of the object.
   *
   * @return the current X velocity
   */
  public double getVelocityX() {
    return velocityX;
  }

  /**
   * Returns the vertical velocity of the object.
   *
   * @return the current Y velocity
   */
  public double getVelocityY() {
    return velocityY;
  }

  /**
   * Returns the gravity value applied to the object.
   *
   * @return the gravitational acceleration
   */
  public double getGravity() {
    return gravity;
  }

  /**
   * Returns the jump force applied to the object.
   *
   * @return the force applied when jumping
   */
  public double getJumpForce() {
    return jumpForce;
  }

  /**
   * Sets the horizontal velocity of the object.
   *
   * @param velocityX the X-axis velocity to set
   */
  public void setVelocityX(double velocityX) {
    this.velocityX = velocityX;
  }

  /**
   * Sets the vertical velocity of the object.
   *
   * @param velocityY the Y-axis velocity to set
   */
  public void setVelocityY(double velocityY) {
    this.velocityY = velocityY;
  }

  /**
   * Sets the gravitational acceleration affecting this object. A positive value typically means
   * downward acceleration.
   *
   * @param gravity the gravity value to apply
   */
  public void setGravity(double gravity) {
    this.gravity = gravity;
  }

  /**
   * Sets the force applied when the object jumps. This is often used as an initial upward
   * velocity.
   *
   * @param jumpForce the jump force to apply
   */
  public void setJumpForce(double jumpForce) {
    this.jumpForce = jumpForce;
  }
}
