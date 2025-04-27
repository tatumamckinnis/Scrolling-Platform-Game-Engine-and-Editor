package oogasalad.userData;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import oogasalad.exceptions.UserDataParseException;
import oogasalad.userData.UserDataApiDefault;
import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;
import oogasalad.userData.records.UserLevelData;

/**
 * Test class for UserDataApiDefault.
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
public class UserDataApiDefaultTest {
  private static final Path USER_DATA_DIR = Paths.get("data", "userData");
  private UserDataApiDefault api;

  @BeforeEach
  void setup() throws IOException {
    // Clean and recreate the data/userData directory
    if (Files.exists(USER_DATA_DIR)) {
      Files.walk(USER_DATA_DIR)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
    Files.createDirectories(USER_DATA_DIR);
    api = new UserDataApiDefault();
  }

  @Test
  void parseUserData_ZeroGames_ReturnsEmptyList() throws Exception {
    // Zero games scenario
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
    Path file = USER_DATA_DIR.resolve("u.xml");
    Files.writeString(file, xml);

    UserData data = api.parseUserData("u", "p");
    assertEquals("u", data.username());
    assertTrue(data.userGameData().isEmpty());
  }

  @Test
  void parseUserData_InvalidPassword_ThrowsUserDataParseException() throws Exception {
    // Boundary: wrong password
    String xml = "<user><username>u</username><displayName>d</displayName>" +
        "<email>e</email><password>correct</password>" +
        "<language>l</language><bio>b</bio>" +
        "<userImage>img.png</userImage><userGameDataList></userGameDataList></user>";
    Path file = USER_DATA_DIR.resolve("u.xml");
    Files.writeString(file, xml);

    assertThrows(UserDataParseException.class, () -> api.parseUserData("u", "wrong"));
  }

  @Test
  void parseUserData_FileMissing_ThrowsIOException() {
    // Invalid: file does not exist
    assertThrows(IOException.class, () -> api.parseUserData("noUser", "pw"));
  }

  @Test
  void writeNewUserData_OneGame_CreatesFileAndParsesBack() throws Exception {
    // One game, one level scenario
    UserLevelData lvl = new UserLevelData("L1", "t1", new HashMap<>());
    UserGameData game = new UserGameData("G1", "tG", new HashMap<>(), Map.of("L1", lvl));
    UserData user = new UserData("x", "dx", "ex", "pwx", "lx", "bx", new File("imgx.png"), null, List.of(game));

    api.writeNewUserData(user);
    Path file = USER_DATA_DIR.resolve("x.xml");
    assertTrue(Files.exists(file));

    UserData reloaded = api.parseUserData("x", "pwx");
    assertEquals(1, reloaded.userGameData().size());
    assertEquals("G1", reloaded.userGameData().get(0).gameName());
  }

  @Test
  void writeCurrentUserData_NoLoad_ThrowsIllegalStateException() {
    // Exception: no data loaded
    assertThrows(IllegalStateException.class, () -> api.writeCurrentUserData());
  }

  @Test
  void updatePlayerLevelStats_NoLoad_ThrowsIllegalStateException() {
    // Exception: no data loaded
    assertThrows(IllegalStateException.class,
        () -> api.updatePlayerLevelStats("u", "g", "l", new HashMap<>()));
  }

  @Test
  void updatePlayerLevelStats_NewEntities_PersistsChanges() throws Exception {
    // One game, one level update scenario
    String xml = "<user><username>u</username><displayName>d</displayName>" +
        "<email>e</email><password>pw</password><language>l</language><bio>b</bio>" +
        "<userImage>i.png</userImage><userGameDataList></userGameDataList></user>";
    Path file = USER_DATA_DIR.resolve("u.xml");
    Files.writeString(file, xml);

    api.parseUserData("u", "pw");
    Map<String,String> stats = Map.of("score", "100");
    api.updatePlayerLevelStats("u", "GameA", "LevelA", stats);

    UserData updated = api.parseUserData("u", "pw");
    assertTrue(updated.userGameData().stream()
        .anyMatch(g -> g.playerHighestGameStatMap().containsKey("score")
            && g.playerLevelStatMap().containsKey("LevelA")));
  }

  @Test
  void getCurrentUsername_DefaultAndAfterParse() throws Exception {
    // Zero state
    assertEquals("defaultUser", api.getCurrentUsername());

    // After parse
    String xml = "<user><username>u2</username><displayName>d2</displayName>" +
        "<email>e2</email><password>pw2</password><language>l</language><bio>b2</bio>" +
        "<userImage>i2.png</userImage><userGameDataList></userGameDataList></user>";
    Path file = USER_DATA_DIR.resolve("u2.xml");
    Files.writeString(file, xml);
    api.parseUserData("u2", "pw2");

    assertEquals("u2", api.getCurrentUsername());
  }

  @Test
  void getUserDataFilePath_DefaultAndAfterParse() throws Exception {
    // Default
    assertEquals("No file loaded", api.getUserDataFilePath());

    // After parse
    String xml = "<user><username>u3</username><displayName>d3</displayName>" +
        "<email>e3</email><password>pw3</password><language>l</language><bio>b3</bio>" +
        "<userImage>i3.png</userImage><userGameDataList></userGameDataList></user>";
    Path file = USER_DATA_DIR.resolve("u3.xml");
    Files.writeString(file, xml);
    api.parseUserData("u3", "pw3");

    assertTrue(api.getUserDataFilePath().endsWith("data/userData/u3.xml"));
  }

  @Test
  void saveLoadStressMultipleUsers_HandlesAll() throws Exception {
    // Stress: multiple users
    for (int i = 0; i < 10; i++) {
      String uname = "user" + i;
      String pass = "pw" + i;
      UserData u = new UserData(uname, "d","e",pass,"l","b",new File("i.png"),null,new ArrayList<>());
      api.writeNewUserData(u);
      UserData out = api.parseUserData(uname, pass);
      assertEquals(uname, out.username());
    }
  }
}