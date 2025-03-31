/**
 * JUnit tests for configAPI.
 *
 * This test document uses the following rules:
 *
 * - Each test method is annotated with @Test.
 * - The method names follow the [MethodName_StateUnderTest_ExpectedBehavior] format.
 *   For example: getAcceptedStates_NullConfig_ThrowsNullPointerException().
 * - Variable names in tests reflect the input and expected state (e.g., ZERO_DIM_CONFIG, SINGLE_STATE_CONFIG).
 * - The ZOMBIES acronym is used to cover scenarios: Zero, One, Many, Boundary, Invalid, Exception, Stress.
 *
 * If any test fails:
 * 1. Comment out the buggy code.
 * 2. Write a comment indicating the cause of the error.
 * 3. Provide the corrected code.
 * 4. Re-run the tests to verify they pass.
 */
package oogasalad.fileParsertests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oogasalad.game.file.parser.GameObjectBlueprintParser;
import oogasalad.game.file.parser.exceptions.GameObjectParseException;
import oogasalad.game.file.parser.records.GameObjectData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GameObjectBlueprintParserTest {

  private GameObjectBlueprintParser parser;

  @BeforeEach
  public void setup() {
    parser = new GameObjectBlueprintParser();
  }

  /**
   * Helper method to create a Document from an XML string.
   */
  private Document createDocumentFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Ensure whitespace is not ignored if needed (to simulate non-element nodes)
    factory.setIgnoringElementContentWhitespace(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * getLevelGameObjectData_ZeroGameNodes_ReturnsEmptyList
   * Zero: Test with a root element that has no <game> nodes.
   */
  @Test
  public void getLevelGameObjectData_ZeroGameNodes_ReturnsEmptyList() throws Exception {
    String xml = "<root></root>";
    Document doc = createDocumentFromString(xml);
    Element root = doc.getDocumentElement();

    List<GameObjectData> result = parser.getLevelGameObjectData(root);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  /**
   * getLevelGameObjectData_SingleGameSingleObject_ReturnsCorrectData
   * One: Test with a valid XML containing one <game> with one object.
   */
  @Test
  public void getLevelGameObjectData_SingleGameSingleObject_ReturnsCorrectData() throws Exception {
    String xml = "<root>" +
        "  <game name='TestGame'>" +
        "    <objectGroup name='TestGroup'>" +
        "      <object id='1' type='Enemy' spriteName='enemySprite' spriteFile='enemy.png'>" +
        "      </object>" +
        "    </objectGroup>" +
        "  </game>" +
        "</root>";
    Document doc = createDocumentFromString(xml);
    Element root = doc.getDocumentElement();

    List<GameObjectData> result = parser.getLevelGameObjectData(root);
    assertNotNull(result);
    assertEquals(1, result.size());
    GameObjectData data = result.get(0);
    assertEquals(1, data.id());
    assertEquals("TestGame", data.gameName());
    assertEquals("Enemy", data.type());
    assertEquals("TestGroup", data.group());
    assertEquals("enemySprite", data.spriteName());
    assertEquals("enemy.png", data.spriteFile());
    // Default values for x, y, and layer are 0.
    assertEquals(0, data.x());
    assertEquals(0, data.y());
    assertEquals(0, data.layer());
    // Properties map should be empty.
    assertTrue(data.propertiesForObjectHandlersAndVariables().isEmpty());
  }

  /**
   * getLevelGameObjectData_MultipleGames_ReturnsCombinedGameObjects
   * Many: Test with multiple <game> nodes, each with an <objectGroup> and objects.
   */
  @Test
  public void getLevelGameObjectData_MultipleGames_ReturnsCombinedGameObjects() throws Exception {
    String xml = "<root>" +
        "  <game name='Game1'>" +
        "    <objectGroup name='Group1'>" +
        "      <object id='1' type='Player' spriteName='playerSprite' spriteFile='player.png'></object>" +
        "    </objectGroup>" +
        "  </game>" +
        "  <game name='Game2'>" +
        "    <objectGroup name='Group2'>" +
        "      <object id='2' type='Enemy' spriteName='enemySprite' spriteFile='enemy.png'></object>" +
        "      <object id='3' type='Enemy' spriteName='enemySprite2' spriteFile='enemy2.png'></object>" +
        "    </objectGroup>" +
        "  </game>" +
        "</root>";
    Document doc = createDocumentFromString(xml);
    Element root = doc.getDocumentElement();

    List<GameObjectData> result = parser.getLevelGameObjectData(root);
    assertNotNull(result);
    assertEquals(3, result.size());

    // Validate first game object data.
    GameObjectData data1 = result.get(0);
    assertEquals(1, data1.id());
    assertEquals("Game1", data1.gameName());
    assertEquals("Player", data1.type());
    assertEquals("Group1", data1.group());

    // Validate second game object data.
    GameObjectData data2 = result.get(1);
    assertEquals(2, data2.id());
    assertEquals("Game2", data2.gameName());
    assertEquals("Enemy", data2.type());
    assertEquals("Group2", data2.group());

    // Validate third game object data.
    GameObjectData data3 = result.get(2);
    assertEquals(3, data3.id());
    assertEquals("Game2", data3.gameName());
    assertEquals("Enemy", data3.type());
    assertEquals("Group2", data3.group());
  }

  /**
   * getLevelGameObjectData_InvalidId_ThrowsGameObjectParseException
   * Invalid: Test with a non-numeric id attribute, expecting a GameObjectParseException.
   */
  @Test
  public void getLevelGameObjectData_InvalidId_ThrowsGameObjectParseException() throws Exception {
    String xml = "<root>" +
        "  <game name='TestGame'>" +
        "    <objectGroup name='TestGroup'>" +
        "      <object id='invalid' type='Enemy' spriteName='enemySprite' spriteFile='enemy.png'></object>" +
        "    </objectGroup>" +
        "  </game>" +
        "</root>";
    Document doc = createDocumentFromString(xml);
    Element root = doc.getDocumentElement();

    assertThrows(GameObjectParseException.class, () -> {
      parser.getLevelGameObjectData(root);
    });
  }

  /**
   * getLevelGameObjectData_NonElementInObjectGroup_ThrowsGameObjectParseException
   * Exception: Test with a non-element node inside <objectGroup>, expecting a GameObjectParseException.
   */
  @Test
  public void getLevelGameObjectData_NonElementInObjectGroup_ThrowsGameObjectParseException() throws Exception {
    String xml = "<root>" +
        "  <game name='TestGame'>" +
        "    <objectGroup name='TestGroup'>" +
        "      <object id='1' type='Enemy' spriteName='enemySprite' spriteFile='enemy.png'></object>" +
        "    </objectGroup>" +
        "  </game>" +
        "</root>";
    Document doc = createDocumentFromString(xml);
    Element root = doc.getDocumentElement();

    assertThrows(GameObjectParseException.class, () -> {
      parser.getLevelGameObjectData(root);
    });
  }

  /**
   * getLevelGameObjectData_PropertyHandler_ConcatenatesDuplicateAttributes
   * Boundary: Test that duplicate attribute keys in different <data> nodes within the same handler are concatenated.
   *
   * Note: Since each <data> element normally has distinct attribute names (e.g., "key" and "value"),
   * this test simulates a scenario where two <data> elements contain an attribute with the same name.
   * The parser should concatenate the attribute values using a comma.
   */
  @Test
  public void getLevelGameObjectData_PropertyHandler_ConcatenatesDuplicateAttributes() throws Exception {
    String xml = "<root>" +
        "  <game name='TestGame'>" +
        "    <objectGroup name='TestGroup'>" +
        "      <object id='1' type='Enemy' spriteName='enemySprite' spriteFile='enemy.png'>" +
        "        <properties>" +
        "          <handler>" +
        "            <data damage='10'/>" +
        "            <data damage='5'/>" +
        "          </handler>" +
        "        </properties>" +
        "      </object>" +
        "    </objectGroup>" +
        "  </game>" +
        "</root>";
    Document doc = createDocumentFromString(xml);
    Element root = doc.getDocumentElement();

    List<GameObjectData> result = parser.getLevelGameObjectData(root);
    assertNotNull(result);
    assertEquals(1, result.size());
    GameObjectData data = result.get(0);
    Map<String, Map<String, String>> properties = data.propertiesForObjectHandlersAndVariables();
    assertTrue(properties.containsKey("handler"));
    Map<String, String> handlerData = properties.get("handler");
    // Since the parser concatenates values based on attribute names,
    // we expect the "damage" attribute to be "10,5" after processing.
    assertEquals("10,5", handlerData.get("damage"));
  }

  /**
   * getLevelGameObjectData_Stress_LargeNumberOfObjects
   * Stress: Stress test with a large number of game objects.
   */
  @Test
  public void getLevelGameObjectData_Stress_LargeNumberOfObjects() throws Exception {
    StringBuilder xmlBuilder = new StringBuilder();
    xmlBuilder.append("<root><game name='StressGame'><objectGroup name='StressGroup'>");
    int numObjects = 1000;
    for (int i = 0; i < numObjects; i++) {
      xmlBuilder.append("<object id='").append(i)
          .append("' type='Type").append(i)
          .append("' spriteName='Sprite").append(i)
          .append("' spriteFile='File").append(i).append(".png'></object>");
    }
    xmlBuilder.append("</objectGroup></game></root>");
    Document doc = createDocumentFromString(xmlBuilder.toString());
    Element root = doc.getDocumentElement();

    List<GameObjectData> result = parser.getLevelGameObjectData(root);
    assertNotNull(result);
    assertEquals(numObjects, result.size());
    // Validate a couple of objects.
    GameObjectData first = result.get(0);
    assertEquals(0, first.id());
    GameObjectData last = result.get(result.size() - 1);
    assertEquals(numObjects - 1, last.id());
  }
}

