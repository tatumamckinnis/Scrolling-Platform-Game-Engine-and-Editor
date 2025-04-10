package oogasalad.fileParsertests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import oogasalad.exceptions.PropertyParsingException;
import oogasalad.fileparser.PropertyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for the {@link PropertyParser} class.
 *
 * @author Billy McCune
 */
public class PropertyParserTest {

  private PropertyParser parser;
  private DocumentBuilder builder;

  @BeforeEach
  void setUp() throws Exception {
    parser = new PropertyParser();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    builder = factory.newDocumentBuilder();
  }

  @Test
  void parseDoubleProperties_ValidInput_ReturnsExpectedMap() throws Exception {
    String xml = """
        <object>
          <doubleProperties>
            <data name="speed" value="3.5" />
            <data name="jumpHeight">2.0</data>
          </doubleProperties>
        </object>
        """;
    Element root = getRoot(xml);

    Map<String, Double> result = parser.parseDoubleProperties(root, "doubleProperties", "data");

    assertEquals(2, result.size());
    assertEquals(3.5, result.get("speed"));
    assertEquals(2.0, result.get("jumpHeight"));
  }

  @Test
  void parseStringProperties_ValidInput_ReturnsExpectedMap() throws Exception {
    String xml = """
        <object>
          <stringProperties>
            <data name="type" value="enemy" />
            <data name="color">red</data>
          </stringProperties>
        </object>
        """;
    Element root = getRoot(xml);

    Map<String, String> result = parser.parseStringProperties(root, "stringProperties", "data");
    System.out.println(result);
    assertEquals(2, result.size());
    assertEquals("enemy", result.get("type"));
    assertEquals("red", result.get("color"));
  }

  @Test
  void parseDoubleProperties_MissingValue_ThrowsException() throws Exception {
    String xml = """
        <object>
          <doubleProperties>
            <data name="speed" value="fast" />
          </doubleProperties>
        </object>
        """;
    Element root = getRoot(xml);

    assertThrows(PropertyParsingException.class, () -> {
      parser.parseDoubleProperties(root, "doubleProperties", "data");
    });
  }

  @Test
  void parseProperties_MissingPropertiesTag_ReturnsEmptyMap() throws Exception {
    String xml = """
        <object>
        </object>
        """;
    Element root = getRoot(xml);

    Map<String, String> result = parser.parseStringProperties(root, "stringProperties", "data");
    assertTrue(result.isEmpty());
  }

  private Element getRoot(String xmlString) throws Exception {
    Document doc = builder.parse(new java.io.ByteArrayInputStream(xmlString.getBytes()));
    return doc.getDocumentElement();
  }
}
