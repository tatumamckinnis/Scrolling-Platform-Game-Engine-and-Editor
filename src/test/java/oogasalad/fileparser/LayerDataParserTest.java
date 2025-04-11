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
 * JUnit tests for LayerDataParser.
 *
 * <p>This test class covers several scenarios:
 * <ul>
 *   <li>A valid XML input with multiple layers and game objects.</li>
 *   <li>A layer with an empty data element (no game objects).</li>
 *   <li>An invalid z-index attribute for a layer causing a NumberFormatException.</li>
 *   <li>A missing layers element, causing a NullPointerException.</li>
 * </ul>
 * </p>
 */
public class LayerDataParserTest {

  /**
   * Helper method that converts an XML string to an org.w3c.dom.Element.
   *
   * @param xml the XML string to parse
   * @return the root element of the parsed XML document
   * @throws Exception if an error occurs during parsing
   */
  private Element loadElementFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xml))).getDocumentElement();
  }

  /**
   * Tests that a valid XML input containing layers and game objects returns the correct list
   * of GameObjectData records.
   *
   * <p>
   * In this test:
   * <ul>
   *   <li>The first layer (z="1") contains two game objects with blueprint IDs 101 and 102.</li>
   *   <li>The second layer (z="2") contains one game object with blueprint ID 103.</li>
   *   <li>Each game object element includes valid attributes for id, uid, and coordinates.</li>
   * </ul>
   * The test verifies that the returned list has the expected size and that each record
   * has the correct attributes including the layer's z-index.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectDataList_ValidInput_ReturnsCorrectGameObjectData() throws Exception {
    String xml =
        "<root>" +
            "<layers>" +
            "<layer z='1'>" +
            "<data>" +
            "<object id='101' uid='550e8400-e29b-41d4-a716-446655440000' coordinates='(10,20)'/>" +
            "<object id='102' uid='550e8400-e29b-41d4-a716-446655440001' coordinates='(30,40)'/>" +
            "</data>" +
            "</layer>" +
            "<layer z='2'>" +
            "<data>" +
            "<object id='103' uid='550e8400-e29b-41d4-a716-446655440002' coordinates='(50,60)'/>" +
            "</data>" +
            "</layer>" +
            "</layers>" +
            "</root>";
    Element root = loadElementFromString(xml);
    LayerDataParser parser = new LayerDataParser();

    List<GameObjectData> gameObjectDataList = parser.getGameObjectDataList(root);

    // Expect three game objects in total.
    assertEquals(3, gameObjectDataList.size(), "Expected 3 game objects in total");

    // Verify the first object (from layer z=1).
    GameObjectData obj1 = gameObjectDataList.get(0);
    assertEquals(101, obj1.blueprintId(), "First object's blueprint id should be 101");
    assertEquals(10, obj1.x(), "First object's x-coordinate should be 10");
    assertEquals(20, obj1.y(), "First object's y-coordinate should be 20");
    assertEquals(1, obj1.layer(), "First object's z-index should be 1");
    UUID expectedUuid1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    assertEquals(expectedUuid1, obj1.uniqueId(), "First object's uuid should match");

    // Verify the second object (also from layer z=1).
    GameObjectData obj2 = gameObjectDataList.get(1);
    assertEquals(102, obj2.blueprintId(), "Second object's blueprint id should be 102");
    assertEquals(30, obj2.x(), "Second object's x-coordinate should be 30");
    assertEquals(40, obj2.y(), "Second object's y-coordinate should be 40");
    assertEquals(1, obj2.layer(), "Second object's z-index should be 1");
    UUID expectedUuid2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    assertEquals(expectedUuid2, obj2.uniqueId(), "Second object's uuid should match");

    // Verify the third object (from layer z=2).
    GameObjectData obj3 = gameObjectDataList.get(2);
    assertEquals(103, obj3.blueprintId(), "Third object's blueprint id should be 103");
    assertEquals(50, obj3.x(), "Third object's x-coordinate should be 50");
    assertEquals(60, obj3.y(), "Third object's y-coordinate should be 60");
    assertEquals(2, obj3.layer(), "Third object's z-index should be 2");
    UUID expectedUuid3 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    assertEquals(expectedUuid3, obj3.uniqueId(), "Third object's uuid should match");
  }

  /**
   * Tests that when a layer contains a data element with no game objects,
   * the parser returns an empty list.
   *
   * <p>
   * This "Zero" scenario confirms that a layer with an empty or missing object list will not produce any game object data.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectDataList_EmptyLayerData_ReturnsEmptyList() throws Exception {
    String xml =
        "<root>" +
            "<layers>" +
            "<layer z='1'>" +
            "<data>" +
            // No object elements here.
            "</data>" +
            "</layer>" +
            "</layers>" +
            "</root>";
    Element root = loadElementFromString(xml);
    LayerDataParser parser = new LayerDataParser();

    List<GameObjectData> gameObjectDataList = parser.getGameObjectDataList(root);
    assertTrue(gameObjectDataList.isEmpty(), "Expected an empty list when no game objects are present");
  }

  /**
   * Tests that an invalid z-index attribute in a layer causes an exception.
   *
   * <p>
   * This "Invalid" scenario uses a non-numeric z attribute and expects a NumberFormatException.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectDataList_InvalidLayerZ_ThrowsException() throws Exception {
    String xml =
        "<root>" +
            "<layers>" +
            "<layer z='invalid'>" +
            "<data>" +
            "<object id='101' uid='550e8400-e29b-41d4-a716-446655440000' coordinates='(10,20)'/>" +
            "</data>" +
            "</layer>" +
            "</layers>" +
            "</root>";
    Element root = loadElementFromString(xml);
    LayerDataParser parser = new LayerDataParser();

    assertThrows(NumberFormatException.class, () -> {
      parser.getGameObjectDataList(root);
    });
  }

  /**
   * Tests that a missing layers element in the XML causes a NullPointerException.
   *
   * <p>
   * This scenario verifies that if the expected "layers" element is absent, the parser fails (as layersElement becomes null).
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getGameObjectDataList_MissingLayersElement_ThrowsException() throws Exception {
    String xml =
        "<root>" +
            // No layers element present.
            "</root>";
    Element root = loadElementFromString(xml);
    LayerDataParser parser = new LayerDataParser();

    assertThrows(NullPointerException.class, () -> {
      parser.getGameObjectDataList(root);
    });
  }
}
