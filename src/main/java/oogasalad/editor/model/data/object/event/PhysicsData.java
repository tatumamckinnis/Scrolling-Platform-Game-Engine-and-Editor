package oogasalad.editor.model.data.object.event;
/**
 * Data container for physics-related properties and events of an editor object.
 * Includes velocity, gravity, jump force, and potentially physics-related event bindings.
 *
 * @author Jacob You
 */
public class PhysicsData extends AbstractEventMapData {

  private double velocityX;
  private double velocityY;
  private double gravity;
  private double jumpForce;


  /**
   * Default constructor initializing with empty event map and default physics values.
   */
  public PhysicsData() {
    super();
    this.velocityX = 0.0;
    this.velocityY = 0.0;
    this.gravity = 0.0;   // Default gravity
    this.jumpForce = 0.0; // Default jump force
  }


  public double getVelocityX() {
    return velocityX;
  }

  public double getVelocityY() {
    return velocityY;
  }

  public double getGravity() {
    return gravity;
  }

  public double getJumpForce() {
    return jumpForce;
  }


  public void setVelocityX(double velocityX) {
    this.velocityX = velocityX;
  }

  public void setVelocityY(double velocityY) {
    this.velocityY = velocityY;
  }

  /**
   * Sets the gravitational acceleration affecting this object.
   * A positive value typically means downward acceleration.
   *
   * @param gravity The gravity value.
   */
  public void setGravity(double gravity) {
    this.gravity = gravity;
  }

  /**
   * Sets the force applied when the object jumps.
   * This is often an initial upward velocity change.
   *
   * @param jumpForce The jump force value.
   */
  public void setJumpForce(double jumpForce) {
    this.jumpForce = jumpForce;
  }

}