package oogasalad.fileparser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.GameObjectData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import oogasalad.fileparser.records.LevelData;

public class DefaultFileParser implements FileParserAPI {

  private LayerDataParser layerDataParser;
  private BlueprintDataParser myGameObjectParser;
  private EventDataParser myEventDataParser;
  // Map structure: <Game Name, <Level Directory, List of Level Files>>
  private Map<String, Map<String, List<File>>> mapOfGameLevels;

  public DefaultFileParser(){
    // Initialize parsers. These could be injected as dependencies if preferred.
    layerDataParser = new LayerDataParser();
    myGameObjectParser = new BlueprintDataParser();
    myEventDataParser = new EventDataParser();
    myEventDataParser = new EventDataParser();
    // Assume the map is populated via some Levels API:
    // mapOfGameLevels = LevelsAPI.getMapOfLevels();
  }

  /**
   * Parses the level XML file and returns a LevelData record.
   *
   * Steps:
   * <ul>
   *   <li>Checks if the levels map contains the given filePath key.</li>
   *   <li>Extracts the level name from the file name (removes the ".xml" extension).</li>
   *   <li>Locates the actual File corresponding to the level.</li>
   *   <li>Parses the XML, retrieves the root element (<code>&lt;map&gt;</code>), and then uses helper
   *       parsers to obtain blueprint and layer/game object data.</li>
   *   <li>Constructs and returns a new LevelData record.</li>
   * </ul>
   *
   * @param filePath the key representing the game or level directory
   * @return a LevelData record representing the parsed level
   */
  public LevelData parseLevelFile(String filePath) {
    // Ensure the map has an entry for the provided filePath.

    // Remove the .xml extension to derive the level name.

    // Find the corresponding File from the map.
    File levelFile = new File("/Users/billym./oogasalad/oogasalad_team03/data/gameData/levels/dinosaurgame/Example_File1.xml");
//    Map<String, List<File>> levelDirectories = mapOfGameLevels.get(filePath);
//    for (Entry<String, List<File>> entry : levelDirectories.entrySet()) {
//      for (File file : entry.getValue()) {
//          levelFile = file;
//          break;
//        }
//      if (levelFile != null){
//        break;}
//    }
//    if (levelFile == null) {
//      throw new RuntimeException("Level file " + levelFile.getName() + " not found for game " + filePath);
//    }

    String levelName = levelFile.getName();

    try {
      // Parse the XML file.
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(levelFile);
      doc.getDocumentElement().normalize();

      // Assume the root element is <map>.
      Element root = doc.getDocumentElement();
      int levelWidth = Integer.parseInt(root.getAttribute("width"));
      int levelHeight = Integer.parseInt(root.getAttribute("height"));

      List<EventData> eventList = myEventDataParser.getLevelEvents(root);
      // Use the BlueprintDataParser to extract blueprint data.
      // Assume getBlueprintData returns a Map<Integer, BlueprintData>.
      Map<Integer,BlueprintData> blueprintData = myGameObjectParser.getBlueprintData(root,eventList);
      // Locate the layers element (assumed to be named "layers").
      // Use the LayerDataParser to extract game objects, organized by layer.
      Map<Integer, List<GameObjectData>> gameObjectsByLayer = layerDataParser.getGameObjectDataMap(root);

      System.out.println(eventList.size());
      System.out.println(blueprintData.size());
      System.out.println(gameObjectsByLayer.size());
      for(Integer i : blueprintData.keySet()){
        System.out.println(blueprintData.get(i));
      }
      for (Entry<Integer, List<GameObjectData>> entry : gameObjectsByLayer.entrySet()) {
        System.out.println(entry.getKey());
        for (GameObjectData gameObjectData : entry.getValue()) {
          System.out.println(gameObjectData);
        }
      }

      // Construct and return the LevelData record.
      return new LevelData(levelName, levelWidth,levelHeight, blueprintData, gameObjectsByLayer);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error parsing level file ", e);
    }
  }
}

