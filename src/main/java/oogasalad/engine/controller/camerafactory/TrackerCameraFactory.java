package oogasalad.engine.controller.camerafactory;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import oogasalad.Main;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.camera.Camera;
import oogasalad.engine.view.camera.TrackerCamera;
import oogasalad.fileparser.records.CameraData;

/**
 * {@code TrackerCameraFactory} is a {@link CameraFactory} implementation that produces
 * {@link TrackerCamera} instances.
 *
 * <p>This factory expects the {@link CameraData} to include a string property with key
 * {@code "objectToTrack"} that refers to a {@link GameObject} in the game. The corresponding
 * {@link ImmutableGameObject} is set on the created {@link TrackerCamera} so that the camera can
 * follow the target during gameplay.</p>
 *
 * <p>If the required tracking object is not provided or cannot be found, the factory will
 * throw a runtime exception to signal invalid configuration.</p>
 *
 * @see TrackerCamera
 * @see CameraData
 * @see GameObject
 * @see ImmutableGameObject
 */
public class TrackerCameraFactory implements CameraFactory {

  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackageName() + "." + "Exceptions");

  /**
   * Creates a {@link TrackerCamera} configured to follow a specific {@link GameObject}.
   *
   * <p>It looks for the key {@code "objectToTrack"} in the {@link CameraData}'s string properties.
   * The corresponding {@link GameObject} must be present in the provided {@code gameObjectMap}. If
   * found, the camera will track the converted {@link ImmutableGameObject} of that game
   * object.</p>
   *
   * @param cameraData    the data used to configure the camera
   * @param gameObjectMap the available game objects in the level, indexed by ID
   * @return a fully initialized {@link TrackerCamera}
   * @throws NullPointerException   if the target object is not found in the map
   * @throws NoSuchElementException if the camera data doesn't include a target to track
   */
  @Override
  public Camera create(CameraData cameraData, Map<String, GameObject> gameObjectMap) {
    return makeTrackerCamera(cameraData, gameObjectMap);
  }

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
}
