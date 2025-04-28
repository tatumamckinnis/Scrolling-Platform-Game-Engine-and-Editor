package oogasalad.userData.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import oogasalad.exceptions.UserDataWriteException;
import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;
import oogasalad.userData.records.UserLevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * UserDataWriter is responsible for serializing UserData objects
 * into XML format. It provides methods to write user data to an OutputStream,
 * a file path, a File object, or a default location using XML streaming.
 * It maintains indentation and handles directory creation.
 *
 * @author Billy McCune
 */
public class UserDataWriter {

  private static Logger LOG = LogManager.getLogger();

  /**
   * Writes a single <user> XML document for the given UserData to the provided OutputStream.
   * Uses streaming XML writer to generate properly indented XML.
   *
   * @param user the UserData record to serialize
   * @param out the OutputStream to write the XML data to
   * @throws UserDataWriteException if writing an element fails
   * @throws XMLStreamException if an XML writing error occurs
   */
  public void writeUsersData(UserData user, OutputStream out)
      throws UserDataWriteException, XMLStreamException {
    XMLOutputFactory factory = XMLOutputFactory.newInstance();
    XMLStreamWriter w = factory.createXMLStreamWriter(out, "UTF-8");

    w.writeStartDocument("UTF-8", "1.0");
    w.writeCharacters("\n");
    w.writeStartElement("users");
    w.writeCharacters("\n");
    w.writeStartElement("user");

    writeUserBasicInfo(w, user);
    writeUserGameData(w, user.userGameData());

    w.writeCharacters("\n");
    w.writeEndElement(); // user
    w.writeCharacters("\n");
    w.writeEndElement(); // users
    w.writeEndDocument();
    w.flush();
    w.close();
  }

  /**
   * Writes the user XML to the given file path, creating directories as needed.
   * If the file exists, it is overwritten.
   *
   * @param user the UserData to serialize
   * @param filePath the path of the file to write to
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML writing fails
   * @throws IOException if file operations fail
   */
  public void writeUsersData(UserData user, String filePath)
      throws UserDataWriteException, XMLStreamException, IOException {
    File file = new File(filePath);
    writeUsersData(user, file);
  }

  /**
   * Writes the user XML to the given File, creating parent directories as needed.
   *
   * @param user the UserData to serialize
   * @param file the File object to write to
   * @throws UserDataWriteException if writing fails
   * @throws IOException if directory creation or file IO fails
   * @throws XMLStreamException if XML writing fails
   */
  public void writeUsersData(UserData user, File file)
      throws UserDataWriteException, IOException, XMLStreamException {
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      if (!parent.mkdirs()) {
        throw new IOException("Failed to create directories: " + parent);
      }
    }
    try (OutputStream out = new FileOutputStream(file)) {
      writeUsersData(user, out);
    }
  }

  /**
   * Writes the user XML using a default filename (<username>.xml) in the data/userData directory.
   * Creates the directory if it does not exist and logs the output path.
   *
   * @param user the UserData to serialize
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML writing fails
   * @throws IOException if directory or file IO fails
   */
  public void writeUsersData(UserData user)
      throws UserDataWriteException, XMLStreamException, IOException {
    String directoryPath = "data/userData/";
    String filename = user.username() + ".xml";
    File directory = new File(directoryPath);
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new IOException("Failed to create directory: " + directoryPath);
      }
    }
    
    String fullPath = directoryPath + filename;
    LOG.info("Writing user " + user.username() + " to " + fullPath);
    writeUsersData(user, fullPath);
  }

  /**
   * Writes the basic user information fields to the XML.
   * 
   * @param w the XML writer
   * @param user the user data
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML errors occur
   */
  private void writeUserBasicInfo(XMLStreamWriter w, UserData user) 
      throws UserDataWriteException, XMLStreamException {
    //basic UserData fields
    writeElement(w, "username",    user.username(),       2);
    writeElement(w, "displayName", user.displayName(),    2);
    writeElement(w, "email",       user.email(),          2);
    writeElement(w, "password",    user.password(),       2);
    writeElement(w, "language",    user.language(),       2);
    writeElement(w, "bio",         user.bio(),            2);
    writeElement(w, "userImage",   user.userImage().getPath(), 2);
  }
  
  /**
   * Writes the list of game data for the user.
   * 
   * @param w the XML writer
   * @param gameDataList the list of game data
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML errors occur
   */
  private void writeUserGameData(XMLStreamWriter w, List<UserGameData> gameDataList)
      throws UserDataWriteException, XMLStreamException {
    //userGameDataList
    w.writeCharacters("\n  ");
    w.writeStartElement("userGameDataList");
    
    for (UserGameData ugd : gameDataList) {
      w.writeCharacters("\n    ");
      w.writeStartElement("userGameData");

      writeElement(w, "gameName",    ugd.gameName(),   6);
      writeElement(w, "lastPlayed",  ugd.lastPlayed(), 6);
      
      writeGameStats(w, ugd.playerHighestGameStatMap());
      writeLevelStats(w, ugd.playerLevelStatMap());

      w.writeCharacters("\n    ");
      w.writeEndElement(); // userGameData
    }
    
    w.writeCharacters("\n  ");
    w.writeEndElement(); // userGameDataList
  }
  
  /**
   * Writes the game stats map.
   * 
   * @param w the XML writer
   * @param statsMap the game stats map
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML errors occur
   */
  private void writeGameStats(XMLStreamWriter w, Map<String, String> statsMap)
      throws UserDataWriteException, XMLStreamException {
    //playerHighestGameStatMap
    w.writeCharacters("\n      ");
    w.writeStartElement("playerHighestGameStatMap");
    
    for (Map.Entry<String, String> e : statsMap.entrySet()) {
      w.writeCharacters("\n        ");
      w.writeStartElement("stat");
      w.writeAttribute("name", e.getKey());
      w.writeCharacters(e.getValue());
      w.writeEndElement();
    }
    
    w.writeCharacters("\n      ");
    w.writeEndElement(); // playerHighestGameStatMap
  }
  
  /**
   * Writes the level stats map.
   * 
   * @param w the XML writer
   * @param levelMap the level stats map
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML errors occur
   */
  private void writeLevelStats(XMLStreamWriter w, Map<String, UserLevelData> levelMap)
      throws UserDataWriteException, XMLStreamException {
    //playerLevelStatMap
    w.writeCharacters("\n      ");
    w.writeStartElement("playerLevelStatMap");
    
    for (UserLevelData uld : levelMap.values()) {
      w.writeCharacters("\n        ");
      w.writeStartElement("level");

      writeElement(w, "levelName",      uld.levelName(),      8);
      writeElement(w, "lastPlayed",     uld.lastPlayed(),     8);
      writeLevelStatMap(w, uld.levelHighestStatMap());

      w.writeCharacters("\n        ");
      w.writeEndElement(); //level
    }
    
    w.writeCharacters("\n      ");
    w.writeEndElement(); //playerLevelStatMap
  }
  
  /**
   * Writes a single level's stat map.
   * 
   * @param w the XML writer
   * @param statMap the level stat map
   * @throws UserDataWriteException if writing fails
   * @throws XMLStreamException if XML errors occur
   */
  private void writeLevelStatMap(XMLStreamWriter w, Map<String, String> statMap)
      throws UserDataWriteException, XMLStreamException {
    //levelHighestStatMap
    w.writeCharacters("\n          ");
    w.writeStartElement("levelHighestStatMap");
    
    for (Map.Entry<String, String> se : statMap.entrySet()) {
      w.writeCharacters("\n            ");
      w.writeStartElement("stat");
      w.writeAttribute("name", se.getKey());
      w.writeCharacters(se.getValue());
      w.writeEndElement();
    }
    
    w.writeCharacters("\n          ");
    w.writeEndElement(); //levelHighestStatMap
  }

  /**
   * Helper to write an indented element with text content.
   *
   * @param w the XMLStreamWriter to use
   * @param name the element name
   * @param text the text content of the element (null is allowed)
   * @param indentSpaces number of spaces to indent before the element
   * @throws UserDataWriteException if writing the element fails
   * @throws XMLStreamException if XML writing fails
   */
  private void writeElement(XMLStreamWriter w,
      String name,
      String text,
      int indentSpaces) throws UserDataWriteException, XMLStreamException {
    w.writeCharacters("\n" + " ".repeat(indentSpaces));
    w.writeStartElement(name);
    if (text != null) w.writeCharacters(text);
    w.writeEndElement();
  }
}
