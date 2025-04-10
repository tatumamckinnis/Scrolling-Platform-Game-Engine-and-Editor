package oogasalad.fileparser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import oogasalad.exceptions.PropertyParsingException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The {@code PropertyParser} class is responsible for parsing XML elements that represent
 * properties. It provides public methods to parse both double and string properties contained in
 * designated XML nodes.
 * <p>
 * By default, property children are expected to have the tag name "data". However, overloaded
 * methods allow clients to specify an alternative child tag (for example, "property" when using the
 * format
 * <code>&lt;properties&gt;&lt;property&gt;...&lt;/property&gt;&lt;/properties&gt;</code>).
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *   Element blueprintElement = ...; // your XML blueprint element
 *   PropertyParser parser = new PropertyParser();
 *   // Using the default "data" tag:
 *   Map&lt;String, Double&gt; doubleProps = parser.parseDoubleProperties(blueprintElement);
 *   Map&lt;String, String&gt; stringProps = parser.parseStringProperties(blueprintElement);
 *
 *   // Using a custom child tag "property":
 *   Map&lt;String, Double&gt; doublePropsAlt = parser.parseDoubleProperties(blueprintElement, "property");
 *   Map&lt;String, String&gt; stringPropsAlt = parser.parseStringProperties(blueprintElement, "property");
 * </pre>
 * </p>
 */
public class PropertyParser {


  /**
   * Parses the <code>&lt;doubleProperties&gt;</code> element containing child elements with the
   * given tag holding property data.
   *
   * @param objectNode the XML element containing the <code>&lt;doubleProperties&gt;</code> node.
   * @param childTag   the tag name of the child elements holding property data.
   * @return a map with property names and their corresponding double values.
   * @throws PropertyParsingException if the node is not found or if a value cannot be parsed as a
   *                                  double.
   */
  public Map<String, Double> parseDoubleProperties(Element objectNode, String propertiesTag,
      String childTag)
      throws PropertyParsingException {
    return parseProperties(objectNode, propertiesTag, childTag, Double::parseDouble,
        "error.invalid.doubleValue");
  }


  /**
   * Parses the <code>&lt;stringProperties&gt;</code> element containing child elements with the
   * given tag name holding property data.
   *
   * @param objectNode the XML element containing the <code>&lt;stringProperties&gt;</code> node.
   * @param childTag   the tag name of the child elements holding property data.
   * @return a map with property names and their corresponding string values.
   * @throws PropertyParsingException if the node is not in the expected format.
   */
  public Map<String, String> parseStringProperties(Element objectNode, String propertiesTag,
      String childTag)
      throws PropertyParsingException {
    return parseProperties(objectNode, propertiesTag, childTag, s -> s,
        "error.stringProperties.conversion");
  }

  /**
   * Parses the XML element containing property data into a map whose values have been converted
   * using the provided converter function.
   *
   * @param <T>           the type of the property value.
   * @param parentNode    the element that contains the properties node.
   * @param propertiesTag the tag name of the properties node (e.g., "doubleProperties" or
   *                      "stringProperties").
   * @param childTag      the tag name of the child elements holding property data.
   * @param converter     a {@link Function} to convert property values from {@code String} to the
   *                      desired type.
   * @param errorPrefix   error message prefix if conversion fails.
   * @return a map of property names to their converted values.
   * @throws PropertyParsingException if the node is not found or if a conversion fails.
   */
  private <T> Map<String, T> parseProperties(Element parentNode, String propertiesTag,
      String childTag,
      Function<String, T> converter, String errorPrefix) throws PropertyParsingException {
    Element propertiesElement = getPropertiesElement(parentNode, propertiesTag);
    if (propertiesElement == null) {
      return new HashMap<>();
    }
    return processDataNodes(propertiesElement, childTag, converter, errorPrefix);
  }

  /**
   * Retrieves the properties element from the blueprint node using the specified tag.
   *
   * @param parentNode    the element that contains the properties node.
   * @param propertiesTag the tag name of the properties node.
   * @return the properties element if present; {@code null} otherwise.
   * @throws PropertyParsingException if the node is found but is not an element.
   */
  private Element getPropertiesElement(Element parentNode, String propertiesTag)
      throws PropertyParsingException {
    NodeList propertyList = parentNode.getElementsByTagName(propertiesTag);
    if (propertyList.getLength() > 0) {
      Node node = propertyList.item(0);
      if (!(node instanceof Element)) {
        throw new PropertyParsingException("error." + propertiesTag + ".notElement");
      }
      return (Element) node;
    }
    return null;
  }

  /**
   * Processes all child nodes of the given properties element and returns a map of property names
   * to their converted values.
   *
   * @param <T>               the type of the property value.
   * @param propertiesElement the element containing the child property nodes.
   * @param childTag          the tag name of the child elements to process.
   * @param converter         a {@link Function} to convert property values from {@code String} to
   *                          the desired type.
   * @param errorPrefix       error message prefix if conversion fails.
   * @return a map of property names to converted values.
   * @throws PropertyParsingException if any child node is not in the expected format.
   */
  private <T> Map<String, T> processDataNodes(Element propertiesElement, String childTag,
      Function<String, T> converter, String errorPrefix) throws PropertyParsingException {
    Map<String, T> properties = new HashMap<>();
    NodeList dataNodes = propertiesElement.getChildNodes();
    for (int i = 0; i < dataNodes.getLength(); i++) {
      Node dataNode = dataNodes.item(i);
      if (isIgnorableNode(dataNode)) {
        continue;
      }
      if (!(dataNode instanceof Element)) {
        throw new PropertyParsingException("error.data.notElement");
      }
      Element dataElement = (Element) dataNode;
      if (!childTag.equals(dataElement.getTagName())) {
        continue; // Only process child elements with the expected tag.
      }
      processDataElement(dataElement, properties, converter, errorPrefix);
    }
    return properties;
  }

  /**
   * Processes a single property element by extracting its name and value, converting the value
   * using the provided converter, and placing it into the properties map.
   * <p>
   * If the "value" attribute is missing or empty, the element's text content will be used instead.
   * </p>
   *
   * @param <T>         the type of the property value.
   * @param dataElement the property element.
   * @param properties  the map that collects the properties.
   * @param converter   a {@link Function} to convert the property value from {@code String} to the
   *                    desired type.
   * @param errorPrefix error message prefix if conversion fails.
   * @throws PropertyParsingException if the conversion fails.
   */
  private <T> void processDataElement(Element dataElement, Map<String, T> properties,
      Function<String, T> converter, String errorPrefix) throws PropertyParsingException {
    try {
      String name = dataElement.getAttribute("name");
      // Try reading the "value" attribute; if missing or empty, fall back to the element's text content.
      String value = dataElement.getAttribute("value");
      if (value == null || value.isEmpty()) {
        value = dataElement.getTextContent().trim();
        T convertedValue = converter.apply(value);
        properties.put(name, convertedValue);
      }
    } catch (IllegalArgumentException e) {
      throw new PropertyParsingException("error." + errorPrefix + ".illegalValue");
    }
  }

  /**
   * Checks whether the given node is ignorable (i.e., a text node containing only whitespace).
   *
   * @param node the node to check.
   * @return true if the node is ignorable; false otherwise.
   */
  private boolean isIgnorableNode(Node node) {
    return node.getNodeType() == Node.TEXT_NODE && node.getTextContent().trim().isEmpty();
  }
}
