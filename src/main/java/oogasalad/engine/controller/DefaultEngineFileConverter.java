package oogasalad.engine.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import oogasalad.engine.controller.api.EngineFileConverterAPI;
import oogasalad.engine.event.Event;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.Player;
import oogasalad.engine.model.object.Sprite;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.BlueprintData;
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
  private static final Logger LOG = Logger.getLogger(DefaultEngineFileConverter.class.getName());
  private static final List<String> SUPPORTED_OBJECT_TYPES = Arrays.asList(
      ENGINE_FILE_RESOURCES.getString("ObjectTypes").split(","));

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
    return initGameObjectsMap(convertObjectMapToList(levelData), bluePrintMap);
  }

  private static List<GameObjectData> convertObjectMapToList(LevelData levelData) {
    List<GameObjectData> gameObjectDataList = new ArrayList<>();
    for (List<GameObjectData> gameObjects : levelData.gameObjectsByLayer().values()) {
      gameObjectDataList.addAll(gameObjects);
    }
    return gameObjectDataList;
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
    Map<String, Double> parametersMap = makeParametersMap(blueprintData);

    GameObject newGameObject;
    if (blueprintData.type().equals("Player")) {
      newGameObject = new Player(
          gameObjectData.uniqueId(),
          blueprintData.type(),
          gameObjectData.layer(),
          0,
          0,
          new HitBox(gameObjectData.x(), gameObjectData.y(), blueprintData.hitBoxData().hitBoxWidth(),
              blueprintData.hitBoxData().hitBowHeight()),
          new Sprite(frameMap, blueprintData.spriteData().baseImage(), animationMap, blueprintData.hitBoxData().spriteDx(), blueprintData.hitBoxData().spriteDy()),
          new ArrayList<>(),
          //TODO make map based on player data
          new HashMap<>(),
          blueprintData.objectProperties(),
          parametersMap
      );
    }
    else {
      newGameObject = new Entity(
          gameObjectData.uniqueId(),
          blueprintData.type(),
          gameObjectData.layer(),
          0,
          0,
          new HitBox(gameObjectData.x(), gameObjectData.y(), blueprintData.hitBoxData().hitBoxWidth(),
              blueprintData.hitBoxData().hitBowHeight()),
          new Sprite(frameMap, blueprintData.spriteData().baseImage(), animationMap, blueprintData.hitBoxData().spriteDx(), blueprintData.hitBoxData().spriteDy()),
          new ArrayList<>(),
          blueprintData.objectProperties(),
          parametersMap
      );
    }

    List<Event> events = EventConverter.convertEventData(gameObjectData, newGameObject, bluePrintMap);
    newGameObject.setEvents(events);
    return newGameObject;
  }

  private static Map<String, Double> makeParametersMap(BlueprintData blueprintData) {
    Map<String, Double> parameters = new HashMap<>();
    for (String key: blueprintData.objectProperties().keySet()) {
      parameters.put(key, Double.parseDouble(blueprintData.objectProperties().get(key)));
    }
    return parameters;
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