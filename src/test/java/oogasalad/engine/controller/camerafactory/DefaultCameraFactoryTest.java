package oogasalad.engine.controller.camerafactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.view.camera.AutoScrollingCamera;
import oogasalad.engine.view.camera.Camera;
import oogasalad.engine.view.camera.TrackerCamera;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.FrameData;
import static org.junit.jupiter.api.Assertions.*;

import oogasalad.engine.model.object.GameObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unit tests for {@link DefaultCameraFactory}.
 *
 * @author Alana Zinkin
 */
class DefaultCameraFactoryTest {

  private CameraFactory cameraFactory;
  private Map<String, GameObject> gameObjectMap;

  @BeforeEach
  void setUp() {
    cameraFactory = new DefaultCameraFactory();
    gameObjectMap = new HashMap<>();
  }

  @Test
  void create_makeTrackerCam_Instantiated() throws Exception {

    Map<String, String> stringParams = new HashMap<>();
    Map<String, Double> doubleParams = new HashMap<>();
    UUID objectId = UUID.randomUUID();
    GameObject mockObject = new Entity(objectId, "type", 0, 1, 1, new HitBox(1, 1, 1, 1),
        new Sprite(new HashMap<>(), new FrameData("", 1, 1, 1, 1), new HashMap<>(), 1, 1,
            new File(""), 1, true), new ArrayList<>(), new HashMap<>(), new HashMap<>());
    gameObjectMap.put(objectId.toString(), mockObject);
    stringParams.put("objectToTrack", objectId.toString());
    doubleParams.put("zoom", 1.0);
    doubleParams.put("cameraOffsetX", 10.0);
    doubleParams.put("cameraOffsetY", 20.0);

    CameraData data = new CameraData("Tracker", stringParams, doubleParams);

    Camera camera = cameraFactory.create("Tracker", data, gameObjectMap);

    assertTrue(camera instanceof TrackerCamera);
  }

  @Test
  void create_makeAutoScrollingCam_Instantiated() throws Exception {
    Map<String, String> stringParams = new HashMap<>();
    Map<String, Double> doubleParams = new HashMap<>();

    doubleParams.put("zoom", 2.0);
    doubleParams.put("cameraOffsetX", 300.0);
    doubleParams.put("cameraOffsetY", 200.0);
    doubleParams.put("scrollSpeedX", 1.5);
    doubleParams.put("scrollSpeedY", 2.0);

    CameraData data = new CameraData("AutoScrolling", stringParams, doubleParams);

    Camera camera = cameraFactory.create("AutoScrolling", data, gameObjectMap);

    assertTrue(camera instanceof AutoScrollingCamera);
  }

  @Test
  void create_noTrackedObject_ThrowsException() throws Exception {
    CameraData data = new CameraData("Tracker", new HashMap<>(), null);
    assertThrows(InvocationTargetException.class, () -> {
      cameraFactory.create("Tracker", data, gameObjectMap);
    });
  }

  @Test
  void create_objectTrackedDoesntExist_ThrowsException() throws Exception {
    Map<String, String> stringParams = new HashMap<>();
    stringParams.put("objectToTrack", UUID.randomUUID().toString());
    CameraData data = new CameraData("Tracker", stringParams, null);

    assertThrows(InvocationTargetException.class, () -> {
      cameraFactory.create("Tracker", data, gameObjectMap);
    });
  }

}
