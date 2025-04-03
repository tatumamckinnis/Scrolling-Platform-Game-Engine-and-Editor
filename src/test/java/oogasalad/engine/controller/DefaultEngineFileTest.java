package oogasalad.engine.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.DefaultGameObject;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.BlueprintData;
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

  @BeforeEach
  void setUp() {
    myEngineFile = new DefaultEngineFileConverter();
    Map<Integer, BlueprintData> bluePrintMap = new HashMap<>();
    SpriteData spriteData1 = new SpriteData("Mario", new FrameData("Mario Paused", 1, 1, 2, 4, new File("dd")), new ArrayList<>(), new ArrayList<>());
    HitBoxData hitBoxData1 = new HitBoxData("Mario", 1, 1, 2, 4);
    bluePrintMap.put(1, new BlueprintData(1, "Mario", "Player", "Player", spriteData1, hitBoxData1, new ArrayList<>(), new HashMap<>()));
    List<GameObjectData> gameObjectBluePrintData = new ArrayList<>();
    GameObjectData gameObject1 = new GameObjectData(1, new UUID(4, 1), 1, 1, 0);
    gameObjectBluePrintData.add(gameObject1);
    Map<Integer, List<GameObjectData>> gameObjectsByLayer = new HashMap<>();
    gameObjectsByLayer.put(1, gameObjectBluePrintData);
    //levelData = new LevelData("Super Mario Bros", bluePrintMap, gameObjectsByLayer);
  }

  @Test
  void saveLevelStatus() {

  }

  @Test
  void loadFileToEngine() {

    List<GameObject> expectedObjects = new ArrayList<>();
    SpriteData expectedSpriteData1 = new SpriteData("Mario", new FrameData("Mario Paused", 1, 1, 2, 4, new File("dd")), new ArrayList<>(), new ArrayList<>());
    expectedObjects.add(new DefaultGameObject(new UUID(4, 1), 1, "Player", 1, 1, 5,  10, 0, "Mario", "Player", expectedSpriteData1, new FrameData("Mario Paused", 1, 1, 2, 4, new File("dd")), new HashMap<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>(), new HitBoxData("default", 1, 1, 2, 4)));

    Map<String, GameObject> actualObjects = myEngineFile.loadFileToEngine(levelData);
    List<GameObject> myActualObjects = new ArrayList<>(actualObjects.values());

    assertEquals(expectedObjects.getFirst().getBlueprintID(), myActualObjects.getFirst().getBlueprintID());
    assertEquals(expectedObjects.getFirst().getGroup(), myActualObjects.getFirst().getGroup());
    assertEquals(expectedObjects.getFirst().getSpriteData(), myActualObjects.getFirst().getSpriteData());
    assertEquals(expectedObjects.getFirst().getUuid(), myActualObjects.getFirst().getUuid());
    assertEquals(expectedObjects.getFirst().getType(), myActualObjects.getFirst().getType());
  }
}