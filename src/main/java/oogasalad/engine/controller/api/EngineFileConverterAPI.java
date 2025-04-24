package oogasalad.engine.controller.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.view.camera.Camera;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * Facilitates the translation and sending of data back and forth between the GameFileParser and the
 * engine’s data storage system
 *
 * @author Alana Zinkin
 */
public interface EngineFileConverterAPI {

  /**
   * Saves the current game or level status by: 1) Gathering current state from the Engine (objects,
   * progress, scores) 2) Converting them into a parser-compatible data structure 3) Delegating the
   * final write operation to GameFileParserAPI
   *
   * @throws IOException         if underlying file operations fail
   * @throws DataFormatException if the data cannot be translated into the parser's model
   */
  void saveLevelStatus() throws IOException, DataFormatException;

  /**
   * Loads a new level or resumes saved progress by: 1) Calling GameFileParserAPI to parse the file
   * into a standardized data structure 2) Translating that structure into the Engine’s runtime
   * objects 3) Updating the current Engine state
   */
  Map<String, GameObject> loadFileToEngine(LevelData level);

  /**
   * Returns game object using GameObjectData and blueprint map
   */
  GameObject makeGameObject(GameObjectData gameObjectData,
      Map<Integer, BlueprintData> bluePrintMap);

  /**
   * Takes in LevelData record and converts the Camera data into a view camera based on the type of
   * camera specified in the file
   *
   * @return a new Camera object
   */
  Camera loadCamera(LevelData level)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;

}
