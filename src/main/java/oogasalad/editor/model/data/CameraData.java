package oogasalad.editor.model.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the camera configuration for a game level editor.
 *
 * <p>CameraData holds information about the camera's position, dimensions,
 * type, and customizable parameters that control behavior such as tracking and zooming. String and
 * double parameters allow flexible extension based on the specific camera type.</p>
 *
 * @author Jacob You
 */
public class CameraData {

  private final static String DEFAULT_CAMERA_TYPE = "Tracker";

  private String cameraType;
  private double cameraX;
  private double cameraY;
  private double cameraWidth;
  private double cameraHeight;
  private Map<String, String> stringParams;
  private Map<String, Double> doubleParams;

  /**
   * Constructs a new {@code CameraData} instance with default type {@code TRACKER} and empty
   * parameter maps.
   */
  public CameraData() {
    cameraType = DEFAULT_CAMERA_TYPE;
    stringParams = new HashMap<>();
    doubleParams = new HashMap<>();
  }

  /**
   * Sets the X-coordinate of the camera's position.
   *
   * @param x the X position
   */
  public void setCameraX(int x) {
    cameraX = x;
  }

  /**
   * Sets the Y-coordinate of the camera's position.
   *
   * @param y the Y position
   */
  public void setCameraY(int y) {
    cameraY = y;
  }

  /**
   * Sets the width of the camera's view.
   *
   * @param width the width in pixels
   */
  public void setCameraWidth(int width) {
    cameraWidth = width;
  }

  /**
   * Sets the height of the camera's view.
   *
   * @param height the height in pixels
   */
  public void setCameraHeight(int height) {
    cameraHeight = height;
  }

  /**
   * Sets the type of the camera.
   *
   * @param type the camera type (e.g., "TRACKER")
   */
  public void setCameraType(String type) {
    cameraType = type;
  }

  /**
   * Returns the X-coordinate of the camera's position.
   *
   * @return the camera X position
   */
  public double getCameraX() {
    return cameraX;
  }

  /**
   * Returns the Y-coordinate of the camera's position.
   *
   * @return the camera Y position
   */
  public double getCameraY() {
    return cameraY;
  }

  /**
   * Returns the width of the camera's view.
   *
   * @return the camera width
   */
  public double getCameraWidth() {
    return cameraWidth;
  }

  /**
   * Returns the height of the camera's view.
   *
   * @return the camera height
   */
  public double getCameraHeight() {
    return cameraHeight;
  }

  /**
   * Returns the string parameters associated with the camera.
   *
   * @return a map of string parameters
   */
  public Map<String, String> getStringParams() {
    return stringParams;
  }

  /**
   * Returns the double parameters associated with the camera.
   *
   * @return a map of double parameters
   */
  public Map<String, Double> getDoubleParams() {
    return doubleParams;
  }

  /**
   * Returns the type of the camera.
   *
   * @return the camera type
   */
  public String getCameraType() {
    return cameraType;
  }

  /**
   * Sets a string parameter for the camera if the key is not already present.
   *
   * @param key   the parameter key
   * @param value the parameter value
   */
  public void setStringParam(String key, String value) {
    if (!stringParams.containsKey(key)) {
      stringParams.put(key, value);
    }
  }

  /**
   * Sets a double parameter for the camera if the key is not already present.
   *
   * @param key   the parameter key
   * @param value the parameter value
   */
  public void setDoubleParam(String key, Double value) {
    if (!doubleParams.containsKey(key)) {
      doubleParams.put(key, value);
    }
  }

  /**
   * Retrieves the value of a specific string parameter.
   *
   * @param key the parameter key
   * @return the corresponding string value, or {@code null} if not found
   */
  public String getStringParam(String key) {
    return stringParams.get(key);
  }

  /**
   * Retrieves the value of a specific double parameter.
   *
   * @param key the parameter key
   * @return the corresponding double value, or {@code null} if not found
   */
  public double getDoubleParam(String key) {
    return doubleParams.get(key);
  }
}
