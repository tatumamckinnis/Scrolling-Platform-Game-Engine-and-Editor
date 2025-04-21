package oogasalad.engine.controller;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.controller.api.EngineFileConverterAPI;
import oogasalad.engine.model.event.Event;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.view.camera.Camera;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteData;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultEngineFileTest {

  LevelData levelData;
  EngineFileConverterAPI myEngineFile;
  CameraData expectedCamera;

  @BeforeEach
  void setUp() {
    myEngineFile = new DefaultEngineFileConverter();
    Map<Integer, BlueprintData> bluePrintMap = new HashMap<>();
    SpriteData spriteData1 = new SpriteData("Mario", new File("src/test/resources/sprites1/sprite1.png"),
        new FrameData("Mario Paused", 1, 1, 2, 4), new ArrayList<>(),
        new ArrayList<>());
    HitBoxData hitBoxData1 = new HitBoxData("Mario", 1, 1, 2, 4);
    bluePrintMap.put(1, new BlueprintData(1, 1, 1, 90, false,"Mario", "Player", "Player", spriteData1, hitBoxData1,
        new ArrayList<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>()));
    UUID expectedUUID = UUID.fromString("e816f04c-3047-4e30-9e20-2e601a99dde8");
    GameObjectData gameObject1 = new GameObjectData(1, expectedUUID, 1, 1, 0, "");
    List<GameObjectData> gameObjects = new ArrayList<>();
    gameObjects.add(gameObject1);
    expectedCamera = new CameraData("Tracker", Map.of("objectToTrack","e816f04c-3047-4e30-9e20-2e601a99dde8"), new HashMap<>());
    levelData = new LevelData("Mario", 0, 500, 500, 0, expectedCamera, bluePrintMap, gameObjects);
  }


  @Test
  void loadFileToEngine()
      throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    List<GameObject> expectedObjects = new ArrayList<>();

    // Setup sprite and hitbox
    FrameData marioFrame = new FrameData("Mario Paused", 1, 1, 2, 4);
    Map<String, FrameData> frameMap = new HashMap<>();
    frameMap.put("Mario Paused", marioFrame);
    Map<String, AnimationData> animationMap = new HashMap<>();

    Sprite expectedSprite = new Sprite(frameMap, marioFrame, animationMap, 0, 0, new File("dd"), 90.0, false);
    HitBox expectedHitBox = new HitBox(1, 1, 2, 4); // x, y, width, height

    // Setup entity
    UUID actualUUID = UUID.fromString("e816f04c-3047-4e30-9e20-2e601a99dde8");
    Map<String, String> stringParams = new HashMap<>();
    Map<String, Double> doubleParams = new HashMap<>();
    List<Event> events = new ArrayList<>();
    expectedObjects.add(
        new Entity(actualUUID, "Player", 1, 1.0, 1.0, expectedHitBox, expectedSprite, events,
            stringParams, doubleParams));

    // Call method under test
    Map<String, GameObject> actualObjects = myEngineFile.loadFileToEngine(levelData);
    Camera actualCamera = myEngineFile.loadCamera(levelData);
    List<GameObject> myActualObjects = new ArrayList<>(actualObjects.values());

    // Assertions
    assertEquals(expectedCamera.type() + "Camera", actualCamera.getClass().getSimpleName());
    assertEquals(expectedObjects.getFirst().getUUID(), myActualObjects.getFirst().getUUID());
    assertEquals(expectedObjects.getFirst().getXPosition(),
        myActualObjects.getFirst().getXPosition());
    assertEquals(expectedObjects.getFirst().getDoubleParams(),
        myActualObjects.getFirst().getDoubleParams());
    assertEquals(expectedObjects.getFirst().getType(), myActualObjects.getFirst().getType());
  }

}