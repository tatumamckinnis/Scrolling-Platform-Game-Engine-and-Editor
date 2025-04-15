package oogasalad.fileparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.fileparser.records.GameObjectData;

/**
 * JUnit tests for GameObjectDataParser.
 *
 * <p>This test class adheres to the following guidelines:
 * <ul>
 *   <li>Each test method is annotated with @Test.</li>
 *   <li>Method names follow the [MethodName_StateUnderTest_ExpectedBehavior] naming format.</li>
 *   <li>The "ZOMBIES" approach is used to cover scenarios (Zero, One, Many, Boundary, Invalid,
 *   Exception, Stress).</li>
 *   <li>If any test fails, please follow the provided steps to correct the issue.</li>
 * </ul>
 * </p>
 */
public class GameObjectDataParserTest {

  /**
   * Helper method that converts an XML string to an org.w3c.dom.Element.
   *
   * @param xml the XML string to be parsed
   * @return the root element of the parsed XML document
   * @throws Exception if an error occurs during parsing
   */
  private Element loadElementFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xml))).getDocumentElement();
  }

  /**
   * Tests that a valid gameObject element with matching uid and coordinate entries returns the
   * expected list of GameObjectData records.
   *
   * <p>This "One" scenario validates that for a correct input, the parser extracts:
   * <ul>
   *   <li>The blueprint ID correctly.</li>
   *   <li>The individual coordinate pairs as x and y values.</li>
   *   <li>The UUIDs matching the order provided in the uid attribute.</li>
   *   <li>The supplied z-index layer.</li>
   * </ul>
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectData_ValidInput_ReturnsCorrectRecords() throws Exception {
    // Example XML with two coordinate pairs and two corresponding UUIDs.
    String xml = "<gameObject id=\"101\" " +
        "uid=\"550e8400-e29b-41d4-a716-446655440000, 550e8400-e29b-41d4-a716-446655440001\" " +
        "coordinates=\"(10,20),(30,40)\"/>";
    Element element = loadElementFromString(xml);
    GameObjectDataParser parser = new GameObjectDataParser();
    int z = 5;

    List<GameObjectData> dataList = parser.getGameObjectData(element, z);
    assertEquals(2, dataList.size(), "Expected two GameObjectData records");

    // Verify the first record.
    GameObjectData firstData = dataList.get(0);
    assertEquals(101, firstData.blueprintId(), "Blueprint ID should be 101");
    assertEquals(10, firstData.x(), "x-coordinate should be 10");
    assertEquals(20, firstData.y(), "y-coordinate should be 20");
    assertEquals(z, firstData.layer(), "z-index should be " + z);
    UUID expectedFirstUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    assertEquals(expectedFirstUuid, firstData.uniqueId(), "First UUID must match the expected value");

    // Verify the second record.
    GameObjectData secondData = dataList.get(1);
    assertEquals(101, secondData.blueprintId(), "Blueprint ID should be 101");
    assertEquals(30, secondData.x(), "x-coordinate should be 30");
    assertEquals(40, secondData.y(), "y-coordinate should be 40");
    assertEquals(z, secondData.layer(), "z-index should be " + z);
    UUID expectedSecondUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    assertEquals(expectedSecondUuid, secondData.uniqueId(), "Second UUID must match the expected value");
  }

  /**
   * Tests that when there are more coordinate entries than UID values, the parser only returns
   * as many GameObjectData records as there are UIDs.
   *
   * <p>This "Boundary" test ensures that extra coordinates are ignored once the UID array is exhausted.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectData_InsufficientUids_ReturnsPartialRecords() throws Exception {
    // Only one UUID is provided, but two coordinate pairs exist.
    String xml = "<gameObject id=\"102\" " +
        "uid=\"550e8400-e29b-41d4-a716-446655440000\" " +
        "coordinates=\"(15,25),(35,45)\"/>";
    Element element = loadElementFromString(xml);
    GameObjectDataParser parser = new GameObjectDataParser();
    int z = 3;

    List<GameObjectData> dataList = parser.getGameObjectData(element, z);
    // Expect only one record to be created.
    assertEquals(1, dataList.size(), "Should only create one GameObjectData record");
    GameObjectData data = dataList.get(0);
    assertEquals(102, data.blueprintId(), "Blueprint ID should be 102");
    assertEquals(15, data.x(), "x-coordinate should be 15");
    assertEquals(25, data.y(), "y-coordinate should be 25");
    assertEquals(z, data.layer(), "z-index should be " + z);
    UUID expectedUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    assertEquals(expectedUuid, data.uniqueId(), "UUID must match the expected value");
  }

  /**
   * Tests that a non-numeric blueprint ID causes a GameObjectParseException.
   *
   * <p>This "Invalid" test provides an invalid numeric value in the id attribute and expects the parser
   * to catch the resulting NumberFormatException and rethrow it as a GameObjectParseException.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectData_InvalidId_ThrowsGameObjectParseException() throws Exception {
    // The id attribute is non-numeric.
    String xml = "<gameObject id=\"abc\" " +
        "uid=\"550e8400-e29b-41d4-a716-446655440000\" " +
        "coordinates=\"(10,20)\"/>";
    Element element = loadElementFromString(xml);
    GameObjectDataParser parser = new GameObjectDataParser();
    int z = 2;

    assertThrows(GameObjectParseException.class, () -> {
      parser.getGameObjectData(element, z);
    });
  }

  /**
   * Tests that malformed coordinate data results in a GameObjectParseException.
   *
   * <p>This "Invalid" scenario supplies a coordinate value where one component is non-numeric.
   * The parser should throw a GameObjectParseException.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectData_InvalidCoordinates_ThrowsGameObjectParseException() throws Exception {
    // Coordinates contain a non-numeric value ("foo").
    String xml = "<gameObject id=\"103\" " +
        "uid=\"550e8400-e29b-41d4-a716-446655440000\" " +
        "coordinates=\"(10,foo)\"/>";
    Element element = loadElementFromString(xml);
    GameObjectDataParser parser = new GameObjectDataParser();
    int z = 0;

    assertThrows(GameObjectParseException.class, () -> {
      parser.getGameObjectData(element, z);
    });
  }

  /**
   * Tests that an invalid UID format (non-UUID string) throws an exception.
   *
   * <p>This "Invalid" test checks that when the uid attribute is not a valid UUID,
   * UUID.fromString will throw an IllegalArgumentException.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectData_InvalidUid_ThrowsIllegalArgumentException() throws Exception {
    // The uid attribute is malformed.
    String xml = "<gameObject id=\"104\" " +
        "uid=\"invalid-uuid\" " +
        "coordinates=\"(10,20)\"/>";
    Element element = loadElementFromString(xml);
    GameObjectDataParser parser = new GameObjectDataParser();
    int z = 1;

    // Expecting IllegalArgumentException from UUID.fromString.
    assertThrows(IllegalArgumentException.class, () -> {
      parser.getGameObjectData(element, z);
    });
  }
}
