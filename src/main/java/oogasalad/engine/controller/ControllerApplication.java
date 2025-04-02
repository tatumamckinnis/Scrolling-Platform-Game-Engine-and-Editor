package oogasalad.engine.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.application.Application;
import javafx.stage.Stage;
import oogasalad.engine.model.object.DefaultGameObject;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteData;

/**
 * Test application for controller methods
 *
 * @author Alana Zinkin
 */
public class ControllerApplication extends Application {

  LevelData levelData;
  EngineFileConverterAPI myEngineFile;

  @Override
  public void start(Stage primaryStage) throws Exception {
    myEngineFile = new DefaultEngineFileConverter();
    Map<Integer, BlueprintData> bluePrintMap = new HashMap<>();
    List<GameObjectData> gameObjectBluePrintData = new ArrayList<>();
    SpriteData spriteData1 = new SpriteData("Mario", new FrameData("Mario Paused", 1, 1, 2, 4, new File("dd")), new ArrayList<>(), new ArrayList<>());
    GameObjectData gameObject1 = new GameObjectData(1, new UUID(4, 1), 1, 1, 0);
    gameObjectBluePrintData.add(gameObject1);
    Map<Integer, List<GameObjectData>> gameObjectsByLayer = new HashMap<>();
    levelData = new LevelData("Super Mario Bros", bluePrintMap, gameObjectsByLayer);


    List<GameObject> expectedObjects = new ArrayList<>();
    expectedObjects.add(new DefaultGameObject(new UUID(4, 1), 1, "Player", 1, 1, 5, 10, 0, "Mario", "Player", spriteData1, new HashMap<>(), new ArrayList<>()));
    Map<String, GameObject> actualObjects = myEngineFile.loadFileToEngine(levelData);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
