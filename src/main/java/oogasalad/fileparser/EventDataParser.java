package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.fileparser.records.EventData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 *
 *
 * @author Billy McCune
 */
public class EventDataParser {
  List<EventData> events;

  public List<EventData> getLevelEvents(Element root) {
    NodeList eventNodes = root.getElementsByTagName("event");
    events = new ArrayList<>();
    for (int i = 0; i < eventNodes.getLength(); i++) {
      Element eventElement = (Element) eventNodes.item(i);
      events.add(parseEventNode(eventElement));
    }
    return events;
  }

  private EventData parseEventNode(Element eventElement) {
    String name = eventElement.getAttribute("name");
    String type = eventElement.getAttribute("type");
    String id = eventElement.getAttribute("id");

    List<List<String>> conditions = parseEventConditions(
        (Element) eventElement.getElementsByTagName("conditions").item(0));

    List<String> outcomes = parseOutcomes(
        (Element) eventElement.getElementsByTagName("outcomes").item(0));

    Map<String, String> parameters = parseParameters(
        (Element) eventElement.getElementsByTagName("parameters").item(0));

    return new EventData(name, type, id, conditions, outcomes, parameters);
  }

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
