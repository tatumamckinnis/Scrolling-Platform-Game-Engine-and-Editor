package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import oogasalad.engine.controller.exception.ObjectNotSupportedException;
import oogasalad.engine.controller.gameobjectfactory.GameObjectFactory;
import oogasalad.engine.event.Event;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.BlueprintData;
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
      DefaultEngineFileConverter.class.getPackageName() + "." + "EngineFile");
  private static final Logger LOG = Logger.getLogger(DefaultEngineFileConverter.class.getName());

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
  public Map<String, GameObject> loadFileToEngine(LevelData levelData)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Map<String, GameObject> gameObjectUUIDMap = new HashMap<>();
    List<GameObjectData> gameObjectDataList = new ArrayList<>();
    Map<Integer, BlueprintData> bluePrintMap = levelData.gameBluePrintData();
    for (List<GameObjectData> gameObjects : levelData.gameObjectsByLayer().values()) {
      gameObjectDataList.addAll(gameObjects);
    }
    initGameObjectsMap(gameObjectDataList, gameObjectUUIDMap, bluePrintMap);
    return gameObjectUUIDMap;
  }

  private void initGameObjectsMap(List<GameObjectData> gameObjects,
      Map<String, GameObject> gameObjectMap, Map<Integer, BlueprintData> bluePrintMap)
      throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    for (GameObjectData gameObjectData : gameObjects) {
      String className = ENGINE_FILE_RESOURCES.getString("GameObjectFactoryPackageName") + "."
          + bluePrintMap.get(gameObjectData.blueprintId()) + ENGINE_FILE_RESOURCES.getString("Factory");
      try {
        GameObject newObject = makeGameObject(gameObjectData, className, bluePrintMap);
        gameObjectMap.put(newObject.getUuid(), newObject);
      } catch (ClassNotFoundException e) {
        throw new ObjectNotSupportedException(
            ENGINE_FILE_RESOURCES.getString("ObjectNotSupported"));
      }
    }
  }

  private GameObject makeGameObject(GameObjectData gameObjectData, String className, Map<Integer, BlueprintData> bluePrintMap)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Class<?> factoryClass = Class.forName(className);
    GameObjectFactory gameObjectFactory = (GameObjectFactory) factoryClass.getDeclaredConstructor()
        .newInstance();

    GameObject gameObject = gameObjectFactory.createGameObject(gameObjectData.uniqueId(),
        gameObjectData.blueprintId(),
        gameObjectData.x(),
        gameObjectData.y(),
        bluePrintMap.get(gameObjectData.blueprintId()).hitBoxData().hitBoxWidth(),
        bluePrintMap.get(gameObjectData.blueprintId()).hitBoxData().hitBowHeight(),
        gameObjectData.layer(),
        bluePrintMap.get(gameObjectData.blueprintId()).type(),
        bluePrintMap.get(gameObjectData.blueprintId()).group(),
        bluePrintMap.get(gameObjectData.blueprintId()).spriteData(),
        new DynamicVariableCollection(),
        new ArrayList<>()
    );

    List<Event> events = EventConverter.convertEventData(gameObjectData, gameObject, bluePrintMap);
    gameObject.setEvents(events);
    return gameObject;
  }
}