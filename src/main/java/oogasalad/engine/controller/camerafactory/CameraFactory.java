package oogasalad.engine.controller.camerafactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.view.camera.Camera;
import oogasalad.fileparser.records.CameraData;

/**
 * CameraFactory is responsible for creating instances of {@link Camera} based on the given
 * {@link CameraData}.
 *
 * <p>This abstraction allows different camera types (e.g., TrackerCamera, AutoScrollingCamera)
 * to be instantiated without tightly coupling to specific implementations.</p>
 *
 * <p>Common use cases include:
 * <ul>
 *   <li>Dynamically creating a camera from configuration (e.g., a level file)</li>
 *   <li>Encapsulating camera setup logic (e.g., assigning a target to track)</li>
 *   <li>Supporting extensible camera behaviors through factory-based instantiation</li>
 * </ul>
 * </p>
 *
 * @author Alana Zinkin
 */
public interface CameraFactory {

  /**
   * Creates a new {@link Camera} instance based on the provided data and game object context.
   *
   * @param type the type of camera to create
   * @param data          the CameraData containing the type and any configuration for the camera
   * @param gameObjectMap a map of GameObject IDs to their corresponding GameObject instances (used,
   *                      for example, to find a tracked object)
   * @return a fully initialized Camera instance
   * @throws IllegalArgumentException if the camera type is not recognized or invalid
   * @throws NullPointerException     if required parameters are missing in the configuration
   */
  Camera create(String type, CameraData data, Map<String, GameObject> gameObjectMap)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

}
