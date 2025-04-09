package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.OutcomeData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses event-related data from an XML document into a list of {@link EventData} records.
 * <p>
 * This parser extracts events found in the XML and creates {@code EventData} objects by
 * processing the event’s type, id, conditions, outcomes, and parameters.
 * </p>
 * <p>
 * Each event node is expected to have nested <code>conditions</code> with one or more
 * <code>conditionSet</code> elements (each with one or more <code>condition</code> children)
 * and <code>outcomes</code> with one or more <code>outcome</code> children. Conditions and outcomes
 * use dedicated <code>doubleParameters</code> and <code>stringParameters</code> sections to specify
 * their parameter data.
 * </p>
 *
 * Example usage:
 * <pre>
 *   Element root = ...; // obtain your XML root element
 *   EventDataParser parser = new EventDataParser();
 *   List&lt;EventData&gt; events = parser.getLevelEvents(root);
 * </pre>
 *
 * @author Billy
 */
public class EventDataParser {

  // A single instance of the PropertyParser to handle our property parsing.
  private final PropertyParser myPropertyParser = new PropertyParser();

  /**
   * Extracts the list of event data from the provided XML root element.
   *
   * @param root the XML {@link Element} that contains the event nodes.
   * @return a list of {@link EventData} objects parsed from the XML.
   * @throws BlueprintParseException if there is any issue parsing the property data.
   */
  public List<EventData> getLevelEvents(Element root) throws BlueprintParseException, EventParseException {
    NodeList eventNodes = root.getElementsByTagName("event");
    List<EventData> events = new ArrayList<>();
    for (int i = 0; i < eventNodes.getLength(); i++) {
      Element eventElement = (Element) eventNodes.item(i);
      events.add(parseEventNode(eventElement));
    }
    return events;
  }

  /**
   * Parses an individual event node into an {@link EventData} record.
   *
   * @param eventElement the XML {@link Element} representing an event.
   * @return the {@link EventData} record containing the parsed event data.
   * @throws BlueprintParseException if property parsing fails.
   */
  private EventData parseEventNode(Element eventElement) throws BlueprintParseException, EventParseException {
    String type = eventElement.getAttribute("type");
    String id = eventElement.getAttribute("id");

    // Parse conditions and outcomes using explicit loops.
    List<List<ConditionData>> conditions = parseConditions(eventElement);
    List<OutcomeData> outcomes = parseOutcomes(eventElement);

    return new EventData(type, id, conditions, outcomes);
  }

  /**
   * Parses all the condition sets from an event element.
   *
   * @param eventElement the XML {@link Element} representing an event.
   * @return a list of condition lists, each list representing a condition set.
   * @throws BlueprintParseException if condition parsing fails.
   */
  private List<List<ConditionData>> parseConditions(Element eventElement) throws BlueprintParseException, EventParseException{
    try {
      List<List<ConditionData>> conditions = new ArrayList<>();
      Element conditionsElement = getFirstElementByTagName(eventElement, "conditions");
        NodeList conditionSetNodes = conditionsElement.getElementsByTagName("conditionSet");
        for (int i = 0; i < conditionSetNodes.getLength(); i++) {
          Element conditionSetElement = (Element) conditionSetNodes.item(i);
          List<ConditionData> conditionList = new ArrayList<>();
          NodeList conditionNodes = conditionSetElement.getElementsByTagName("condition");
          for (int j = 0; j < conditionNodes.getLength(); j++) {
            Element conditionElement = (Element) conditionNodes.item(j);
            conditionList.add(parseCondition(conditionElement));
          }
          conditions.add(conditionList);
        }
      return conditions;
    } catch (NullPointerException e){
      throw new EventParseException(e.getMessage());
    }
  }

  /**
   * Parses all the outcomes from an event element.
   *
   * @param eventElement the XML {@link Element} representing an event.
   * @return a list of {@link OutcomeData} records.
   * @throws BlueprintParseException if outcome parsing fails.
   */
  private List<OutcomeData> parseOutcomes(Element eventElement) throws BlueprintParseException, EventParseException {
    try {
      List<OutcomeData> outcomes = new ArrayList<>();
      Element outcomesElement = getFirstElementByTagName(eventElement, "outcomes");
      if (outcomesElement != null) {
        NodeList outcomeNodes = outcomesElement.getElementsByTagName("outcome");
        for (int i = 0; i < outcomeNodes.getLength(); i++) {
          Element outcomeElement = (Element) outcomeNodes.item(i);
          outcomes.add(parseOutcome(outcomeElement));
        }
      }
      return outcomes;
    } catch (NullPointerException e){
      throw new EventParseException(e.getMessage());
    }
  }

  /**
   * Parses a condition element into a {@link ConditionData} record.
   *
   * @param conditionElement the XML {@link Element} representing a condition.
   * @return the parsed {@link ConditionData} record.
   * @throws BlueprintParseException if property parsing fails.
   */
  private ConditionData parseCondition(Element conditionElement) throws BlueprintParseException {
    String name = conditionElement.getAttribute("name");
    Map<String, Double> doubleProperties = extractDoubleProperties(conditionElement);
    Map<String, String> stringProperties = extractStringProperties(conditionElement);
    return new ConditionData(name, stringProperties, doubleProperties);
  }

  /**
   * Parses an outcome element into an {@link OutcomeData} record.
   *
   * @param outcomeElement the XML {@link Element} representing an outcome.
   * @return the parsed {@link OutcomeData} record.
   * @throws BlueprintParseException if property parsing fails.
   */
  private OutcomeData parseOutcome(Element outcomeElement) throws BlueprintParseException {
    // In the new XML, outcome uses the "type" attribute to denote its action.
    String outcomeName = outcomeElement.getAttribute("type");
    if (outcomeName == null || outcomeName.isEmpty()) {
      outcomeName = outcomeElement.getAttribute("name");
    }
    Map<String, Double> doubleProperties = extractDoubleProperties(outcomeElement);
    Map<String, String> stringProperties = extractStringProperties(outcomeElement);
    return new OutcomeData(outcomeName, stringProperties, doubleProperties);
  }

  /**
   * Extracts double properties from an element by finding its "doubleParameters" child.
   *
   * @param element the element from which to extract double properties.
   * @return a map of double properties; an empty map if none are found.
   * @throws BlueprintParseException if property parsing fails.
   */
  private Map<String, Double> extractDoubleProperties(Element element) throws BlueprintParseException {
    Element doubleParams = getFirstElementByTagName(element, "doubleParameters");
    if (doubleParams != null) {
      return myPropertyParser.parseDoubleProperties(doubleParams, "doubleParameters", "parameter");
    }
    return new HashMap<>();
  }

  /**
   * Extracts string properties from an element by finding its "stringParameters" child.
   *
   * @param element the element from which to extract string properties.
   * @return a map of string properties; an empty map if none are found.
   * @throws BlueprintParseException if property parsing fails.
   */
  private Map<String, String> extractStringProperties(Element element) throws BlueprintParseException, EventParseException{
    Element stringParams = getFirstElementByTagName(element, "stringParameters");
    if (stringParams != null) {
      return myPropertyParser.parseStringProperties(stringParams, "stringParameters", "parameter");
    }
    return new HashMap<>();
  }

  /**
   * Helper method to get the first direct child element with the given tag name.
   *
   * @param parent  the parent element.
   * @param tagName the tag name to search for.
   * @return the first matching child element, or null if not found.
   */
  private Element getFirstElementByTagName(Element parent, String tagName) {
    NodeList nodeList = parent.getElementsByTagName(tagName);
    return (nodeList.getLength() > 0) ? (Element) nodeList.item(0) : null;
  }
}
