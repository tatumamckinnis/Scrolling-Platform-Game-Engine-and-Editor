package oogasalad.engine.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.stage.Stage;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.Player;
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
  EngineFileAPI myEngineFile;

  @Override
  public void start(Stage primaryStage) throws Exception {
    myEngineFile = new DefaultEngineFile();
    List<GameObjectData> gameObjectBluePrintData = new ArrayList<>();
    SpriteData spriteData1 = new SpriteData("Mario", 1, 1, 2, 4, new FrameData("Mario Paused", 1, 1, 2, 4), new ArrayList<>(), new ArrayList<>());
    GameObjectData gameObject1 = new GameObjectData(1, 1, "Mario", "Player", "Player", spriteData1, 1, 1, 1, new ArrayList<>());
    gameObjectBluePrintData.add(gameObject1);
    Map<Integer, List<GameObjectData>> gameObjectsByLayer = new HashMap<>();
    levelData = new LevelData("Super Mario Bros", gameObjectBluePrintData, gameObjectsByLayer);


    List<GameObject> expectedObjects = new ArrayList<>();
    expectedObjects.add(new Player(String.valueOf(1), "Player", "Player", spriteData1, new DynamicVariableCollection()));
    List<GameObject> actualObjects = myEngineFile.loadFileToEngine(levelData);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
