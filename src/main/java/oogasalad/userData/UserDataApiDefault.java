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

/**
 * Default implementation of UserApi using XML parsers and writers.
 * Maintains an in-memory UserData object and its backing file.
 *
 * @author Billy McCune
 */
public class UserDataApiDefault implements UserApi {

  private UserDataParser myUserDataParser;
  private UserDataWriter myUserDataWriter;
  private UserData myUserData;
  private File dataFile;

  /**
   * Parses and validates user credentials from XML.
   *
   * @param username the username of the user
   * @param password the plaintext password to validate
   * @return the loaded UserData if credentials are correct
   * @throws IOException if the user file is missing or unreadable
   * @throws ParserConfigurationException if XML parser configuration fails
   * @throws SAXException if XML parsing errors occur
   * @throws UserDataParseException if password validation fails
   */
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

  /**
   * Writes a new user record to XML (overwriting if exists).
   *
   * @param userData the UserData to write
   * @throws XMLStreamException if XML streaming errors occur
   * @throws IOException if I/O fails
   * @throws UserDataWriteException if writing fails
   */
  @Override
  public void writeNewUserData(UserData userData)
      throws XMLStreamException, IOException, UserDataWriteException {
    // for new users, just write out the data (creates or overwrites)
    writeUserData(userData);
  }

  /**
   * Serializes the given UserData to its associated XML file.
   * Lazy-initializes the writer if necessary.
   *
   * @param userData the UserData to persist
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML streaming errors occur
   * @throws IOException if I/O fails
   */
  @Override
  public void writeUserData(UserData userData)
      throws UserDataWriteException, XMLStreamException, IOException {
    if (myUserDataWriter == null) {
      myUserDataWriter = new UserDataWriter();
    }
    myUserDataWriter.writeUsersData(userData);
  }

  /**
   * Writes the currently loaded UserData back to its file.
   *
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML streaming errors occur
   * @throws IOException if I/O fails
   */
  @Override
  public void writeCurrentUserData()
      throws UserDataWriteException, XMLStreamException, IOException {
    if (myUserData == null) {
      throw new IllegalStateException("No user data loaded to write");
    }
    writeUserData(myUserData);
  }

  /**
   * Updates or creates game and level data for the user, merges stats, and persists.
   *
   * @param playerName the username of the player (must match loaded data)
   * @param gameName the game identifier to update
   * @param levelName the level identifier to update
   * @param newLevelStats map of stat names to new values
   */
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
    Map<String, String> mergedStatsForLevel = updatePlayerStatMap(
        newLevelStats,
        targetLevel.levelHighestStatMap()
    );
    // update lastPlayed timestamps
    targetLevel = new UserLevelData(
        targetLevel.levelName(),
        new Date().toString(),
        mergedStatsForLevel
    );
    targetGame.playerLevelStatMap().put(levelName, targetLevel);

    Map<String,String> mergedStatsForGame = updatePlayerStatMap(newLevelStats, targetGame.playerHighestGameStatMap());
    // also update game lastPlayed
    targetGame = new UserGameData(
        targetGame.gameName(),
        new Date().toString(),
        mergedStatsForGame,
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

  /**
   * Checks if the provided password matches the stored one.
   *
   * @param data the loaded UserData record
   * @param password the plaintext password to validate
   * @return true if passwords match, false otherwise
   */
  private boolean isCorrectPassword(UserData data, String password) {
    return data.password().equals(password);
  }

  /**
   * Creates a new UserGameData with empty stats and current timestamp.
   *
   * @param gameName the name of the new game
   * @return a blank UserGameData record
   */
  private UserGameData makeNewGameData(String gameName) {
    String now = new Date().toString();
    return new UserGameData(
        gameName,
        now,
        new HashMap<>(),
        new HashMap<>()
    );
  }

  /**
   * Creates a new UserLevelData with empty stats and current timestamp.
   *
   * @param levelName the name of the new level
   * @return a blank UserLevelData record
   */
  private UserLevelData makeNewLevelData(String levelName) {
    String now = new Date().toString();
    return new UserLevelData(
        levelName,
        now,
        new HashMap<>()
    );
  }

  /**
   * Merges newStats into oldStats, keeping the greater numeric values when applicable.
   * Non-numeric values are always overwritten.
   *
   * @param newStats map of incoming stat values
   * @param oldStats existing stat map to update
   * @return the merged stat map
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
   *
   * @return the current username, or "defaultUser" if none loaded
   */
  public String getCurrentUsername() {
    // Return the username of currently parsed user data
    return myUserData != null ? myUserData.username() : "defaultUser";
  }


  /**
   * Returns the file path for the current user data file.
   *
   * @return absolute path of the loaded data file, or a placeholder if none loaded
   */
  public String getUserDataFilePath() {
    return dataFile != null ? dataFile.getAbsolutePath() : "No file loaded";
  }
}
