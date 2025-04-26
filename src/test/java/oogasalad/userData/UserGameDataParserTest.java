package oogasalad.userData;

import static org.junit.jupiter.api.Assertions.*;

import oogasalad.userData.parser.UserGameDataParser;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Map;
import oogasalad.userData.records.UserGameData;
import oogasalad.userData.records.UserLevelData;

/**
 * Test class for UserGameDataParser.
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
public class UserGameDataParserTest {
  private final UserGameDataParser parser = new UserGameDataParser();

  private Document newDocument() throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    return db.newDocument();
  }

  @Test
  void fromElement_ZeroStatsZeroLevels_ReturnsEmptyMaps() throws Exception {
    Document doc = newDocument();
    Element ugdElem = doc.createElement("userGameData");
    doc.appendChild(ugdElem);
    Element gameNameElem = doc.createElement("gameName");
    gameNameElem.setTextContent("GameZero");
    ugdElem.appendChild(gameNameElem);
    Element lastPlayedElem = doc.createElement("lastPlayed");
    lastPlayedElem.setTextContent("2025-04-26");
    ugdElem.appendChild(lastPlayedElem);
    Element highMapElem = doc.createElement("playerHighestGameStatMap");
    ugdElem.appendChild(highMapElem);
    Element lvlMapElem = doc.createElement("playerLevelStatMap");
    ugdElem.appendChild(lvlMapElem);

    UserGameData result = parser.fromElement(ugdElem);
    assertEquals("GameZero", result.gameName());
    assertEquals("2025-04-26", result.lastPlayed());
    assertTrue(result.playerHighestGameStatMap().isEmpty());
    assertTrue(result.playerLevelStatMap().isEmpty());
  }

  @Test
  void fromElement_OneStatOneLevel_ReturnsSingleEntryAndLevel() throws Exception {
    Document doc = newDocument();
    Element ugdElem = doc.createElement("userGameData");
    doc.appendChild(ugdElem);
    Element gameNameElem = doc.createElement("gameName");
    gameNameElem.setTextContent("GameOne");
    ugdElem.appendChild(gameNameElem);
    Element lastPlayedElem = doc.createElement("lastPlayed");
    lastPlayedElem.setTextContent("2025-04-26");
    ugdElem.appendChild(lastPlayedElem);
    Element highMapElem = doc.createElement("playerHighestGameStatMap");
    ugdElem.appendChild(highMapElem);
    Element statElem = doc.createElement("stat");
    statElem.setAttribute("name", "wins");
    statElem.setTextContent("5");
    highMapElem.appendChild(statElem);
    Element lvlMapElem = doc.createElement("playerLevelStatMap");
    ugdElem.appendChild(lvlMapElem);
    Element levelElem = doc.createElement("level");
    lvlMapElem.appendChild(levelElem);
    Element lvlNameElem = doc.createElement("levelName");
    lvlNameElem.setTextContent("Level1");
    levelElem.appendChild(lvlNameElem);
    Element lvlLastElem = doc.createElement("lastPlayed");
    lvlLastElem.setTextContent("2025-04-25");
    levelElem.appendChild(lvlLastElem);
    Element lvlStatMap = doc.createElement("levelHighestStatMap");
    levelElem.appendChild(lvlStatMap);

    UserGameData result = parser.fromElement(ugdElem);
    Map<String,String> highStats = result.playerHighestGameStatMap();
    assertEquals(1, highStats.size());
    assertEquals("5", highStats.get("wins"));
    Map<String,UserLevelData> levels = result.playerLevelStatMap();
    assertEquals(1, levels.size());
    UserLevelData lvlData = levels.get("Level1");
    assertNotNull(lvlData);
    assertEquals("2025-04-25", lvlData.lastPlayed());
  }

  @Test
  void fromElement_ManyStatsManyLevels_ReturnsAllEntries() throws Exception {
    final int STAT_COUNT = 3;
    final int LEVEL_COUNT = 2;
    Document doc = newDocument();
    Element ugdElem = doc.createElement("userGameData");
    doc.appendChild(ugdElem);
    Element gameNameElem = doc.createElement("gameName");
    gameNameElem.setTextContent("GameMany");
    ugdElem.appendChild(gameNameElem);
    Element lastPlayedElem = doc.createElement("lastPlayed");
    lastPlayedElem.setTextContent("2025-04-26");
    ugdElem.appendChild(lastPlayedElem);
    Element highMapElem = doc.createElement("playerHighestGameStatMap");
    ugdElem.appendChild(highMapElem);
    for (int i = 1; i <= STAT_COUNT; i++) {
      Element stat = doc.createElement("stat");
      stat.setAttribute("name", "stat" + i);
      stat.setTextContent(String.valueOf(i));
      highMapElem.appendChild(stat);
    }
    Element lvlMapElem = doc.createElement("playerLevelStatMap");
    ugdElem.appendChild(lvlMapElem);
    for (int j = 1; j <= LEVEL_COUNT; j++) {
      Element levelElem = doc.createElement("level");
      lvlMapElem.appendChild(levelElem);
      Element lvlNameElem = doc.createElement("levelName");
      lvlNameElem.setTextContent("L" + j);
      levelElem.appendChild(lvlNameElem);
      Element lvlLastElem = doc.createElement("lastPlayed");
      lvlLastElem.setTextContent("2025-04-2" + j);
      levelElem.appendChild(lvlLastElem);
      Element lvlStatMap = doc.createElement("levelHighestStatMap");
      levelElem.appendChild(lvlStatMap);
    }

    UserGameData result = parser.fromElement(ugdElem);
    assertEquals(STAT_COUNT, result.playerHighestGameStatMap().size());
    assertEquals(LEVEL_COUNT, result.playerLevelStatMap().size());
  }

  @Test
  void fromElement_MissingGameName_ReturnsNullName() throws Exception {
    Document doc = newDocument();
    Element ugdElem = doc.createElement("userGameData");
    doc.appendChild(ugdElem);
    Element lastPlayedElem = doc.createElement("lastPlayed");
    lastPlayedElem.setTextContent("2025-04-26");
    ugdElem.appendChild(lastPlayedElem);
    Element highMapElem = doc.createElement("playerHighestGameStatMap");
    ugdElem.appendChild(highMapElem);
    Element lvlMapElem = doc.createElement("playerLevelStatMap");
    ugdElem.appendChild(lvlMapElem);

    UserGameData result = parser.fromElement(ugdElem);
    assertNull(result.gameName());
    assertEquals("2025-04-26", result.lastPlayed());
  }

  @Test
  void fromElement_MissingHighMap_ThrowsException() throws Exception {
    Document doc = newDocument();
    Element ugdElem = doc.createElement("userGameData");
    doc.appendChild(ugdElem);
    Element gameNameElem = doc.createElement("gameName");
    gameNameElem.setTextContent("GameX");
    ugdElem.appendChild(gameNameElem);
    Element lastPlayedElem = doc.createElement("lastPlayed");
    lastPlayedElem.setTextContent("2025-04-26");
    ugdElem.appendChild(lastPlayedElem);
    Element lvlMapElem = doc.createElement("playerLevelStatMap");
    ugdElem.appendChild(lvlMapElem);

    assertThrows(NullPointerException.class, () -> parser.fromElement(ugdElem));
  }

  @Test
  void fromElement_NullElement_ThrowsException() {
    assertThrows(NullPointerException.class, () -> parser.fromElement(null));
  }

  @Test
  void fromElement_StressManyStatsAndLevels_HandlesLargeInput() throws Exception {
    final int N = 100;
    Document doc = newDocument();
    Element ugdElem = doc.createElement("userGameData");
    doc.appendChild(ugdElem);
    Element gameNameElem = doc.createElement("gameName");
    gameNameElem.setTextContent("GameStress");
    ugdElem.appendChild(gameNameElem);
    Element lastPlayedElem = doc.createElement("lastPlayed");
    lastPlayedElem.setTextContent("2025-04-26");
    ugdElem.appendChild(lastPlayedElem);
    Element highMapElem = doc.createElement("playerHighestGameStatMap");
    ugdElem.appendChild(highMapElem);
    Element lvlMapElem = doc.createElement("playerLevelStatMap");
    ugdElem.appendChild(lvlMapElem);
    for (int i = 0; i < N; i++) {
      Element stat = doc.createElement("stat");
      stat.setAttribute("name", "s" + i);
      stat.setTextContent("v" + i);
      highMapElem.appendChild(stat);
      Element levelElem = doc.createElement("level");
      lvlMapElem.appendChild(levelElem);
      Element lvlName = doc.createElement("levelName");
      lvlName.setTextContent("L" + i);
      levelElem.appendChild(lvlName);
      Element lvlLast = doc.createElement("lastPlayed");
      lvlLast.setTextContent("date");
      levelElem.appendChild(lvlLast);
      Element lvlStats = doc.createElement("levelHighestStatMap");
      levelElem.appendChild(lvlStats);
    }

    UserGameData result = parser.fromElement(ugdElem);
    assertEquals(N, result.playerHighestGameStatMap().size());
    assertEquals(N, result.playerLevelStatMap().size());
  }
}
