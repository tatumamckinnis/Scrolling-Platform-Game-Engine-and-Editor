package oogasalad.editor.model.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds configuration and runtime parameters for a camera in the editor.
 * <p>
 * The camera has a type (defaulting to {@code TRACKER}), a rectangular viewport
 * defined by position and size, and optional string and double parameters
 * for extensible configuration.
 * </p>
 *
 * @author Jacob You
 */
public class CameraData {

  private static final String DEFAULT_CAMERA_TYPE = "TRACKER";

  private String cameraType;
  private double cameraX;
  private double cameraY;
  private double cameraWidth;
  private double cameraHeight;
  private Map<String, String> stringParams;
  private Map<String, Double> doubleParams;

  /**
   * Constructs a new CameraData instance with the default camera type
   * and empty parameter maps.
   */
  public CameraData() {
    this.cameraType = DEFAULT_CAMERA_TYPE;
    this.stringParams = new HashMap<>();
    this.doubleParams = new HashMap<>();
  }

  /**
   * Sets the horizontal (X) position of the camera viewport.
   *
   * @param x the new X coordinate
   */
  public void setCameraX(int x) {
    this.cameraX = x;
  }

  /**
   * Sets the vertical (Y) position of the camera viewport.
   *
   * @param y the new Y coordinate
   */
  public void setCameraY(int y) {
    this.cameraY = y;
  }

  /**
   * Sets the width of the camera viewport.
   *
   * @param width the new viewport width
   */
  public void setCameraWidth(int width) {
    this.cameraWidth = width;
  }

  /**
   * Sets the height of the camera viewport.
   *
   * @param height the new viewport height
   */
  public void setCameraHeight(int height) {
    this.cameraHeight = height;
  }

  /**
   * Sets the type of the camera.
   *
   * @param type the camera type identifier
   */
  public void setCameraType(String type) {
    this.cameraType = type;
  }

  /**
   * Returns the horizontal (X) position of the camera viewport.
   *
   * @return the camera's X coordinate
   */
  public double getCameraX() {
    return cameraX;
  }

  /**
   * Returns the vertical (Y) position of the camera viewport.
   *
   * @return the camera's Y coordinate
   */
  public double getCameraY() {
    return cameraY;
  }

  /**
   * Returns the width of the camera viewport.
   *
   * @return the viewport width
   */
  public double getCameraWidth() {
    return cameraWidth;
  }

  /**
   * Returns the height of the camera viewport.
   *
   * @return the viewport height
   */
  public double getCameraHeight() {
    return cameraHeight;
  }

  /**
   * Returns the current camera type identifier.
   *
   * @return the camera type
   */
  public String getCameraType() {
    return cameraType;
  }

  /**
   * Returns a modifiable map of string parameters associated with this camera.
   *
   * @return the map of string parameters
   */
  public Map<String, String> getStringParams() {
    return stringParams;
  }

  /**
   * Returns a modifiable map of double parameters associated with this camera.
   *
   * @return the map of double parameters
   */
  public Map<String, Double> getDoubleParams() {
    return doubleParams;
  }

  /**
   * Adds a string parameter to this camera configuration if the key is not already present.
   *
   * @param key   the parameter name
   * @param value the parameter value
   */
  public void setStringParam(String key, String value) {
    if (!stringParams.containsKey(key)) {
      stringParams.put(key, value);
    }
  }

  /**
   * Adds a double parameter to this camera configuration if the key is not already present.
   *
   * @param key   the parameter name
   * @param value the parameter value
   */
  public void setDoubleParam(String key, Double value) {
    if (!doubleParams.containsKey(key)) {
      doubleParams.put(key, value);
    }
  }

  /**
   * Retrieves the value of a string parameter by key.
   *
   * @param key the parameter name
   * @return the associated string value, or {@code null} if not present
   */
  public String getStringParam(String key) {
    return stringParams.get(key);
  }

  /**
   * Retrieves the value of a double parameter by key.
   *
   * @param key the parameter name
   * @return the associated double value, or {@code null} if not present
   */
  public double getDoubleParam(String key) {
    return doubleParams.getOrDefault(key, 0.0);
  }
}
