package oogasalad.userData.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import oogasalad.exceptions.UserDataWriteException;
import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;
import oogasalad.userData.records.UserLevelData;

public class UserDataWriter {

  /**
   * Writes a single <user> XML document for the given UserData to the provided OutputStream.
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

    //basic UserData fields
    writeElement(w, "username",    user.username(),       2);
    writeElement(w, "displayName", user.displayName(),    2);
    writeElement(w, "email",       user.email(),          2);
    writeElement(w, "password",    user.password(),       2);
    writeElement(w, "language",    user.language(),       2);
    writeElement(w, "bio",         user.bio(),            2);
    writeElement(w, "userImage",   user.userImage().getPath(), 2);

    //userGameDataList
    w.writeCharacters("\n  ");
    w.writeStartElement("userGameDataList");
    for (UserGameData ugd : user.userGameData()) {
      w.writeCharacters("\n    ");
      w.writeStartElement("userGameData");

      writeElement(w, "gameName",    ugd.gameName(),   6);
      writeElement(w, "lastPlayed",  ugd.lastPlayed(), 6);

      //playerHighestGameStatMap
      w.writeCharacters("\n      ");
      w.writeStartElement("playerHighestGameStatMap");
      for (Map.Entry<String, Double> e : ugd.playerHighestGameStatMap().entrySet()) {
        w.writeCharacters("\n        ");
        w.writeStartElement("stat");
        w.writeAttribute("name", e.getKey());
        w.writeCharacters(e.getValue().toString());
        w.writeEndElement();
      }
      w.writeCharacters("\n      ");
      w.writeEndElement(); // playerHighestGameStatMap

      //playerLevelStatMap
      w.writeCharacters("\n      ");
      w.writeStartElement("playerLevelStatMap");
      for (UserLevelData uld : ugd.playerLevelStatMap().values()) {
        w.writeCharacters("\n        ");
        w.writeStartElement("level");

        writeElement(w, "levelName",      uld.levelName(),      8);
        writeElement(w, "lastPlayed",     uld.lastPlayed(),     8);

        //levelHighestStatMap
        w.writeCharacters("\n          ");
        w.writeStartElement("levelHighestStatMap");
        for (Map.Entry<String, String> se : uld.levelHighestStatMap().entrySet()) {
          w.writeCharacters("\n            ");
          w.writeStartElement("stat");
          w.writeAttribute("name", se.getKey());
          w.writeCharacters(se.getValue());
          w.writeEndElement();
        }
        w.writeCharacters("\n          ");
        w.writeEndElement(); //levelHighestStatMap

        w.writeCharacters("\n        ");
        w.writeEndElement(); //level
      }
      w.writeCharacters("\n      ");
      w.writeEndElement(); //playerLevelStatMap

      w.writeCharacters("\n    ");
      w.writeEndElement(); // userGameData
    }
    w.writeCharacters("\n  ");
    w.writeEndElement(); // userGameDataList

    w.writeCharacters("\n");
    w.writeEndElement(); // user
    w.writeCharacters("\n");
    w.writeEndElement(); // users
    w.writeEndDocument();
    w.flush();
    w.close();
  }

  /**
   * Writes the user XML to the given file path, creating directories if needed.
   * If the file does not exist, it is created; otherwise it's overwritten.
   */
  public void writeUsersData(UserData user, String filePath)
      throws UserDataWriteException, XMLStreamException, IOException {
    File file = new File(filePath);
    writeUsersData(user, file);
  }

  /**
   * Writes the user XML to the given File, creating directories if needed.
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
    System.out.println("Writing user " + user.username() + " to " + fullPath);
    writeUsersData(user, fullPath);
  }

  /**
   * Helper to write an indented element with text content
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
