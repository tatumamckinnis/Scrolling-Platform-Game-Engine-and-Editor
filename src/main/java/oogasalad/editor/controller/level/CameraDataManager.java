package oogasalad.editor.controller.level;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import oogasalad.editor.model.data.CameraSpecLoader;
import oogasalad.editor.model.data.CameraSpecLoader.Specifications;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.CameraData;

/**
 * Thin wrapper that exposes safe, typed access to {@link CameraData} in a well defined file.
 *
 * @author Jacob You
 */
public class CameraDataManager {

  private final EditorLevelData level;
  private final CameraData camera;
  private final CameraSpecLoader cameraSpecLoader;

  public CameraDataManager(EditorLevelData level) {
    this.level = level;
    this.camera = level.getCameraData();
    this.cameraSpecLoader = new CameraSpecLoader();
  }

  /* ===== basic geometry ===== */

  public int getX() {
    return (int) camera.getCameraX();
  }

  public int getY() {
    return (int) camera.getCameraY();
  }

  public int getWidth() {
    return (int) camera.getCameraWidth();
  }

  public int getHeight() {
    return (int) camera.getCameraHeight();
  }

  public void setX(int x) {
    camera.setCameraX(x);
  }

  public void setY(int y) {
    camera.setCameraY(y);
  }

  public void setWidth(int w) {
    camera.setCameraWidth(w);
  }

  public void setHeight(int h) {
    camera.setCameraHeight(h);
  }

  public String getType() {
    return camera.getCameraType();
  }

  public void setType(String type) {
    if (type != null && !type.isBlank()) {
      camera.setCameraType(type);
    }
  }

  /* ===== parameter helpers ===== */

  public Map<String, String> getStringParams() {
    return Collections.unmodifiableMap(camera.getStringParams());
  }

  public Map<String, Double> getDoubleParams() {
    return Collections.unmodifiableMap(camera.getDoubleParams());
  }

  public String getString(String key) {
    return camera.getStringParam(key);
  }

  public Double getDouble(String key) {
    return camera.getDoubleParam(key);
  }

  public void putString(String key, String val) {
    if (key != null) {
      camera.getStringParams().put(key, val);
    }
  }

  public void putDouble(String key, Double val) {
    if (key != null && val != null) {
      camera.getDoubleParams().put(key, val);
    }
  }

  public void removeParam(String key) {
    camera.getStringParams().remove(key);
    camera.getDoubleParams().remove(key);
  }

  public Set<String> getCameraTypes() {
    return cameraSpecLoader.getCameraTypes();
  }

  public Specifications getCameraSpecifications(String cameraType) {
    return cameraSpecLoader.getSpecifications(cameraType);
  }

  public void replaceStringParams(Map<String,String> map) {
    camera.getStringParams().clear();
    if (map!=null) camera.getStringParams().putAll(map);
  }

  public void replaceDoubleParams(Map<String,Double> map) {
    camera.getDoubleParams().clear();
    if (map!=null) camera.getDoubleParams().putAll(map);
  }
}
