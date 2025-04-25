package oogasalad.editor.model.data.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CameraData {

  private final static String DEFAULT_CAMERA_TYPE = "TRACKER";

  private String cameraType;
  private double cameraX;
  private double cameraY;
  private double cameraWidth;
  private double cameraHeight;
  private Map<String, String> stringParams;
  private Map<String, Double> doubleParams;

  public CameraData() {
    cameraType = DEFAULT_CAMERA_TYPE;
    stringParams = new HashMap<>();
    doubleParams = new HashMap<>();
  }

  public void setCameraX(int x) {
    cameraX = x;
  }

  public void setCameraY(int y) {
    cameraY = y;
  }

  public void setCameraWidth(int width) {
    cameraWidth = width;
  }

  public void setCameraHeight(int height) {
    cameraHeight = height;
  }

  public void setCameraType(String type) {
    cameraType = type;
  }

  public double getCameraX() {
    return cameraX;
  }

  public double getCameraY() {
    return cameraY;
  }

  public double getCameraWidth() {
    return cameraWidth;
  }

  public double getCameraHeight() {
    return cameraHeight;
  }

  public Map<String, String> getStringParams() {
    return stringParams;
  }

  public Map<String, Double> getDoubleParams() {
    return doubleParams;
  }

  public String getCameraType() {
    return cameraType;
  }

  public void setStringParam(String key, String value) {
    if (!stringParams.containsKey(key)) {
      stringParams.put(key, value);
    }
  }

  public void setDoubleParam(String key, Double value) {
    if (!doubleParams.containsKey(key)) {
      doubleParams.put(key, value);
    }
  }

  public String getStringParam(String key) {
    return stringParams.get(key);
  }

  public double getDoubleParam(String key) {
    return doubleParams.get(key);
  }
}
