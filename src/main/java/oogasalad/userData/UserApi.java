package oogasalad.userData;

import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import oogasalad.exceptions.UserDataParseException;
import oogasalad.exceptions.UserDataWriteException;
import oogasalad.userData.records.UserData;
import org.xml.sax.SAXException;

public interface UserApi {

  public UserData parseUserData(String username, String password)
      throws IOException, ParserConfigurationException, SAXException, UserDataParseException;

  public void writeUserData(UserData userData)
      throws UserDataWriteException, XMLStreamException, IOException;

  public void updatePlayerLevelStats(String playerName, String game, String level, Map<String,String> playerHighestStatMap);

  public void writeNewUserData(UserData userData)
      throws XMLStreamException, IOException, UserDataWriteException;

  public void writeCurrentUserData() throws UserDataWriteException, XMLStreamException, IOException;
}
