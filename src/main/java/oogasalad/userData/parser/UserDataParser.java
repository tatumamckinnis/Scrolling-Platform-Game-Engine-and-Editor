package oogasalad.userData.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;

/**
 * Converts a <user> XML document into a UserData record, including nested game and level data.
 * Uses DOM parsing to read elements and delegates to UserGameDataParser.
 *
 * @author Billy McCune
 */
public class UserDataParser {

  private final UserGameDataParser myUserGameDataParser = new UserGameDataParser();

  /**
   * Parses the specified XML file into a UserData record.
   *
   * @param xmlFile the File containing the <user> XML document
   * @return a UserData record populated with user profile and game data
   * @throws IOException if file access fails or required elements are missing
   * @throws ParserConfigurationException if a parser cannot be created
   * @throws SAXException if XML parsing errors occur
   */
  public UserData getUserData(File xmlFile)
      throws IOException, ParserConfigurationException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(xmlFile);

    Element userElem = (Element) doc
        .getElementsByTagName("user").item(0);
    if (userElem == null) throw new IOException("<user> element missing");

    String username    = getText(userElem, "username");
    String displayName = getText(userElem, "displayName");
    String email       = getText(userElem, "email");
    String password    = getText(userElem, "password");
    String language    = getText(userElem, "language");
    String bio         = getText(userElem, "bio");
    File userImage     = new File(getText(userElem, "userImage"));

    // parse all <userGameData>
    List<UserGameData> games = new ArrayList<>();
    Element listElem = (Element) userElem
        .getElementsByTagName("userGameDataList").item(0);
    NodeList ugdNodes = listElem.getElementsByTagName("userGameData");
    for (int i = 0; i < ugdNodes.getLength(); i++) {
      Element ugdElem = (Element) ugdNodes.item(i);
      games.add(myUserGameDataParser.fromElement(ugdElem));
    }

    return new UserData(
        username,
        displayName,
        email,
        password,
        language,
        bio,
        userImage,
        null, //didn't have to implement avatar
        games
    );
  }

  /**
   * Utility method to extract text content of the first occurrence of a tag.
   *
   * @param parent the Element to search within
   * @param tag the tag name whose text content is to be retrieved
   * @return the text content of the tag, or null if not present
   */
  private static String getText(Element parent, String tag) {
    NodeList list = parent.getElementsByTagName(tag);
    return (list.getLength() == 0)
        ? null
        : list.item(0).getTextContent();
  }
}