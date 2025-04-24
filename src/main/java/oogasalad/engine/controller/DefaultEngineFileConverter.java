package oogasalad.engine.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.DataFormatException;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.controller.api.EngineFileConverterAPI;
import oogasalad.engine.controller.camerafactory.CameraFactory;
import oogasalad.engine.controller.camerafactory.DefaultCameraFactory;
import oogasalad.engine.model.event.Event;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.Player;
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.view.camera.AutoScrollingCamera;
import oogasalad.engine.view.camera.Camera;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default implementation of the EngineFileAPI Used for converting the level data to game objects
 * and event actions
 *
 * @author Alana Zinkin
 */
public class DefaultEngineFileConverter implements EngineFileConverterAPI {

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();
  private static final Logger LOG = LogManager.getLogger();

  private Map<String, GameObject> gameObjectMap;

  /**
   * Saves the current game or level status by: 1) Gathering current state from the Engine (objects,
   * progress, scores) 2) Converting them into a parser-compatible data structure 3) Delegating the
   * final write operation to GameFileParserAPI
   *
   * @throws IOException         if underlying file operations fail
   * @throws DataFormatException if the data cannot be translated into the parser's model
   */
  @Override
  public void saveLevelStatus() throws IOException, DataFormatException {
    // TODO: will be implemented soon...
  }

  /**
   * Loads a new level or resumes saved progress by translating the standardized LevelData structure
   * created by the File Parser into the Engine’s runtime objects
   *
   * @return Map of the String of the UUID to the newly instantiated GameObject
   */
  @Override
  public Map<String, GameObject> loadFileToEngine(LevelData levelData) {
    Map<Integer, BlueprintData> bluePrintMap = levelData.gameBluePrintData();
    gameObjectMap = initGameObjectsMap(levelData.gameObjects(), bluePrintMap);
    return gameObjectMap;
  }

  @Override
  public Camera loadCamera(LevelData level) {
    try {
      CameraData cameraData = level.cameraData();
      String cameraType = cameraData.type();
      CameraFactory cameraFactory = new DefaultCameraFactory();
      LOG.info("Camera Type Created:" + cameraType);
      return cameraFactory.create(cameraType, cameraData, gameObjectMap);

    } catch (Exception e) {
      LOG.warn(resourceManager.getText("exceptions","FailToLoadCameraType") + ": " + e.getMessage());
      return new AutoScrollingCamera();
    }
  }


  private Map<String, GameObject> initGameObjectsMap(List<GameObjectData> gameObjects,
      Map<Integer, BlueprintData> bluePrintMap) {
    Map<String, GameObject> gameObjectMap = new HashMap<>();
    for (GameObjectData gameObjectData : gameObjects) {
      GameObject newObject = makeGameObject(gameObjectData, bluePrintMap);
      gameObjectMap.put(newObject.getUUID(), newObject);
    }
    return gameObjectMap;
  }

  @Override
  public GameObject makeGameObject(GameObjectData gameObjectData,
      Map<Integer, BlueprintData> bluePrintMap) {
    BlueprintData blueprintData = bluePrintMap.get(gameObjectData.blueprintId());
    Map<String, FrameData> frameMap = makeFrameMap(blueprintData);
    Map<String, AnimationData> animationMap = makeAnimationMap(blueprintData);

    GameObject newGameObject;
    UUID uniqueId = gameObjectData.uniqueId();
    String type = blueprintData.type();
    int layer = gameObjectData.layer();
    int xVelocity = 0;
    int yVelocity = 0;
    HitBox hitBox = new HitBox(gameObjectData.x(), gameObjectData.y(),
        blueprintData.hitBoxData().hitBoxWidth(),
        blueprintData.hitBoxData().hitBoxHeight());
    Sprite sprite = new Sprite(frameMap, blueprintData.spriteData().baseImage(), animationMap,
        blueprintData.hitBoxData().spriteDx(), blueprintData.hitBoxData().spriteDy(), blueprintData.spriteData().spriteFile(),
        blueprintData.rotation(), blueprintData.isFlipped());
    List<Event> emptyEvents = new ArrayList<>();
    Map<String, String> stringParams = blueprintData.stringProperties();
    Map<String, Double> doubleParams = blueprintData.doubleProperties();
    List<String> displayedStats = blueprintData.displayedProperties();

    if (blueprintData.type().equals("player")) {
      newGameObject = new Player(uniqueId, type, layer, xVelocity, yVelocity, hitBox, sprite,
          emptyEvents, displayedStats, stringParams, doubleParams);
    } else {
      newGameObject = new Entity(uniqueId, type, layer, xVelocity, yVelocity, hitBox, sprite,
          emptyEvents, stringParams, doubleParams);
    }

    List<Event> events = EventConverter.convertEventData(gameObjectData, newGameObject,
        bluePrintMap);
    newGameObject.setEvents(events);
    return newGameObject;
  }

  private static Map<String, FrameData> makeFrameMap(BlueprintData blueprintData) {
    Map<String, FrameData> frameMap = new HashMap<>();
    for (FrameData frameData : blueprintData.spriteData().frames()) {
      frameMap.put(frameData.name(), frameData);
    }
    return frameMap;
  }

  private static Map<String, AnimationData> makeAnimationMap(BlueprintData blueprintData) {
    Map<String, AnimationData> animationMap = new HashMap<>();
    for (AnimationData animationData : blueprintData.spriteData().animations()) {
      animationMap.put(animationData.name(), animationData);
    }
    return animationMap;
  }
}