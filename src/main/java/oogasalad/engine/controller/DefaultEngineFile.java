package oogasalad.engine.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import oogasalad.engine.controller.exception.ObjectNotSupportedException;
import oogasalad.engine.controller.gameobjectfactory.GameObjectFactory;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * Default implementation of the EngineFileAPI Used for converting the level data to game objects
 * and event actions
 *
 * @author Alana Zinkin
 */
public class DefaultEngineFile implements EngineFileAPI {

  private static final ResourceBundle ENGINE_FILE_RESOURCES = ResourceBundle.getBundle(
      DefaultEngineFile.class.getPackageName() + "." + "EngineFile");
  private static final Logger LOG = Logger.getLogger(DefaultEngineFile.class.getName());

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
   * @return list of newly instantiated GameObjects
   */
  @Override
  public List<GameObject> loadFileToEngine(LevelData levelData)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    List<GameObjectData> gameObjectBluePrints = levelData.gameObjectBluePrintData();
    List<GameObject> gameObjectList = new ArrayList<>();
    final String factoryPackage = ENGINE_FILE_RESOURCES.getString("FactoryPackage");
    initGameObjectsList(gameObjectBluePrints, factoryPackage, gameObjectList);
    return gameObjectList;
  }

  private void initGameObjectsList(List<GameObjectData> gameObjects, String factoryPackage,
      List<GameObject> gameObjectList)
      throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    for (GameObjectData gameObjectData : gameObjects) {
      String className =
          factoryPackage + "." + gameObjectData.type() + ENGINE_FILE_RESOURCES.getString("Factory");
      System.out.println(className);
      try {
        GameObject newObject = makeGameObjectFromFactory(gameObjectData, className);
        gameObjectList.add(newObject);
      } catch (ClassNotFoundException e) {
        throw new ObjectNotSupportedException(
            ENGINE_FILE_RESOURCES.getString("ObjectNotSupported"));
      }
    }
  }

  private GameObject makeGameObjectFromFactory(GameObjectData gameObjectData, String className)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Class<?> clazz = Class.forName(className);
    GameObjectFactory gameObjectFactory = (GameObjectFactory) clazz.getDeclaredConstructor()
        .newInstance();
    return gameObjectFactory.createGameObject(
        String.valueOf(gameObjectData.uniqueId()), gameObjectData.type(), gameObjectData.group(),
        gameObjectData.spriteData(), new DynamicVariableCollection());
  }
}
