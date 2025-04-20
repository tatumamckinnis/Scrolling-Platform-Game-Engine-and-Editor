package oogasalad.fileparser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.exceptions.SpriteSheetLoadException;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteSheetData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Provides a default implementation of the {@code FileParserAPI} interface.
 * <p>
 * This class is responsible for parsing level configuration files, which are in XML format. It
 * leverages various helper parsers:
 * <ul>
 *   <li>{@link LayerDataParser} to extract game object and layer information,</li>
 *   <li>{@link BlueprintDataParser} to extract blueprint data, and</li>
 *   <li>{@link EventDataParser} to extract event-related information.</li>
 *   <li>{@link SpriteSheetDataParser} to extract sprite sheet information.</li>
 * </ul>
 * The parsed data is then bundled into a {@link LevelData} record.
 * </p>
 *
 * @author Billy McCune, Jacob You
 * @see FileParserApi
 * @see LevelData
 */
public class DefaultFileParser implements FileParserApi {

  private LayerDataParser layerDataParser;
  private BlueprintDataParser myGameObjectParser;
  private EventDataParser myEventDataParser;
  private CameraDataParser myCameraDataParser;
  private SpriteSheetDataParser mySpriteSheetDataParser;

  /**
   * Constructs a new {@code DefaultFileParser} and initializes the helper parsers.
   * <p>
   * The constructor instantiates the required helper parsers:
   * <ul>
   *   <li>{@link LayerDataParser} for parsing layer and game object data,</li>
   *   <li>{@link BlueprintDataParser} for parsing blueprint data, and</li>
   *   <li>{@link EventDataParser} for parsing level event data.</li>
   * </ul>
   * </p>
   */
  public DefaultFileParser() {
    layerDataParser = new LayerDataParser();
    myGameObjectParser = new BlueprintDataParser();
    myEventDataParser = new EventDataParser();
    myCameraDataParser = new CameraDataParser();
    mySpriteSheetDataParser = new SpriteSheetDataParser();
  }

  /**
   * Parses the level XML file and returns a LevelData record.
   * <p>
   * Steps:
   * <ul>
   *   <li>Checks if the levels map contains the given filePath key.</li>
   *   <li>Extracts the level name from the
   *   file name (removes the ".xml" extension).</li>
   *   <li>Locates the actual File corresponding to the level.</li>
   *   <li>Parses the XML,
   *   retrieves the root element (<code>&lt;map&gt;</code>), and then uses helper
   *       parsers to obtain blueprint, layer/game object data,
   *       and camera data.</li>
   *   <li>Constructs and returns a new LevelData record.</li>
   * </ul>
   *
   * @param filePath the key representing the game or level directory
   * @return a LevelData record representing the parsed level
   * @throws SAXException                 if there is an issue with the xml document parsing
   * @throws IOException                  if there is an issue with the xml document parsing
   * @throws ParserConfigurationException if there is an issue with the xml document parsing
   */
  public LevelData parseLevelFile(String filePath)
      throws BlueprintParseException, SpriteParseException,
      LevelDataParseException, HitBoxParseException, GameObjectParseException,
      PropertyParsingException, EventParseException {
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

      CameraData cameraData = myCameraDataParser.parseCameraData(root);

      return new LevelData(levelName, minX, minY, maxX, maxY, cameraData, blueprintData,
          gameObjectDataList);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new LevelDataParseException(e.getMessage(), e);
    }
  }

  /**
   * Parse the specified sprite sheet XML file and return the sprite data.
   *
   * @param filePath the path to the sprite sheet to be parsed
   * @return the {@link SpriteSheetData} record representing the parsed sprite sheet data
   * @throws SpriteSheetLoadException if an error occurs while parsing the sprite sheet
   */
  @Override
  public SpriteSheetData parseSpriteSheet(String filePath) throws SpriteSheetLoadException {
    File spriteSheetFile = new File(filePath);

    try {
      Document doc = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder()
          .parse(spriteSheetFile);

      Element root = doc.getDocumentElement();
      return mySpriteSheetDataParser.getSpriteSheetData(root);

    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new SpriteSheetLoadException("Failed to load spritesheet: " + e.getMessage(), e);
    }
  }
}

