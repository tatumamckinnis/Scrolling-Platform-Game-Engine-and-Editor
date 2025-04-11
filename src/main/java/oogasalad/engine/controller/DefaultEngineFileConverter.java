package oogasalad.engine.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import oogasalad.Main;
import oogasalad.engine.controller.api.EngineFileConverterAPI;
import oogasalad.engine.event.Event;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.Player;
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.model.object.ViewObject;
import oogasalad.engine.view.camera.AutoScrollingCamera;
import oogasalad.engine.view.camera.Camera;
import oogasalad.engine.view.camera.TrackerCamera;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * Default implementation of the EngineFileAPI Used for converting the level data to game objects
 * and event actions
 *
 * @author Alana Zinkin
 */
public class DefaultEngineFileConverter implements EngineFileConverterAPI {

  private static final ResourceBundle ENGINE_FILE_RESOURCES = ResourceBundle.getBundle(
      DefaultEngineFileConverter.class.getPackageName() + "." + "Controller");
  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackageName() + "." + "Exceptions");
  private static final Logger LOG = Logger.getLogger(DefaultEngineFileConverter.class.getName());
  private static final List<String> SUPPORTED_OBJECT_TYPES = Arrays.asList(
      ENGINE_FILE_RESOURCES.getString("ObjectTypes").split(","));


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
    CameraData cameraData = level.cameraData();
    if (cameraData.type().equals("Tracker")) {
      return makeTrackerCamera(cameraData);
    }
    return new AutoScrollingCamera();
  }

  private TrackerCamera makeTrackerCamera(CameraData cameraData) {
    TrackerCamera camera = new TrackerCamera();
    GameObject objectToTrack;
    if (cameraData.stringProperties().containsKey("objectToTrack")) {
        try {
          objectToTrack = gameObjectMap.get(cameraData.stringProperties().get("objectToTrack"));
          ViewObject viewObjectToTrack = DefaultGameController.convertToViewObject(objectToTrack);
          camera.setViewObjectToTrack(viewObjectToTrack);
          return camera;
        }
        catch (NullPointerException e) {
          throw new NullPointerException(EXCEPTIONS.getString("CameraObjectNonexistent"));
        }
    }
    else{
      throw new NoSuchElementException(EXCEPTIONS.getString("CameraObjectNotSpecified"));
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

  private GameObject makeGameObject(GameObjectData gameObjectData,
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
        blueprintData.hitBoxData().spriteDx(), blueprintData.hitBoxData().spriteDy(), blueprintData.spriteData().spriteFile());
    List<Event> emptyEvents = new ArrayList<>();
    Map<String, String> stringParams = blueprintData.stringProperties();
    Map<String, Double> doubleParams = blueprintData.doubleProperties();
    Map<String, Double> displayedStats = makeDisplayedStatsMap(blueprintData, doubleParams);

    if (blueprintData.type().equals("Player")) {
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

  private static Map<String, Double> makeDisplayedStatsMap(BlueprintData blueprintData,
      Map<String, Double> doubleParams) {
    Map<String, Double> displayedStats = new HashMap<>();
    for (String stat : blueprintData.displayedProperties()) {
      displayedStats.put(stat, doubleParams.getOrDefault(stat, 0.0));
    }
    return displayedStats;
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