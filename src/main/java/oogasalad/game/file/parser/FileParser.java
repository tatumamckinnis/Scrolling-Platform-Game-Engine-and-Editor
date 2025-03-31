package oogasalad.game.file.parser;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import oogasalad.game.file.parser.records.LevelData;

public class FileParser {
//
//  private GameObjectBlueprintParser myGmeObjectParser;
//  private EventParser myEventParser;
//  private SpriteParser mySpriteParser;
//  //Map is <The name of the game, <Name of Level Directory (used for level progression when you have a subdirectory
//  // from the game EX: <SuperMario, <BaseGame, List of levels in the base game version>>
//
//  private HashMap<String, HashMap<String,List<File>>> mapOfGameLevels;
//
//  public LevelData getLevelData(String filePath,String fileName) {
//    if (!mapOfGameLevels.containsKey(filePath)) {
//      createListOfConfigFiles(filePath, fileName);
//    }
//    //File dataFile = mapOfGameLevels;
//
//  }



//  /**
//   * Creates a mapping from file names to configuration files found in the designated folder.
//   */
//  private void createListOfConfigFiles(String filePath, String fileName) throws IllegalArgumentException, IllegalStateException {
//    try {
//      File folder = new File(System.getProperty("user.dir") + filePath);
//      File[] fileList = folder.listFiles();
//      Arrays.stream(fileList)
//          .filter(File::isFile)
//          .forEach(file -> fileMap.put(file.getName(), file));
//    } catch (NullPointerException e) {
//      throw new IllegalStateException(
//          "error-configDirectoryNotFound," + System.getProperty("user.dir") + ","
//              + mapOfGameLevels);
//    }
  //}
}
