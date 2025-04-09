package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.EventData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses event-related data from an XML document into a list of {@link EventData} records.
 * <p>
 * This parser extracts events found in the XML and creates {@code EventData} objects by
 * processing the event's attributes, conditions, outcomes, and parameters.
 * </p>
 *
 * @author Billy McCune
 */
public class EventDataParser {
  List<EventData> events;

  /**
   * Extracts the list of event data from the provided XML root element.
   *
   * @param root the XML {@link Element} that contains the event nodes.
   * @return a list of {@link EventData} objects parsed from the XML.
   */
  public List<EventData> getLevelEvents(Element root) {
    NodeList eventNodes = root.getElementsByTagName("event");
    events = new ArrayList<>();
    for (int i = 0; i < eventNodes.getLength(); i++) {
      Element eventElement = (Element) eventNodes.item(i);
      events.add(parseEventNode(eventElement));
    }
    return events;
  }

  /**
   * Parses an individual event node into an {@link EventData} object.
   *
   * @param eventElement the XML {@link Element} representing an event.
   * @return the {@link EventData} object containing the parsed event data.
   */
  private EventData parseEventNode(Element eventElement) {
    String type = eventElement.getAttribute("type");
    String id = eventElement.getAttribute("id");

    // Parse conditions from the "conditions" element
    List<List<String>> conditions = parseEventConditions(
        (Element) eventElement.getElementsByTagName("conditions").item(0));

    // Parse outcomes from the "outcomes" element
    List<String> outcomes = parseOutcomes(
        (Element) eventElement.getElementsByTagName("outcomes").item(0));

    // Parse parameters from the "parameters" element
    Map<String, String> parameters = parseParameters(
        (Element) eventElement.getElementsByTagName("parameters").item(0));

    return new EventData(type, id, conditions, outcomes, parameters);
  }

  /**
   * Parses the conditions defined in an event's <code>conditions</code> element.
   * <p>
   * Each <code>condition</code> element is expected to have a "list" attribute containing a
   * comma-separated string of condition tokens.
   * </p>
   *
   * @param conditionsElement the XML {@link Element} that holds the event conditions.
   * @return a list of condition lists; each inner list contains condition tokens for a single
   *         condition.
   */
  private List<List<String>> parseEventConditions(Element conditionsElement) {
    List<List<String>> conditions = new ArrayList<>();
    if (conditionsElement != null) {
      NodeList conditionNodes = conditionsElement.getElementsByTagName("condition");
      for (int i = 0; i < conditionNodes.getLength(); i++) {
        Element condition = (Element) conditionNodes.item(i);
        String conditionValue = condition.getAttribute("list");
        List<String> conditionList = new ArrayList<>();
        // Split by comma in case multiple tokens are provided in the "list" attribute.
        for (String token : conditionValue.split(",")) {
          conditionList.add(token.trim());
        }
        conditions.add(conditionList);
      }
    }
    return conditions;
  }

  /**
   * Parses the outcomes defined in an event's <code>outcomes</code> element.
   * <p>
   * The outcomes element should have a "list" attribute containing a comma-separated string of
   * outcome tokens.
   * </p>
   *
   * @param outcomeElement the XML {@link Element} that holds the event outcomes.
   * @return a list of outcome tokens.
   */
  private List<String> parseOutcomes(Element outcomeElement) {
    List<String> outcomes = new ArrayList<>();
    if (outcomeElement != null) {
      String outcomeValue = outcomeElement.getAttribute("list");
      // Split by comma to support multiple outcomes if needed.
      for (String token : outcomeValue.split(",")) {
        outcomes.add(token.trim());
      }
    }
    return outcomes;
  }

  /**
   * Parses the parameters defined in an event's <code>parameters</code> element.
   * <p>
   * Each parameter is represented by a <code>parameter</code> element with a "name" attribute
   * and its value provided as text content.
   * </p>
   *
   * @param parametersElement the XML {@link Element} that holds the event parameters.
   * @return a map where the keys are parameter names and the values are the corresponding parameter values.
   */
  private Map<String, String> parseParameters(Element parametersElement) {
    Map<String, String> parameters = new HashMap<>();
    if (parametersElement != null) {
      NodeList parameterNodes = parametersElement.getElementsByTagName("parameter");
      for (int i = 0; i < parameterNodes.getLength(); i++) {
        Element parameter = (Element) parameterNodes.item(i);
        String paramName = parameter.getAttribute("name");
        String paramValue = parameter.getTextContent().trim();
        parameters.put(paramName, paramValue);
      }
    }
    return parameters;
  }
}
