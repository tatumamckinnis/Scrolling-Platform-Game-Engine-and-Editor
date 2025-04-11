package oogasalad.fileparser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.StringReader;
import oogasalad.exceptions.CameraParserException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.fileparser.records.CameraData;

/**
 * JUnit tests for CameraDataParser.
 *
 * <p>This test class follows the guidelines:
 * <ul>
 *   <li>Each test method is annotated with @Test.</li>
 *   <li>Method names follow the [MethodName_StateUnderTest_ExpectedBehavior] format.</li>
 *   <li>Variable names in tests reflect input and expected state.</li>
 *   <li>The "ZOMBIES" acronym (Zero, One, Many, Boundary, Invalid, Exception, Stress) guides testing scenarios.</li>
 * </ul>
 * If any test fails:
 * <ol>
 *   <li>Comment out the buggy code.</li>
 *   <li>Write a comment indicating the cause of the error.</li>
 *   <li>Provide the corrected code.</li>
 *   <li>Re-run the tests to verify they pass.</li>
 * </ol>
 */
public class CameraDataParserTest {

  /**
   * Helper method to create a DOM Document from an XML string.
   *
   * @param xml the XML string to be parsed
   * @return a {@link Document} representing the parsed XML
   * @throws Exception if an error occurs during parsing
   */
  private Document loadXMLFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xml)));
  }

  /**
   * Tests that parseCameraData throws a CameraParserException when no cameraData element is present.
   *
   * <p>This test represents a "Zero" scenario where the necessary cameraData element is missing.
   * The parser should not be able to find camera data and consequently throw an exception.</p>
   *
   * @throws Exception if an error occurs during XML parsing
   */
  @Test
  public void parseCameraData_NoCameraData_ThrowsException() throws Exception {
    String xml = "<root><dummy>Test</dummy></root>";
    Document doc = loadXMLFromString(xml);
    CameraDataParser parser = new CameraDataParser();

    assertThrows(CameraParserException.class, () -> {
      parser.parseCameraData(doc.getDocumentElement());
    });
  }

  /**
   * Tests that parseCameraData correctly parses a valid cameraData XML and returns a CameraData record.
   *
   * <p>This test represents a "One" scenario where a single valid cameraData element is present.
   * The XML includes a cameraData element with a shape attribute, nested double and string properties.
   * The parser should return a CameraData record with the correct camera shape and property mappings.
   * The test verifies:
   * <ul>
   *   <li>The camera shape is correctly parsed.</li>
   *   <li>The string property "mode" is mapped to "default".</li>
   *   <li>The double property "fov" is mapped to 75.0.</li>
   * </ul>
   * </p>
   *
   * @throws Exception if an error occurs during XML parsing or property parsing
   */
  @Test
  public void parseCameraData_ValidXML_ReturnsCameraData() throws Exception {
    String xml =
        "<root>" +
            "<cameraData shape='perspective'>" +
            "<doubleProperties>" +
            "<property name='fov'>75.0</property>" +
            "</doubleProperties>" +
            "<stringProperties>" +
            "<property name='mode'>default</property>" +
            "</stringProperties>" +
            "</cameraData>" +
            "</root>";
    Document doc = loadXMLFromString(xml);
    CameraDataParser parser = new CameraDataParser();

    CameraData cameraData = parser.parseCameraData(doc.getDocumentElement());

    // Verify that the camera shape is parsed correctly.
    assertEquals("perspective", cameraData.type(), "Camera shape should be 'perspective'");

    // Verify that the string properties are parsed correctly.
    Map<String, String> stringProps = cameraData.stringProperties();
    assertEquals(1, stringProps.size(), "There should be one string property");
    assertEquals("default", stringProps.get("mode"), "The 'mode' property should be 'default'");

    // Verify that the double properties are parsed correctly.
    Map<String, Double> doubleProps = cameraData.doubleProperties();
    assertEquals(1, doubleProps.size(), "There should be one double property");
    assertEquals(75.0, doubleProps.get("fov"), "The 'fov' property should be 75.0");
  }
}
