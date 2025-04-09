package oogasalad.engine.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.scene.Group;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.ViewObject;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TimeCameraTest {
  private static final ResourceBundle LEVEL_RESOURCES = ResourceBundle.getBundle(
      LevelDisplay.class.getPackage().getName() + ".Level");
  private static final double EXPECTED_CAMERA_OFFSET_X = Double.parseDouble(
      LEVEL_RESOURCES.getString("LevelWidth")) / 2.0;
  private static final double EXPECTED_CAMERA_OFFSET_Y = Double.parseDouble(
      LEVEL_RESOURCES.getString("LevelHeight")) / 2.0;


  @Test
  void updateCamera_Basic_SceneIsTranslated() throws FileNotFoundException {
    TimeCamera timeCamera = new TimeCamera();
    Group gameWorld = new Group();
    ViewObject objectToFollow = createTempViewObject();
    timeCamera.updateCamera(gameWorld, objectToFollow);

    assertEquals(EXPECTED_CAMERA_OFFSET_X - objectToFollow.getX(), gameWorld.getTranslateX());
    assertEquals(EXPECTED_CAMERA_OFFSET_Y - objectToFollow.getY(), gameWorld.getTranslateY());

  }

  @Test
  void updateCamera_NullObject_ThrowsException() throws NoSuchElementException {
    TimeCamera timeCamera = new TimeCamera();
    Group gameWorld = new Group();
    assertThrows(NoSuchElementException.class, () -> timeCamera.updateCamera(gameWorld, null));
  }

  @Test
  void updateCamera_NullGroup_ThrowsException() throws NullPointerException {
    TimeCamera timeCamera = new TimeCamera();
    ViewObject objectToFollow = createTempViewObject();
    assertThrows(NullPointerException.class, () -> timeCamera.updateCamera(null, objectToFollow));
  }

  private static ViewObject createTempViewObject() {
    FrameData currentFrame = new FrameData("DinoRun1", 708, 0, 87, 94, new File("/Users/alanazinkin/Desktop/CS308/oogasalad_team03/data/gameData/gameObjects/dinosaurgame/dinosaurgame-sprites.xml"));
    SpriteData expectedSpriteData1 = new SpriteData("DinoRun1", currentFrame, List.of(currentFrame), new ArrayList<>());
    GameObject gameObjectToFollow = new Entity(new UUID(4, 1), 1, "Player", 1, 1, 5,  10, 0, "Dino", "Player", expectedSpriteData1, currentFrame, new HashMap<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>(), new HitBoxData("default", 2, 4, 2, 4));
    gameObjectToFollow.setX(100);
    gameObjectToFollow.setY(100);
    return new ViewObject(gameObjectToFollow);
  }
}