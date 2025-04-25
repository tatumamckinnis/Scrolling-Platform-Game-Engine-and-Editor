package oogasalad.file;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Level Map returns a list of levels for a given game.
 *
 * @author Billy McCune
 */
public class levelMap {

  private static final Logger LOG = LogManager.getLogger();

  /**
   * reads the levels directory and builds the nested map structure.
   *
   * @param baseDirPath the file path of the level to retrieve
   * @return a map of games to level file paths
   */
  public static Map<String, Map<String, List<String>>> readLevels(String baseDirPath) {
    File levelsDir = new File(baseDirPath);
    if (!levelsDir.isDirectory()) {
      LOG.info("Provided path is not a valid directory: " + baseDirPath);
      return new HashMap<>();
    }
    return getGameMap(levelsDir);
  }

  //processes the game directories inside the levels directory.
  private static Map<String, Map<String, List<String>>> getGameMap(File levelsDir) {
    Map<String, Map<String, List<String>>> gameMap = new HashMap<>();
    File[] gameDirs = levelsDir.listFiles(File::isDirectory);
    if (gameDirs != null) {
      for (File gameDir : gameDirs) {
        //for each game directory, get its category map.
        gameMap.put(gameDir.getName(), getCategoryMap(gameDir));
      }
    }
    return gameMap;
  }

  // Processes the category directories inside a game directory.
  private static Map<String, List<String>> getCategoryMap(File gameDir) {
    Map<String, List<String>> categoryMap = new HashMap<>();
    File[] categoryDirs = gameDir.listFiles(File::isDirectory);
    if (categoryDirs != null) {
      for (File categoryDir : categoryDirs) {
        // For each category, retrieve the list of level files.
        categoryMap.put(categoryDir.getName(), getLevelFiles(categoryDir));
      }
    }
    return categoryMap;
  }

  // Retrieves all level file names within a category directory.
  private static List<String> getLevelFiles(File categoryDir) {
    List<String> levelFiles = new ArrayList<>();
    File[] files = categoryDir.listFiles(File::isFile);
    if (files != null) {
      for (File file : files) {
        levelFiles.add(file.getName());
      }
    }
    return levelFiles;
  }
}

