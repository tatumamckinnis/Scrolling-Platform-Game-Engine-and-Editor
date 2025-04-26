package oogasalad.userData;

import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import oogasalad.exceptions.UserDataParseException;
import oogasalad.exceptions.UserDataWriteException;
import oogasalad.userData.records.UserData;
import org.xml.sax.SAXException;

/**
 * UserApi defines operations for parsing, writing, and updating user data.
 * Implementations handle XML serialization and validation of UserData objects.
 *
 * @author Billy McCune
 */
public interface UserApi {

  /**
   * Parses and loads user data for the given credentials.
   *
   * @param username the username to locate the XML file (data/userData/{username}.xml)
   * @param password the password to validate against the stored record
   * @return the loaded UserData record if credentials match
   * @throws IOException if file I/O fails or XML structure is invalid
   * @throws ParserConfigurationException if XML parser cannot be configured
   * @throws SAXException if XML parsing errors occur
   * @throws UserDataParseException if username/password validation fails
   */
   UserData parseUserData(String username, String password)
      throws IOException, ParserConfigurationException, SAXException, UserDataParseException;

  /**
   * Writes the provided UserData to persistent storage, overwriting existing data.
   *
   * @param userData the UserData record to serialize
   * @throws UserDataWriteException if writing to XML fails
   * @throws XMLStreamException if XML streaming errors occur
   * @throws IOException if file I/O fails
   */
  void writeUserData(UserData userData)
      throws UserDataWriteException, XMLStreamException, IOException;

  /**
   * Updates level-level statistics for the specified player and persists the change.
   * If the game or level does not exist, it will be created.
   *
   * @param playerName the username of the player
   * @param game the game identifier to update
   * @param level the level identifier to update
   * @param playerHighestStatMap a map of stat names to new stat values
   */
  void updatePlayerLevelStats(String playerName, String game, String level, Map<String,String> playerHighestStatMap);

  /**
   * Writes a new UserData record, intended for first-time user creation.
   * Behavior mirrors writeUserData but may include initialization logic.
   *
   * @param userData the new UserData record to create
   * @throws XMLStreamException if XML streaming errors occur
   * @throws IOException if file I/O fails
   * @throws UserDataWriteException if writing fails
   */
   void writeNewUserData(UserData userData)
      throws XMLStreamException, IOException, UserDataWriteException;

  /**
   * Persists the currently loaded UserData without requiring an explicit object.
   *
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML streaming errors occur
   * @throws IOException if file I/O fails
   */
   void writeCurrentUserData() throws UserDataWriteException, XMLStreamException, IOException;
}
