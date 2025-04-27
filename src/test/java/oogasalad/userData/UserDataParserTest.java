package oogasalad.userData;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


import oogasalad.userData.parser.UserDataParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;

/**
 * Test class for UserDataParser.
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
public class UserDataParserTest {
  private final UserDataParser parser = new UserDataParser();

  @Test
  void getUserData_ZeroGames_ReturnsEmptyGameList(@TempDir Path tempDir) throws Exception {
    String xml = "<user>" +
        "<username>u</username>" +
        "<displayName>d</displayName>" +
        "<email>e</email>" +
        "<password>p</password>" +
        "<language>l</language>" +
        "<bio>b</bio>" +
        "<userImage>img.png</userImage>" +
        "<userGameDataList></userGameDataList>" +
        "</user>";
    Path xmlFile = tempDir.resolve("user0.xml");
    Files.write(xmlFile, xml.getBytes(StandardCharsets.UTF_8));

    UserData result = parser.getUserData(xmlFile.toFile());
    assertEquals("u", result.username());
    assertTrue(result.userGameData().isEmpty());
  }

  @Test
  void getUserData_OneGame_ReturnsSingleGame(@TempDir Path tempDir) throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("<user>");
    sb.append("<username>u1</username>");
    sb.append("<displayName>d1</displayName>");
    sb.append("<email>e1</email>");
    sb.append("<password>p1</password>");
    sb.append("<language>l1</language>");
    sb.append("<bio>b1</bio>");
    sb.append("<userImage>img1.png</userImage>");
    sb.append("<userGameDataList>");
    sb.append("<userGameData>");
    sb.append("<gameName>g1</gameName>");
    sb.append("<lastPlayed>2025-04-26</lastPlayed>");
    sb.append("<playerHighestGameStatMap></playerHighestGameStatMap>");
    sb.append("<playerLevelStatMap></playerLevelStatMap>");
    sb.append("</userGameData>");
    sb.append("</userGameDataList>");
    sb.append("</user>");
    Path xmlFile = tempDir.resolve("user1.xml");
    Files.write(xmlFile, sb.toString().getBytes(StandardCharsets.UTF_8));

    UserData result = parser.getUserData(xmlFile.toFile());
    List<UserGameData> games = result.userGameData();
    assertEquals(1, games.size());
    assertEquals("g1", games.get(0).gameName());
  }

  @Test
  void getUserData_ManyGames_ReturnsAllGames(@TempDir Path tempDir) throws Exception {
    final int COUNT = 5;
    StringBuilder sb = new StringBuilder();
    sb.append("<user><username>uM</username><displayName>dM</displayName>");
    sb.append("<email>eM</email><password>pM</password><language>lM</language><bio>bM</bio>");
    sb.append("<userImage>imgM.png</userImage><userGameDataList>");
    for (int i = 0; i < COUNT; i++) {
      sb.append("<userGameData><gameName>g"+i+"</gameName><lastPlayed>lp"+i+"</lastPlayed>");
      sb.append("<playerHighestGameStatMap></playerHighestGameStatMap>");
      sb.append("<playerLevelStatMap></playerLevelStatMap></userGameData>");
    }
    sb.append("</userGameDataList></user>");
    Path xmlFile = tempDir.resolve("userM.xml");
    Files.write(xmlFile, sb.toString().getBytes(StandardCharsets.UTF_8));

    UserData result = parser.getUserData(xmlFile.toFile());
    assertEquals(COUNT, result.userGameData().size());
  }

  @Test
  void getUserData_MissingUserElement_ThrowsIOException(@TempDir Path tempDir) throws Exception {
    String xml = "<root></root>";
    Path xmlFile = tempDir.resolve("bad.xml");
    Files.write(xmlFile, xml.getBytes(StandardCharsets.UTF_8));

    assertThrows(IOException.class, () -> parser.getUserData(xmlFile.toFile()));
  }

  @Test
  void getUserData_InvalidXML_ThrowsSAXException(@TempDir Path tempDir) throws Exception {
    String xml = "<user><username>u</username>"; // no closing tags
    Path xmlFile = tempDir.resolve("inv.xml");
    Files.write(xmlFile, xml.getBytes(StandardCharsets.UTF_8));

    assertThrows(SAXException.class, () -> parser.getUserData(xmlFile.toFile()));
  }

  @Test
  void getUserData_MissingGameDataList_ThrowsException(@TempDir Path tempDir) throws Exception {
    String xml = "<user><username>u</username><displayName>d</displayName>" +
        "<email>e</email><password>p</password><language>l</language><bio>b</bio>" +
        "<userImage>img.png</userImage></user>";
    Path xmlFile = tempDir.resolve("nogame.xml");
    Files.write(xmlFile, xml.getBytes(StandardCharsets.UTF_8));

    assertThrows(NullPointerException.class, () -> parser.getUserData(xmlFile.toFile()));
  }

  @Test
  void getUserData_StressManyEntries_HandlesLargeInput(@TempDir Path tempDir) throws Exception {
    final int N = 50;
    StringBuilder sb = new StringBuilder();
    sb.append("<user><username>uS</username><displayName>dS</displayName>");
    sb.append("<email>eS</email><password>pS</password><language>lS</language><bio>bS</bio>");
    sb.append("<userImage>iS.png</userImage><userGameDataList>");
    for (int i = 0; i < N; i++) {
      sb.append("<userGameData><gameName>g"+i+"</gameName><lastPlayed>lp"+i+"</lastPlayed>");
      sb.append("<playerHighestGameStatMap></playerHighestGameStatMap>");
      sb.append("<playerLevelStatMap></playerLevelStatMap></userGameData>");
    }
    sb.append("</userGameDataList></user>");
    Path xmlFile = tempDir.resolve("userS.xml");
    Files.write(xmlFile, sb.toString().getBytes(StandardCharsets.UTF_8));

    UserData result = parser.getUserData(xmlFile.toFile());
    assertEquals(N, result.userGameData().size());
  }
}

