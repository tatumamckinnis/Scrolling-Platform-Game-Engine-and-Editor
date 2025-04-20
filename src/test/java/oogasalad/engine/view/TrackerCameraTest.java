package oogasalad.engine.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.scene.Group;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.view.camera.TrackerCamera;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TrackerCameraTest {

  private static final ResourceBundle LEVEL_RESOURCES = ResourceBundle.getBundle(
      LevelDisplay.class.getPackage().getName() + ".Level");
  private static final double EXPECTED_CAMERA_OFFSET_X = Double.parseDouble(
      LEVEL_RESOURCES.getString("LevelWidth")) / 2.0;
  private static final double EXPECTED_CAMERA_OFFSET_Y = Double.parseDouble(
      LEVEL_RESOURCES.getString("LevelHeight")) / 2.0;


  @Test
  void updateCamera_Basic_SceneIsTranslated() throws FileNotFoundException {
    TrackerCamera timeCamera = new TrackerCamera();
    Group gameWorld = new Group();
    ImmutableGameObject objectToFollow = createTempObject();
    timeCamera.setViewObjectToTrack(objectToFollow);
    timeCamera.updateCamera(gameWorld);

    assertEquals(EXPECTED_CAMERA_OFFSET_X - objectToFollow.getXPosition(),
        gameWorld.getTranslateX());
    assertEquals(EXPECTED_CAMERA_OFFSET_Y - objectToFollow.getYPosition(),
        gameWorld.getTranslateY());

  }

  @Test
  void updateCamera_NullObject_ThrowsException() throws NoSuchElementException {
    TrackerCamera timeCamera = new TrackerCamera();
    Group gameWorld = new Group();
    assertThrows(NoSuchElementException.class, () -> timeCamera.updateCamera(gameWorld));
  }

  @Test
  void updateCamera_NullGroup_ThrowsException() throws NullPointerException {
    TrackerCamera timeCamera = new TrackerCamera();
    assertThrows(NullPointerException.class, () -> timeCamera.updateCamera(null));
  }

  private static ImmutableGameObject createTempObject() {
    // Create FrameData and supporting sprite info
    FrameData currentFrame = new FrameData(
        "DinoRun1",
        708,
        0,
        87,
        94
    );

    Map<String, FrameData> frameMap = Map.of("DinoRun1", currentFrame);
    Map<String, AnimationData> animationMap = new HashMap<>();

    // Construct Sprite object with offset (dx/dy) = 0
    Sprite sprite = new Sprite(frameMap, currentFrame, animationMap, 0, 0,
        new File("DinoRun1.png"), 90.0, false);

    // Construct HitBox for the object
    HitBox hitBox = new HitBox(100, 100, 87, 94); // x, y, width, height

    // Create the Entity with required params
    // events
    // stringParams
    // doubleParams

    return new Entity(
        new UUID(4, 1),
        "Player",
        1,
        1.0,
        1.0,
        hitBox,
        sprite,
        new ArrayList<>(), // events
        new HashMap<>(),   // stringParams
        new HashMap<>()    // doubleParams
    );
  }

}