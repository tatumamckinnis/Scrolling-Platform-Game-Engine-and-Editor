package oogasalad.userData;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import oogasalad.exceptions.UserDataParseException;
import oogasalad.exceptions.UserDataWriteException;
import oogasalad.userData.parser.UserDataParser;
import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;
import oogasalad.userData.records.UserLevelData;
import oogasalad.userData.writer.UserDataWriter;

public class UserDataApiDefault implements UserApi {

  private UserDataParser myUserDataParser;
  private UserDataWriter myUserDataWriter;
  private UserData myUserData;
  private File dataFile;

  @Override
  public UserData parseUserData(String username, String password)
      throws IOException, ParserConfigurationException, SAXException, UserDataParseException {

    myUserDataParser = new UserDataParser();
    // locate the file
    this.dataFile = new File("data/userData/" + username + ".xml");

    // parse into the field
    this.myUserData = myUserDataParser.getUserData(dataFile);

    // verify credentials
    if (isCorrectPassword(myUserData, password)) {
      return myUserData;
    } else {
      throw new UserDataParseException("Wrong Username or Password");
    }
  }

  @Override
  public void writeNewUserData(UserData userData)
      throws XMLStreamException, IOException, UserDataWriteException {
    // for new users, just write out the data (creates or overwrites)
    writeUserData(userData);
  }

  @Override
  public void writeUserData(UserData userData)
      throws UserDataWriteException, XMLStreamException, IOException {
    if (myUserDataWriter == null) {
      myUserDataWriter = new UserDataWriter();
    }
    myUserDataWriter.writeUsersData(userData);
  }

  @Override
  public void writeCurrentUserData()
      throws UserDataWriteException, XMLStreamException, IOException {
    if (myUserData == null) {
      throw new IllegalStateException("No user data loaded to write");
    }
    writeUserData(myUserData);
  }

  @Override
  public void updatePlayerLevelStats(String playerName,
      String gameName,
      String levelName,
      Map<String, String> newLevelStats) {
    // ensure we have parsed data
    if (this.myUserData == null || this.dataFile == null) {
      throw new IllegalStateException("User data not loaded. Call parseUserData(...) first.");
    }

    // 1) Find or create the UserGameData
    UserGameData targetGame = null;
    for (UserGameData ugd : myUserData.userGameData()) {
      if (ugd.gameName().equals(gameName)) {
        targetGame = ugd;
        break;
      }
    }
    if (targetGame == null) {
      targetGame = makeNewGameData(gameName);
      myUserData.userGameData().add(targetGame);
    }

    // 2) Find or create the UserLevelData
    UserLevelData targetLevel = targetGame.playerLevelStatMap().get(levelName);
    if (targetLevel == null) {
      targetLevel = makeNewLevelData(levelName);
      targetGame.playerLevelStatMap().put(levelName, targetLevel);
    }

    // 3) Merge in the new stats with numeric comparison
    Map<String, String> merged = updatePlayerStatMap(
        newLevelStats,
        targetLevel.levelHighestStatMap()
    );
    // update lastPlayed timestamps
    targetLevel = new UserLevelData(
        targetLevel.levelName(),
        new Date().toString(),
        merged
    );
    targetGame.playerLevelStatMap().put(levelName, targetLevel);

    // also update game lastPlayed
    targetGame = new UserGameData(
        targetGame.gameName(),
        new Date().toString(),
        targetGame.playerHighestGameStatMap(),
        targetGame.playerLevelStatMap()
    );
    myUserData.userGameData().removeIf(g -> g.gameName().equals(gameName));
    myUserData.userGameData().add(targetGame);

    // 4) Persist back to XML
    try {
      writeUserData(myUserData);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save updated user data", e);
    }
  }

  private boolean isCorrectPassword(UserData data, String password) {
    return data.password().equals(password);
  }

  private UserGameData makeNewGameData(String gameName) {
    String now = new Date().toString();
    return new UserGameData(
        gameName,
        now,
        new HashMap<>(),
        new HashMap<>()
    );
  }

  private UserLevelData makeNewLevelData(String levelName) {
    String now = new Date().toString();
    return new UserLevelData(
        levelName,
        now,
        new HashMap<>()
    );
  }

  /**
   * For each entry in newStats:
   * - if oldStats contains the key and both old/new values parse as doubles,
   *   only overwrite when newValue > oldValue
   * - otherwise (non-numeric or key not present) just put the new value
   */
  private Map<String, String> updatePlayerStatMap(
      Map<String, String> newStats,
      Map<String, String> oldStats) {

    for (Map.Entry<String, String> e : newStats.entrySet()) {
      String key = e.getKey();
      String newVal = e.getValue();

      if (oldStats.containsKey(key)) {
        String oldVal = oldStats.get(key);
        try {
          double newD = Double.parseDouble(newVal);
          double oldD = Double.parseDouble(oldVal);
          if (newD > oldD) {
            oldStats.put(key, newVal);
          }
        } catch (NumberFormatException ex) {
          // non-numeric: overwrite
          oldStats.put(key, newVal);
        }
      } else {
        // brand new stat key: add it
        oldStats.put(key, newVal);
      }
    }
    return oldStats;
  }

  /**
   * Returns the username of the currently active user.
   * @return The current username
   */
  public String getCurrentUsername() {
    // Return the username of currently parsed user data
    return myUserData != null ? myUserData.username() : "defaultUser";
  }

  /**
   * Returns the path to the current user data file.
   * @return the absolute path to the user data file
   */
  public String getUserDataFilePath() {
    return dataFile != null ? dataFile.getAbsolutePath() : "No file loaded";
  }
}
