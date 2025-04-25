package oogasalad.engine.controller.camerafactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.camera.AutoScrollingCamera;
import oogasalad.engine.view.camera.Camera;
import oogasalad.engine.view.camera.TrackerCamera;
import oogasalad.fileparser.records.CameraData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default Camera Factory implementation - implements the Camera Factory API Used to create new
 * camera types (ex: autoscrolling, tracker)
 *
 * @author Alana Zinkin
 */
public class DefaultCameraFactory implements CameraFactory {

  private static Logger LOG = LogManager.getLogger();
  private static ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  @Override
  public Camera create(String type, CameraData data, Map<String, GameObject> gameObjectMap)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    String method =
        resourceManager.getConfig("engine.controller.controller", "Make") + type
            + resourceManager.getConfig("engine.controller.controller", "Camera");
    LOG.info(STR."Using \{method} for \{type}");
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
        throw new NullPointerException(
            resourceManager.getText("exceptions", "CameraObjectNonexistent"));
      }
    } else {
      throw new NoSuchElementException(
          resourceManager.getText("exceptions", "CameraObjectNotSpecified"));
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
        Double.parseDouble(resourceManager.getConfig("engine.controller.level", "ScrollSpeedX"))));
    camera.setScrollSpeedY(properties.getOrDefault("scrollSpeedY",
        Double.parseDouble(resourceManager.getConfig("engine.controller.level", "ScrollSpeedY"))));
    return camera;
  }

}
