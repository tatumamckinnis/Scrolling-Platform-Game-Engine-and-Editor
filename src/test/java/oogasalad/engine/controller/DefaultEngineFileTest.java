package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
  EngineFileConverterAPI myEngineFile;

  @BeforeEach
  void setUp() {
    myEngineFile = new DefaultEngineFileConverter();
    List<GameObjectData> gameObjectBluePrintData = new ArrayList<>();
    SpriteData spriteData1 = new SpriteData("Mario", 1, 1, 2, 4, new FrameData("Mario Paused", 1, 1, 2, 4), new ArrayList<>(), new ArrayList<>());
    GameObjectData gameObject1 = new GameObjectData(1, new UUID(4, 1), "Mario", "Player", "Player", spriteData1, 1, 1, 1, new ArrayList<>());
    gameObjectBluePrintData.add(gameObject1);
    Map<Integer, List<GameObjectData>> gameObjectsByLayer = new HashMap<>();
    levelData = new LevelData("Super Mario Bros", gameObjectBluePrintData, gameObjectsByLayer);
  }

  @Test
  void saveLevelStatus() {

  }

  @Test
  void loadFileToEngine()
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    List<GameObject> expectedObjects = new ArrayList<>();
    SpriteData expectedSpriteData1 = new SpriteData("Mario", 1, 1, 2, 4, new FrameData("Mario Paused", 1, 1, 2, 4), new ArrayList<>(), new ArrayList<>());
    expectedObjects.add(new Player(new UUID(4, 1), 1, 1, 1, 5, 10, 0, "Mario", "Player", expectedSpriteData1, new DynamicVariableCollection(), new ArrayList<>()));
    Map<String, GameObject> actualObjects = myEngineFile.loadFileToEngine(levelData);
    List<GameObject> myActualObjects = new ArrayList<>(actualObjects.values());

    assertEquals(expectedObjects.getFirst().getBlueprintID(), myActualObjects.getFirst().getBlueprintID());
    assertEquals(expectedObjects.getFirst().getGroup(), myActualObjects.getFirst().getGroup());
    assertEquals(expectedObjects.getFirst().getSpriteData(), myActualObjects.getFirst().getSpriteData());
    assertEquals(expectedObjects.getFirst().getUuid(), myActualObjects.getFirst().getUuid());
  }
}