package oogasalad.fileparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import java.io.StringReader;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;

/**
 * JUnit tests for BlueprintDataParser.
 *
 * <p>This test class verifies that the blueprint parser properly extracts blueprint data from XML
 * and appropriately handles invalid input. It mimics the file structure and property configuration used by
 * SpriteDataParser.
 * </p>
 */
public class BlueprintDataParserTest {

  @TempDir
  Path tempDir;

  private File propertiesDir;
  private File gameDataDir;
  private File graphicsDir;

  @BeforeEach
  void setup() throws Exception {
    // Set the temporary directory as our user.dir.
    System.setProperty("user.dir", tempDir.toAbsolutePath().toString());

    // Create the directory for the properties file at "oogasalad/file/"
    propertiesDir = tempDir.resolve("oogasalad/file").toFile();
    propertiesDir.mkdirs();
    File propFile = new File(propertiesDir, "fileStructure.properties");
    try (FileWriter writer = new FileWriter(propFile)) {
      // Only the keys used by SpriteDataParser (and hence BlueprintDataParser) are needed.
      writer.write("path.to.game.data=oogasalad_team03/data/gameData/gameObjects\n");
      writer.write("path.to.graphics.data=oogasalad_team03/data/graphicsData\n");
    }
    // (Optional) Ensure our tempDir is on the classpath.
    System.setProperty("java.class.path", tempDir.toAbsolutePath().toString());

    // Create directories based on the properties.
    // For game data:
    // Expected path: {user.dir}/oogasalad_team03/data/gameData/gameObjects
    gameDataDir = tempDir.resolve("oogasalad_team03")
        .resolve("data")
        .resolve("gameData")
        .resolve("gameObjects")
        .toFile();
    gameDataDir.mkdirs();

    // For graphics:
    // Expected path: {user.dir}/oogasalad_team03/data/graphicsData
    graphicsDir = tempDir.resolve("oogasalad_team03")
        .resolve("data")
        .resolve("graphicsData")
        .toFile();
    graphicsDir.mkdirs();

    // Create a dummy sprite file for use by BlueprintDataParser.
    // In our blueprint XML we will use:
    // gameName = "TestGame", group = "GroupA", shape = "player", spriteFile = "player.png"
    createDummySpriteFile();
  }

  @AfterEach
  void tearDown() {
    // @TempDir automatically cleans up temporary files after tests.
  }

  /**
   * Helper method to create a dummy sprite file.
   *
   * <p>
   * This method creates the file at the expected location:
   * {user.dir}/oogasalad_team03/data/gameData/gameObjects/TestGame/GroupA/player/player.png
   * containing a minimal valid sprite XML document that defines a sprite with name "PlayerSprite".
   * </p>
   *
   * @throws Exception if an error occurs during file creation.
   */
  private void createDummySpriteFile() throws Exception {
    String baseDir = System.getProperty("user.dir");
    // Expected directory: {baseDir}/oogasalad_team03/data/gameData/gameObjects/TestGame/GroupA/player
    File spriteDir = new File(baseDir, "oogasalad_team03/data/gameData/gameObjects/TestGame/GroupA/player");
    if (!spriteDir.exists()) {
      boolean dirsCreated = spriteDir.mkdirs();
      if (!dirsCreated) {
        throw new Exception("Failed to create directories for dummy sprite file at " + spriteDir.getAbsolutePath());
      }
    }
    // Create sprite file "player.png" with a valid XML document that defines a sprite with name "PlayerSprite".
    File spriteFile = new File(spriteDir, "player.png");
    try (FileOutputStream fos = new FileOutputStream(spriteFile)) {
      String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
          "<sprites imagePath=\"dummy.png\">\n" +
          "  <sprite name=\"PlayerSprite\" x=\"0\" y=\"0\" width=\"10\" height=\"10\">\n" +
          "    <frames></frames>\n" +
          "    <animations></animations>\n" +
          "  </sprite>\n" +
          "</sprites>";
      fos.write(xmlContent.getBytes());
    }
  }

  /**
   * Helper method to delete the dummy sprite file.
   *
   * <p>
   * This cleans up the dummy file after the tests have run.
   * </p>
   */
  private void deleteDummySpriteFile() {
    String baseDir = System.getProperty("user.dir");
    File spriteFile = new File(baseDir, "oogasalad_team03/data/gameData/gameObjects/TestGame/GroupA/player/player.png");
    if (spriteFile.exists()) {
      spriteFile.delete();
    }
    // Optionally, clean up empty parent directories.
  }

  /**
   * Helper method to convert an XML string into an org.w3c.dom.Element.
   *
   * @param xml the XML string to parse.
   * @return the root element of the parsed XML document.
   * @throws Exception if an error occurs during parsing.
   */
  private Element loadXML(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xml))).getDocumentElement();
  }

  /**
   * Tests that a valid blueprint XML input returns a correct map of BlueprintData.
   *
   * <p>
   * The XML defines a single game node with name "TestGame" that contains one object group ("GroupA")
   * and one game object. The game object is defined with a valid numeric id and required attributes.
   * It also includes a child <code>displayedProperties</code> element and empty property nodes.
   * A dummy event list is provided with one event for id "E1". Since the game object's eventIDs
   * attribute is set to "E1,E2", the first event is matched while the second remains missing (null).
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing or blueprint processing.
   */
  @Test
  public void getBlueprintData_ValidInput_ReturnsCorrectBlueprintData() throws Exception {
    // Create a dummy event list with one event (id "E1").
    List<EventData> eventList = new ArrayList<>();
    eventList.add(new EventData("dummy", "E1", new ArrayList<>(), new ArrayList<>()));

    String xml =
        "<root>" +
            "<game name='TestGame'>" +
            "<objectGroup name='GroupA'>" +
            "<object " +
            "id='1' " +
            "velocityX='1.5' " +
            "velocityY='2.5' " +
            "rotation='45' " +
            "shape='player' " +
            "spriteName='PlayerSprite' " +
            "spriteFile='player.png' " +
            "eventIDs='E1,E2'>" +
            "<displayedProperties propertyList='prop1,prop2'/>" +
            "<doubleProperties></doubleProperties>" +
            "<stringProperties></stringProperties>" +
            "</object>" +
            "</objectGroup>" +
            "</game>" +
            "</root>";
    Element root = loadXML(xml);
    BlueprintDataParser parser = new BlueprintDataParser();

    // Process the blueprint data.
    Map<Integer, BlueprintData> blueprintDataMap = parser.getBlueprintData(root, eventList);

    // Validate results.
    assertNotNull(blueprintDataMap, "Blueprint data map should not be null");
    assertTrue(blueprintDataMap.containsKey(1), "Blueprint id 1 should be present");

    BlueprintData bp = blueprintDataMap.get(1);
    assertEquals(1, bp.blueprintId(), "Blueprint id should be 1");
    assertEquals(1.5, bp.velocityX(), 0.001, "velocityX should be 1.5");
    assertEquals(2.5, bp.velocityY(), 0.001, "velocityY should be 2.5");
    assertEquals(45.0, bp.rotation(), 0.001, "rotation should be 45.0");
    assertEquals("TestGame", bp.gameName(), "Game name should be 'TestGame'");
    // Adjust the accessor as defined in BlueprintData (here assumed to be group() for the group name).
    assertEquals("GroupA", bp.group(), "Group name should be 'GroupA'");
    assertEquals("player", bp.type(), "Type should be 'player'");

    // The sprite and hitbox data should be non-null.
    assertNotNull(bp.spriteData(), "SpriteData should not be null");
    assertNotNull(bp.hitBoxData(), "HitBoxData should not be null");

    // Verify event data list.
    // The eventIDs attribute "E1,E2" should yield a list with two entries:
    // first matching "E1" (non-null) and second as null (since "E2" is not provided).
    assertEquals(2, bp.eventDataList().size(), "Event data list should contain two entries");
    assertNotNull(bp.eventDataList().get(0), "First event (E1) should be found");
    assertNull(bp.eventDataList().get(1), "Second event should be null because it is not provided");

    // Verify that the property maps (double and string) are empty when none are provided.
    assertTrue(bp.stringProperties().isEmpty(), "String properties should be empty");
    assertTrue(bp.doubleProperties().isEmpty(), "Double properties should be empty");

    // Verify displayed properties.
    List<String> displayedProps = bp.displayedProperties();
    assertEquals(2, displayedProps.size(), "There should be two displayed properties");
    assertTrue(displayedProps.contains("prop1"), "Displayed properties should contain 'prop1'");
    assertTrue(displayedProps.contains("prop2"), "Displayed properties should contain 'prop2'");
  }

  /**
   * Tests that when a game object node has a non-numeric id attribute,
   * blueprint parsing fails with a BlueprintParseException.
   *
   * <p>
   * The XML contains a game object with id "abc" (non-numeric). The parser should throw a
   * BlueprintParseException.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing.
   */
  @Test
  public void getBlueprintData_InvalidObjectId_ThrowsBlueprintParseException() throws Exception {
    List<EventData> eventList = new ArrayList<>();
    String xml =
        "<root>" +
            "<game name='TestGame'>" +
            "<objectGroup name='GroupA'>" +
            "<object " +
            "id='abc' " +  // Invalid id
            "velocityX='1.5' " +
            "velocityY='2.5' " +
            "rotation='45' " +
            "shape='player' " +
            "spriteName='PlayerSprite' " +
            "spriteFile='player.png'>" +
            "<displayedProperties propertyList='prop1,prop2'/>" +
            "<doubleProperties></doubleProperties>" +
            "<stringProperties></stringProperties>" +
            "</object>" +
            "</objectGroup>" +
            "</game>" +
            "</root>";
    Element root = loadXML(xml);
    BlueprintDataParser parser = new BlueprintDataParser();

    assertThrows(BlueprintParseException.class, () -> {
      parser.getBlueprintData(root, eventList);
    }, "A non-numeric object id should result in a BlueprintParseException");
  }
}
