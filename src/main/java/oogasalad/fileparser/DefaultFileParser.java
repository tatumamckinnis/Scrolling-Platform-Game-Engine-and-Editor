package oogasalad.fileparser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DefaultFileParser implements FileParserAPI {

  private LayerDataParser layerDataParser;
  private BlueprintDataParser myGameObjectParser;
  private EventDataParser myEventDataParser;

  public DefaultFileParser() {
    layerDataParser = new LayerDataParser();
    myGameObjectParser = new BlueprintDataParser();
    myEventDataParser = new EventDataParser();
  }

  /**
   * Parses the level XML file and returns a LevelData record.
   * <p>
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
  public LevelData parseLevelFile(String filePath)
      throws BlueprintParseException, SpriteParseException {
    File levelFile = new File(filePath);

    String levelName = levelFile.getName();

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(levelFile);
      doc.getDocumentElement().normalize();

      Element root = doc.getDocumentElement();
      int minX = Integer.parseInt(root.getAttribute("minX"));
      int minY = Integer.parseInt(root.getAttribute("minY"));
      int maxX = Integer.parseInt(root.getAttribute("maxX"));
      int maxY = Integer.parseInt(root.getAttribute("maxY"));

      List<EventData> eventList = myEventDataParser.getLevelEvents(root);

      Map<Integer, BlueprintData> blueprintData = myGameObjectParser.getBlueprintData(root,
          eventList);

      List<GameObjectData> gameObjectDataList = layerDataParser.getGameObjectDataList(
          root);

      return new LevelData(levelName, minX, minY, maxX, maxY, blueprintData, gameObjectDataList);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }
}

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

//      System.out.println(eventList.size());
//      System.out.println(blueprintData.size());
//      System.out.println(gameObjectsByLayer.size());
//      for(Integer i : blueprintData.keySet()){
//        System.out.println(blueprintData.get(i));
//      }
//      for (Entry<Integer, List<GameObjectData>> entry : gameObjectsByLayer.entrySet()) {
//        System.out.println(entry.getKey());
//        for (GameObjectData gameObjectData : entry.getValue()) {
//          System.out.println(gameObjectData);
//        }
//      }

// Construct and return the LevelData record.

