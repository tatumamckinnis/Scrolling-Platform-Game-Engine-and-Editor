package oogasalad.fileParsertests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.fileparser.EventDataParser;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.OutcomeData;

/**
 * JUnit tests for the {@link EventDataParser} class.
 *
 * <p>
 * Test methods follow the [MethodName_StateUnderTest_ExpectedBehavior] format. The tests cover
 * scenarios including valid XML input, absence of event nodes, and invalid property data.
 * </p>
 *
 * @author Billy McCune
 */
public class EventParserTest {

  /**
   * Helper method to convert an XML string into a DOM Element.
   *
   * @param xml the XML as a string.
   * @return the root {@link Element} of the parsed XML Document.
   * @throws Exception if an error occurs during parsing.
   */
  private Element getRootElement(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    doc.getDocumentElement().normalize();
    return doc.getDocumentElement();
  }

  /**
   * Helper method that builds an XML Document from a string.
   *
   * @param xml the XML content as a string.
   * @return the root element of the parsed Document.
   * @throws Exception if any parsing error occurs.
   */
  private Element buildRootElement(String xml) throws Exception {
    DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
    ByteArrayInputStream input =
        new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    Document doc = builder.parse(input);
    return doc.getDocumentElement();
  }

  /**
   * [getLevelEvents_ValidInput_ReturnsCorrectEventData]
   * <p>
   * Tests that a valid XML input containing one event is parsed correctly. The event contains one
   * condition set (with one condition) and one outcome. Both the condition and outcome include one
   * double property and one string property.
   * </p>
   *
   * @throws Exception if any parsing error occurs.
   */
  @Test
  void getLevelEvents_ValidInput_ReturnsCorrectEventData() throws Exception {
    // Create an XML input with one event. The event has one condition with a double property "threshold"
    // and a string property "desc", and one outcome with a double property "factor" and a string property "result".
    String xml = """
        <root>
          <event type="trigger" id="evt1">
            <conditions>
              <conditionSet>
                <condition name="cond1">
                  <doubleParameters>
                    <parameter name="threshold" value="5.0"/>
                  </doubleParameters>
                  <stringParameters>
                    <parameter name="desc" value="Test condition"/>
                  </stringParameters>
                </condition>
              </conditionSet>
            </conditions>
            <outcomes>
              <outcome name="outcome1">
                <doubleParameters>
                  <parameter name="factor" value="2.5"/>
                </doubleParameters>
                <stringParameters>
                  <parameter name="result" value="Success"/>
                </stringParameters>
              </outcome>
            </outcomes>
          </event>
        </root>
        """;

    Element root = buildRootElement(xml);
    EventDataParser parser = new EventDataParser();
    List<EventData> events = parser.getLevelEvents(root);

    // Verify that one event was parsed.
    assertEquals(1, events.size(), "There should be one event parsed.");

    EventData event = events.get(0);
    assertEquals("trigger", event.type(), "Event type should match.");
    assertEquals("evt1", event.eventId(), "Event id should match.");

    // Verify conditions.
    List<List<ConditionData>> conditionSets = event.conditions();
    assertEquals(1, conditionSets.size(), "There should be one condition set.");
    List<ConditionData> conditions = conditionSets.get(0);
    assertEquals(1, conditions.size(), "There should be one condition in the set.");

    ConditionData condition = conditions.get(0);
    assertEquals("cond1", condition.name(), "Condition name should match.");

    // Check double parameters in condition.
    Map<String, Double> condDoubleProps = condition.doubleProperties();
    assertEquals(1, condDoubleProps.size(), "Expected one double property in condition.");
    assertEquals(5.0, condDoubleProps.get("threshold"), 0.001, "Threshold property should be 5.0.");

    // Check string parameters in condition.
    Map<String, String> condStringProps = condition.stringProperties();
    assertEquals(1, condStringProps.size(), "Expected one string property in condition.");
    assertEquals("Test condition", condStringProps.get("desc"), "Description property should match.");

    // Verify outcomes.
    List<OutcomeData> outcomes = event.outcomes();
    assertEquals(1, outcomes.size(), "There should be one outcome.");

    OutcomeData outcome = outcomes.get(0);
    assertEquals("outcome1", outcome.name(), "Outcome name should match.");

    // Check double parameters in outcome.
    Map<String, Double> outDoubleProps = outcome.doubleProperties();
    assertEquals(1, outDoubleProps.size(), "Expected one double property in outcome.");
    assertEquals(2.5, outDoubleProps.get("factor"), 0.001, "Factor property should be 2.5.");

    // Check string parameters in outcome.
    Map<String, String> outStringProps = outcome.stringProperties();
    assertEquals(1, outStringProps.size(), "Expected one string property in outcome.");
    assertEquals("Success", outStringProps.get("result"), "Outcome result should match.");
  }

  /**
   * Test that when no event nodes are present in the XML, getLevelEvents returns an empty list.
   *
   * @throws Exception if an error occurs during parsing.
   */
  @Test
  void getLevelEvents_NoEventNodes_ReturnsEmptyList() throws Exception {
    String xml = "<root></root>";
    Element root = getRootElement(xml);
    EventDataParser parser = new EventDataParser();
    List<EventData> events = parser.getLevelEvents(root);
    assertTrue(events.isEmpty(), "Expected no events to be parsed");
  }

  /**
   * Test that when a condition contains an invalid double property (non-numeric value),
   * a {@link PropertyParsingException} is thrown.
   *
   * @throws Exception if an error occurs during parsing.
   */
  @Test
  void getLevelEvents_InvalidDoubleProperty_ThrowsPropertyParsingException() throws Exception {
    String xml = """
        <root>
          <event type="collision" id="evt1">
            <conditions>
              <conditionSet>
                <condition name="cond1">
                  <doubleParameters>
                    <parameter name="threshold" value="not_a_double"/>
                  </doubleParameters>
                  <stringParameters>
                    <parameter name="message" value="ok"/>
                  </stringParameters>
                </condition>
              </conditionSet>
            </conditions>
          </event>
        </root>
        """;
    Element root = getRootElement(xml);
    EventDataParser parser = new EventDataParser();
    assertThrows(PropertyParsingException.class, () -> parser.getLevelEvents(root),
        "Expected a PropertyParsingException due to an invalid double value");
  }
}
