package oogasalad.engine.controller.camerafactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import oogasalad.Main;
import oogasalad.engine.controller.DefaultGameController;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.LevelDisplay;
import oogasalad.engine.view.camera.AutoScrollingCamera;
import oogasalad.engine.view.camera.Camera;
import oogasalad.engine.view.camera.TrackerCamera;
import oogasalad.fileparser.records.CameraData;

public class DefaultCameraFactory implements CameraFactory {

  private static final ResourceBundle LEVEL_RESOURCES = ResourceBundle.getBundle(
      LevelDisplay.class.getPackage().getName() + ".Level");
  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackageName() + "." + "Exceptions");
  private static final ResourceBundle FACTORY_RESOURCES = ResourceBundle.getBundle(
      DefaultGameController.class.getPackage().getName() + ".Controller");

  @Override
  public Camera create(String type, CameraData data, Map<String, GameObject> gameObjectMap)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    String method =
        FACTORY_RESOURCES.getString("Make") + type + FACTORY_RESOURCES.getString("Camera");
    Method creatorMethod = this.getClass().getDeclaredMethod(method, CameraData.class, Map.class);
    return (Camera) creatorMethod.invoke(this, data, gameObjectMap);
  }

  /**
   * called by reflection
   *
   * @param cameraData    the camera data to pass
   * @param gameObjectMap map of UUID strings to game objects
   * @return a new Tracker Camera
   */
  private TrackerCamera makeTrackerCamera(CameraData cameraData,
      Map<String, GameObject> gameObjectMap) {
    TrackerCamera camera = new TrackerCamera();
    Map<String, String> properties = cameraData.stringProperties();

    if (properties.containsKey("objectToTrack")) {
      try {
        ImmutableGameObject viewObjectToTrack = gameObjectMap.get(properties.get("objectToTrack"));
        camera.setViewObjectToTrack(viewObjectToTrack);
        return camera;
      } catch (NullPointerException e) {
        throw new NullPointerException(EXCEPTIONS.getString("CameraObjectNonexistent"));
      }
    } else {
      throw new NoSuchElementException(EXCEPTIONS.getString("CameraObjectNotSpecified"));
    }
  }

  /**
   * called by reflection
   *
   * @param cameraData    the camera data to pass
   * @param gameObjectMap map of UUID strings to game objects
   * @return a new Tracker Camera
   */
  private AutoScrollingCamera makeAutoScrollingCamera(CameraData cameraData,
      Map<String, GameObject> gameObjectMap) {
    AutoScrollingCamera camera = new AutoScrollingCamera();
    Map<String, Double> properties = cameraData.doubleProperties();
    camera.setScrollSpeedX(properties.getOrDefault("scrollSpeedX",
        Double.parseDouble(LEVEL_RESOURCES.getString("ScrollSpeedX"))));
    camera.setScrollSpeedY(properties.getOrDefault("scrollSpeedY",
        Double.parseDouble(LEVEL_RESOURCES.getString("ScrollSpeedY"))));
    return camera;
  }


}
