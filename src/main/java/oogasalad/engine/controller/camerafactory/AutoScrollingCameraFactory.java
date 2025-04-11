package oogasalad.engine.controller.camerafactory;

import java.util.Map;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.view.camera.AutoScrollingCamera;
import oogasalad.engine.view.camera.Camera;
import oogasalad.fileparser.records.CameraData;

/**
 * {@code AutoScrollingCameraFactory} is a concrete implementation of the {@link CameraFactory}
 * interface that creates instances of {@link AutoScrollingCamera}.
 *
 * <p>This factory is used when the camera should automatically scroll at a constant speed
 * (e.g., side-scrolling platformers or endless runners). The resulting camera does not rely on any
 * dynamic game object or user input for its behavior.</p>
 *
 * <p>This factory ignores any parameters in the {@link CameraData} or {@code gameObjectMap},
 * since {@link AutoScrollingCamera} is not dependent on external configuration.</p>
 *
 * @see AutoScrollingCamera
 * @see CameraFactory
 * @see CameraData
 * @see GameObject
 */
public class AutoScrollingCameraFactory implements CameraFactory {

  /**
   * Creates a new {@link AutoScrollingCamera} instance.
   *
   * <p>Any input parameters are ignored since this camera requires no configuration.</p>
   *
   * @param data          the camera configuration (ignored)
   * @param gameObjectMap the map of game objects (ignored)
   * @return a new instance of {@link AutoScrollingCamera}
   */
  @Override
  public Camera create(CameraData data, Map<String, GameObject> gameObjectMap) {
    return new AutoScrollingCamera();
  }
}
