package oogasalad.fileparser;

import java.util.Map;
import oogasalad.exceptions.CameraParserException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.fileparser.records.CameraData;
import org.w3c.dom.Element;

/**
 * Parses camera data from an XML element.
 *
 * <p>The {@code CameraDataParser} is responsible for extracting camera configuration from an XML
 * structure. It searches for a child element named {@code "cameraData"} within the provided root
 * element. If the {@code "cameraData"} element is missing, a {@link CameraParserException} is
 * thrown. When found, it retrieves the type attribute and uses a {@link PropertyParser} to extract
 * both double and string properties, which are then encapsulated in a {@link CameraData}
 * record.</p>
 *
 * @author Billy McCune
 */
public class CameraDataParser {

  /**
   * Parses the camera data contained within the provided XML root element.
   *
   * <p>This method looks for the first occurrence of a child element named {@code "cameraData"}.
   * If such an element exists, the method extracts the camera {@code type} attribute and parses the
   * camera properties using a {@link PropertyParser}. It distinguishes between properties that
   * should be interpreted as {@code double} values and those as {@code String} values, storing them
   * in separate maps. The parsed data is returned as a {@link CameraData} record.</p>
   *
   * @param root the root XML element that contains the camera data
   * @return a {@link CameraData} record holding the camera's type and its properties
   * @throws CameraParserException    if no {@code "cameraData"} element is found in the provided
   *                                  root
   * @throws PropertyParsingException if an error occurs during the parsing of camera properties
   */
  public CameraData parseCameraData(Element root) throws PropertyParsingException {
    Element cameraElement = (Element) root.getElementsByTagName("cameraData").item(0);
    if (cameraElement == null) {
      throw new CameraParserException("No Camera Data found");
    }
    PropertyParser propertyParser = new PropertyParser();
    String type = cameraElement.getAttribute("type");
    Map<String, Double> doubleProperties = propertyParser.parseDoubleProperties(cameraElement,
        "doubleProperties", "property");
    Map<String, String> stringProperties = propertyParser.parseStringProperties(cameraElement,
        "stringProperties", "property");
    return new CameraData(type, stringProperties, doubleProperties);
  }

}
