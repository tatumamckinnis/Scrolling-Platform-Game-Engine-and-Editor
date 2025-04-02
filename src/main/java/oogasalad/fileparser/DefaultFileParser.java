package oogasalad.fileparser;
import java.io.File;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.LevelData;

public class DefaultFileParser {

  private BlueprintDataParser myGmeObjectParser;
  private EventDataParser myEventDataParser;
  private SpriteDataParser mySpriteDataParser;
  //Map is <The name of the game, <Name of Level Directory (used for level progression when you have a subdirectory
  // from the game EX: <SuperMario, <BaseGame, List of levels in the base game version>>

  private Map<String, Map<String, List<File>>> mapOfGameLevels;

  public LevelData getLevelData(String filePath,String fileName) {
    if (!mapOfGameLevels.containsKey(filePath)) {
      //levelsAPI.getMapOfLevels()
    }
    //File dataFile = mapOfGameLevels;
 return null;
  }


}
