package oogasalad.userData;

import static org.junit.jupiter.api.Assertions.*;

import oogasalad.userData.parser.UserLevelDataParser;
import oogasalad.userData.records.UserLevelData;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Map;

/**
 * Test class for UserLevelDataParser.
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
public class UserLevelDataParserTest {
  private final UserLevelDataParser parser = new UserLevelDataParser();

  private Document newDocument() throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    return db.newDocument();
  }

  @Test
  void fromElement_ZeroStats_ReturnsEmptyMap() throws Exception {
    Document doc = newDocument();
    Element levelElem = doc.createElement("level");
    doc.appendChild(levelElem);
    Element nameElem = doc.createElement("levelName");
    nameElem.setTextContent("LevelZero");
    levelElem.appendChild(nameElem);
    Element lastElem = doc.createElement("lastPlayed");
    lastElem.setTextContent("2025-04-26");
    levelElem.appendChild(lastElem);
    Element statMapElem = doc.createElement("levelHighestStatMap");
    levelElem.appendChild(statMapElem);

    UserLevelData result = parser.fromElement(levelElem);
    assertEquals("LevelZero", result.levelName());
    assertEquals("2025-04-26", result.lastPlayed());
    assertTrue(result.levelHighestStatMap().isEmpty());
  }

  @Test
  void fromElement_OneStat_ReturnsSingleEntry() throws Exception {
    Document doc = newDocument();
    Element levelElem = doc.createElement("level");
    doc.appendChild(levelElem);
    Element nameElem = doc.createElement("levelName");
    nameElem.setTextContent("LevelOne");
    levelElem.appendChild(nameElem);
    Element lastElem = doc.createElement("lastPlayed");
    lastElem.setTextContent("2025-04-26");
    levelElem.appendChild(lastElem);
    Element statMapElem = doc.createElement("levelHighestStatMap");
    levelElem.appendChild(statMapElem);
    Element statElem = doc.createElement("stat");
    statElem.setAttribute("name", "score");
    statElem.setTextContent("100");
    statMapElem.appendChild(statElem);

    UserLevelData result = parser.fromElement(levelElem);
    Map<String,String> stats = result.levelHighestStatMap();
    assertEquals(1, stats.size());
    assertEquals("100", stats.get("score"));
  }

  @Test
  void fromElement_ManyStats_ReturnsAllEntries() throws Exception {
    Document doc = newDocument();
    Element levelElem = doc.createElement("level");
    doc.appendChild(levelElem);
    Element nameElem = doc.createElement("levelName");
    nameElem.setTextContent("LevelMany");
    levelElem.appendChild(nameElem);
    Element lastElem = doc.createElement("lastPlayed");
    lastElem.setTextContent("2025-04-26");
    levelElem.appendChild(lastElem);
    Element statMapElem = doc.createElement("levelHighestStatMap");
    levelElem.appendChild(statMapElem);
    for (int i = 1; i <= 3; i++) {
      Element stat = doc.createElement("stat");
      stat.setAttribute("name", "stat" + i);
      stat.setTextContent(String.valueOf(i * 10));
      statMapElem.appendChild(stat);
    }

    UserLevelData result = parser.fromElement(levelElem);
    Map<String,String> stats = result.levelHighestStatMap();
    assertEquals(3, stats.size());
    for (int i = 1; i <= 3; i++) {
      assertEquals(String.valueOf(i * 10), stats.get("stat" + i));
    }
  }

  @Test
  void fromElement_MissingLevelName_ReturnsNullName() throws Exception {
    Document doc = newDocument();
    Element levelElem = doc.createElement("level");
    doc.appendChild(levelElem);
    Element lastElem = doc.createElement("lastPlayed");
    lastElem.setTextContent("2025-04-26");
    levelElem.appendChild(lastElem);
    Element statMapElem = doc.createElement("levelHighestStatMap");
    levelElem.appendChild(statMapElem);

    UserLevelData result = parser.fromElement(levelElem);
    assertNull(result.levelName());
    assertEquals("2025-04-26", result.lastPlayed());
  }

  @Test
  void fromElement_MissingStatMap_ThrowsException() throws Exception {
    Document doc = newDocument();
    Element levelElem = doc.createElement("level");
    doc.appendChild(levelElem);
    Element nameElem = doc.createElement("levelName");
    nameElem.setTextContent("LevelX");
    levelElem.appendChild(nameElem);
    Element lastElem = doc.createElement("lastPlayed");
    lastElem.setTextContent("2025-04-26");
    levelElem.appendChild(lastElem);

    assertThrows(NullPointerException.class, () -> parser.fromElement(levelElem));
  }

  @Test
  void fromElement_NullElement_ThrowsException() {
    assertThrows(NullPointerException.class, () -> parser.fromElement(null));
  }

  @Test
  void fromElement_StressThousandStats_HandlesLargeInput() throws Exception {
    final int COUNT = 1000;
    Document doc = newDocument();
    Element levelElem = doc.createElement("level");
    doc.appendChild(levelElem);
    Element nameElem = doc.createElement("levelName");
    nameElem.setTextContent("LevelStress");
    levelElem.appendChild(nameElem);
    Element lastElem = doc.createElement("lastPlayed");
    lastElem.setTextContent("2025-04-26");
    levelElem.appendChild(lastElem);
    Element statMapElem = doc.createElement("levelHighestStatMap");
    levelElem.appendChild(statMapElem);
    for (int i = 0; i < COUNT; i++) {
      Element stat = doc.createElement("stat");
      stat.setAttribute("name", "s" + i);
      stat.setTextContent("v" + i);
      statMapElem.appendChild(stat);
    }

    UserLevelData result = parser.fromElement(levelElem);
    assertEquals(COUNT, result.levelHighestStatMap().size());
  }
}
