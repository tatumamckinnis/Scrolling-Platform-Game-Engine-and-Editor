package oogasalad.userData;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import oogasalad.exceptions.UserDataParseException;
import oogasalad.exceptions.UserDataWriteException;
import oogasalad.userData.parser.UserDataParser;
import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;
import oogasalad.userData.records.UserLevelData;
import oogasalad.userData.writer.UserDataWriter;
import org.xml.sax.SAXException;

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
    UserGameData targetGame = findOrCreateGameData(gameName);

    // 2) Find or create the UserLevelData
    UserLevelData targetLevel = findOrCreateLevelData(targetGame, levelName);

    // 3) Update level and game stats with merged values
    updateGameAndLevelStats(targetGame, targetLevel, levelName, newLevelStats);

    // 4) Persist back to XML
    saveUpdatedUserData();
  }

  /**
   * Finds or creates game data for the specified game name.
   *
   * @param gameName the game identifier
   * @return the existing or newly created UserGameData
   */
  private UserGameData findOrCreateGameData(String gameName) {
    for (UserGameData ugd : myUserData.userGameData()) {
      if (ugd.gameName().equals(gameName)) {
        return ugd;
      }
    }
    UserGameData newGame = makeNewGameData(gameName);
    myUserData.userGameData().add(newGame);
    return newGame;
  }

  /**
   * Finds or creates level data for the specified level within a game.
   *
   * @param game the game containing the level
   * @param levelName the level identifier
   * @return the existing or newly created UserLevelData
   */
  private UserLevelData findOrCreateLevelData(UserGameData game, String levelName) {
    UserLevelData level = game.playerLevelStatMap().get(levelName);
    if (level == null) {
      level = makeNewLevelData(levelName);
      game.playerLevelStatMap().put(levelName, level);
    }
    return level;
  }

  /**
   * Updates both game and level stats with merged values and current timestamps.
   *
   * @param game the game to update
   * @param level the level to update
   * @param levelName the level identifier
   * @param newStats the new stats to merge
   */
  private void updateGameAndLevelStats(UserGameData game, UserLevelData level, 
                                     String levelName, Map<String, String> newStats) {
    // Update level stats
    Map<String, String> mergedLevelStats = updatePlayerStatMap(
        newStats, level.levelHighestStatMap()
    );
    
    // Create updated level with new timestamp
    UserLevelData updatedLevel = new UserLevelData(
        level.levelName(),
        new Date().toString(),
        mergedLevelStats
    );
    game.playerLevelStatMap().put(levelName, updatedLevel);
    
    // Update game stats
    Map<String, String> mergedGameStats = updatePlayerStatMap(
        newStats, game.playerHighestGameStatMap()
    );
    
    // Create updated game with new timestamp
    UserGameData updatedGame = new UserGameData(
        game.gameName(),
        new Date().toString(),
        mergedGameStats,
        game.playerLevelStatMap()
    );
    
    // Replace old game data with updated version
    myUserData.userGameData().removeIf(g -> g.gameName().equals(game.gameName()));
    myUserData.userGameData().add(updatedGame);
  }
  
  /**
   * Saves the updated user data to XML.
   * 
   * @throws RuntimeException if saving fails
   */
  private void saveUpdatedUserData() {
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

    Map<String, String> result = new HashMap<>(oldStats);
    
    for (Map.Entry<String, String> entry : newStats.entrySet()) {
      String key = entry.getKey();
      String newValue = entry.getValue();
      
      if (!result.containsKey(key)) {
        // Brand new stat - just add it
        result.put(key, newValue);
        continue;
      }
      
      // Update existing stat
      updateExistingStat(result, key, newValue);
    }
    
    return result;
  }
  
  /**
   * Updates an existing stat with a new value if appropriate.
   * For numeric values, keeps the larger value. For non-numeric, overwrites.
   * 
   * @param stats the stats map to update
   * @param key the stat key
   * @param newValue the new stat value
   */
  private void updateExistingStat(Map<String, String> stats, String key, String newValue) {
    String oldValue = stats.get(key);
    
    if (areNumericValues(oldValue, newValue)) {
      double oldNumeric = Double.parseDouble(oldValue);
      double newNumeric = Double.parseDouble(newValue);
      
      if (newNumeric > oldNumeric) {
        stats.put(key, newValue);
      }
    } else {
      // Non-numeric values are always overwritten
      stats.put(key, newValue);
    }
  }
  
  /**
   * Checks if both values can be parsed as numbers.
   * 
   * @param value1 first value to check
   * @param value2 second value to check
   * @return true if both are valid numbers
   */
  private boolean areNumericValues(String value1, String value2) {
    try {
      Double.parseDouble(value1);
      Double.parseDouble(value2);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
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
