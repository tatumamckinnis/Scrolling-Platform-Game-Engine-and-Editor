package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.Player;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteData;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultEngineFileTest {
  LevelData levelData;
  EngineFileAPI myEngineFile;

  @BeforeEach
  void setUp() {
    myEngineFile = new DefaultEngineFile();
    List<GameObjectData> gameObjectBluePrintData = new ArrayList<>();
    SpriteData spriteData1 = new SpriteData("Mario", 1, 1, 2, 4, new FrameData("Mario Paused", 1, 1, 2, 4), new ArrayList<>(), new ArrayList<>());
    GameObjectData gameObject1 = new GameObjectData(1, "Super Mario Bros", "Player", "Player", spriteData1, 1, 1, 1, new HashMap<String, Map<String, String>>());
    gameObjectBluePrintData.add(gameObject1);
    levelData = new LevelData("Super Mario Bros", gameObjectBluePrintData, new HashMap<>(), new ArrayList<>(), new ArrayList<>());
  }

  @Test
  void saveLevelStatus() {
  }

  @Test
  void loadFileToEngine()
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    List<GameObject> expectedObjects = new ArrayList<>();
    SpriteData expectedSpriteData1 = new SpriteData("Mario", 1, 1, 2, 4, new FrameData("Mario Paused", 1, 1, 2, 4), new ArrayList<>(), new ArrayList<>());
    expectedObjects.add(new Player(String.valueOf(1), "Player", "Player", expectedSpriteData1, new DynamicVariableCollection()));
    List<GameObject> actualObjects = myEngineFile.loadFileToEngine(levelData);
    assertEquals(expectedObjects, actualObjects);
  }
}