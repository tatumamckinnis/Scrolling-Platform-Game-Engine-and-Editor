package oogasalad.editor.controller.level;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import oogasalad.editor.model.data.CameraData;
import oogasalad.editor.model.data.CameraSpecLoader;
import oogasalad.editor.model.data.CameraSpecLoader.Specifications;
import oogasalad.editor.model.data.EditorLevelData;

/**
 * Thin wrapper that exposes safe, typed access to {@link CameraData} in a well defined file.
 * Manages camera-related data within an editor level. Provides utility methods to access and modify
 * the camera's geometry, type, and parameter configurations.
 *
 * @author Jacob You
 */
public class CameraDataManager {

  private final EditorLevelData level;
  private final CameraData camera;
  private final CameraSpecLoader cameraSpecLoader;

  /**
   * Constructs a new {@code CameraDataManager} for the given editor level.
   *
   * @param level the level containing the camera data to manage
   */
  public CameraDataManager(EditorLevelData level) {
    this.level = level;
    this.camera = level.getCameraData();
    this.cameraSpecLoader = new CameraSpecLoader();
  }

  /* ===== basic geometry ===== */

  /**
   * Returns the X position of the camera.
   *
   * @return the camera's X coordinate
   */
  public int getX() {
    return (int) camera.getCameraX();
  }

  /**
   * Returns the Y position of the camera.
   *
   * @return the camera's Y coordinate
   */
  public int getY() {
    return (int) camera.getCameraY();
  }

  /**
   * Returns the width of the camera view.
   *
   * @return the camera width
   */
  public int getWidth() {
    return (int) camera.getCameraWidth();
  }

  /**
   * Returns the height of the camera view.
   *
   * @return the camera height
   */
  public int getHeight() {
    return (int) camera.getCameraHeight();
  }

  /**
   * Sets the X position of the camera.
   *
   * @param x the new X coordinate
   */
  public void setX(int x) {
    camera.setCameraX(x);
  }

  /**
   * Sets the Y position of the camera.
   *
   * @param y the new Y coordinate
   */
  public void setY(int y) {
    camera.setCameraY(y);
  }

  /**
   * Sets the width of the camera view.
   *
   * @param w the new camera width
   */
  public void setWidth(int w) {
    camera.setCameraWidth(w);
  }

  /**
   * Sets the height of the camera view.
   *
   * @param h the new camera height
   */
  public void setHeight(int h) {
    camera.setCameraHeight(h);
  }

  /**
   * Returns the type of the camera (e.g., "Tracker", "AutoScroller").
   *
   * @return the camera type as a string
   */
  public String getType() {
    return camera.getCameraType();
  }

  /**
   * Sets the camera type.
   *
   * @param type the type to set; must not be {@code null} or blank
   */
  public void setType(String type) {
    if (type != null && !type.isBlank()) {
      camera.setCameraType(type);
    }
  }

  /* ===== parameter helpers ===== */

  /**
   * Returns an unmodifiable view of the camera's string parameters.
   *
   * @return a map of string parameter names to their values
   */
  public Map<String, String> getStringParams() {
    return Collections.unmodifiableMap(camera.getStringParams());
  }

  /**
   * Returns an unmodifiable view of the camera's double parameters.
   *
   * @return a map of double parameter names to their values
   */
  public Map<String, Double> getDoubleParams() {
    return Collections.unmodifiableMap(camera.getDoubleParams());
  }

  /**
   * Retrieves the value of a specific string parameter.
   *
   * @param key the name of the parameter
   * @return the value associated with the key, or {@code null} if not found
   */
  public String getString(String key) {
    return camera.getStringParam(key);
  }

  /**
   * Retrieves the value of a specific double parameter.
   *
   * @param key the name of the parameter
   * @return the value associated with the key, or {@code null} if not found
   */
  public Double getDouble(String key) {
    return camera.getDoubleParam(key);
  }

  /**
   * Adds or updates a string parameter for the camera.
   *
   * @param key the parameter name
   * @param val the value to associate with the key
   */
  public void putString(String key, String val) {
    if (key != null) {
      camera.getStringParams().put(key, val);
    }
  }

  /**
   * Adds or updates a double parameter for the camera.
   *
   * @param key the parameter name
   * @param val the value to associate with the key
   */
  public void putDouble(String key, Double val) {
    if (key != null && val != null) {
      camera.getDoubleParams().put(key, val);
    }
  }

  /**
   * Removes a parameter by its key from both the string and double parameter maps.
   *
   * @param key the name of the parameter to remove
   */
  public void removeParam(String key) {
    camera.getStringParams().remove(key);
    camera.getDoubleParams().remove(key);
  }

  /**
   * Returns the set of available camera types loaded from specifications.
   *
   * @return a set of camera type names
   */
  public Set<String> getCameraTypes() {
    return cameraSpecLoader.getCameraTypes();
  }

  /**
   * Retrieves the specifications for a given camera type.
   *
   * @param cameraType the name of the camera type
   * @return the {@link Specifications} for the specified type
   */
  public Specifications getCameraSpecifications(String cameraType) {
    return cameraSpecLoader.getSpecifications(cameraType);
  }

  /**
   * Replaces all current string parameters with the provided map.
   *
   * @param map a map of new string parameters; if {@code null}, clears the parameters
   */
  public void replaceStringParams(Map<String, String> map) {
    camera.getStringParams().clear();
    if (map != null) {
      camera.getStringParams().putAll(map);
    }
  }

  /**
   * Replaces all current double parameters with the provided map.
   *
   * @param map a map of new double parameters; if {@code null}, clears the parameters
   */
  public void replaceDoubleParams(Map<String, Double> map) {
    camera.getDoubleParams().clear();
    if (map != null) {
      camera.getDoubleParams().putAll(map);
    }
  }
}

