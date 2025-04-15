package oogasalad.fileparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.fileparser.records.HitBoxData;

/**
 * JUnit tests for HitBoxDataParser.
 *
 * <p>This test class follows the guidelines:
 * <ul>
 *   <li>Each test method is annotated with @Test.</li>
 *   <li>Method names follow the [MethodName_StateUnderTest_ExpectedBehavior] format.</li>
 *   <li>The tests cover various scenarios (valid input, invalid numeric values, and missing numeric attributes).</li>
 * </ul>
 * </p>
 */
public class HitBoxDataParserTest {

  /**
   * Helper method to create an org.w3c.dom.Element from an XML string.
   *
   * @param xml the XML string to be parsed
   * @return the root element of the parsed XML document
   * @throws Exception if an error occurs during XML parsing
   */
  private Element loadElementFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xml))).getDocumentElement();
  }

  /**
   * Tests that a valid hitbox XML element produces a correct HitBoxData record.
   *
   * <p>This "One" scenario validates that all hitbox attributes, including shape and numeric
   * values, are correctly parsed.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getHitBoxData_ValidInput_ReturnsCorrectHitBoxData() throws Exception {
    String xml = "<object hitBoxShape='rectangle' " +
        "hitBoxWidth='100' " +
        "hitBoxHeight='200' " +
        "spriteDx='10' " +
        "spriteDy='20' />";
    Element element = loadElementFromString(xml);
    HitBoxDataParser parser = new HitBoxDataParser();

    HitBoxData data = parser.getHitBoxData(element);

    // Verify that all fields are parsed correctly.
    assertEquals("rectangle", data.shape(), "The hit box shape should be 'rectangle'");
    assertEquals(100, data.hitBoxWidth(), "The hit box width should be 100");
    assertEquals(200, data.hitBoxHeight(), "The hit box height should be 200");
    assertEquals(10, data.spriteDx(), "The spriteDx should be 10");
    assertEquals(20, data.spriteDy(), "The spriteDy should be 20");
  }

  /**
   * Tests that a non-numeric value for hitBoxWidth results in a HitBoxParseException.
   *
   * <p>This "Invalid" scenario provides a non-numeric hitBoxWidth value which should trigger a
   * NumberFormatException internally, and then be caught and rethrown as a HitBoxParseException.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getHitBoxData_InvalidWidth_ThrowsHitBoxParseException() throws Exception {
    String xml = "<object hitBoxShape='rectangle' " +
        "hitBoxWidth='abc' " +
        "hitBoxHeight='200' " +
        "spriteDx='10' " +
        "spriteDy='20' />";
    Element element = loadElementFromString(xml);
    HitBoxDataParser parser = new HitBoxDataParser();

    assertThrows(HitBoxParseException.class, () -> {
      parser.getHitBoxData(element);
    });
  }

  /**
   * Tests that a missing numeric attribute (hitBoxWidth) triggers a HitBoxParseException.
   *
   * <p>This "Invalid" scenario simulates a missing hitBoxWidth attribute. The parser will attempt
   * to parse an empty string, causing a NumberFormatException that is then rethrown as a
   * HitBoxParseException.
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void getHitBoxData_MissingWidth_ThrowsHitBoxParseException() throws Exception {
    String xml = "<object hitBoxShape='rectangle' " +
        "hitBoxHeight='200' " +
        "spriteDx='10' " +
        "spriteDy='20' />";
    Element element = loadElementFromString(xml);
    HitBoxDataParser parser = new HitBoxDataParser();

    assertThrows(HitBoxParseException.class, () -> {
      parser.getHitBoxData(element);
    });
  }
}
