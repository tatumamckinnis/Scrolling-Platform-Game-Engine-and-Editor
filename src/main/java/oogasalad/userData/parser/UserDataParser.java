package oogasalad.userData.parser;

import javax.xml.parsers.ParserConfigurationException;
import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * Converts a <user> XML document into a UserData record.
 */
public class UserDataParser {

  UserGameDataParser myUserGameDataParser = new UserGameDataParser();

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
        /* avatar omitted */ null,
        games
    );
  }

  private static String getText(Element parent, String tag) {
    NodeList list = parent.getElementsByTagName(tag);
    return (list.getLength() == 0)
        ? null
        : list.item(0).getTextContent();
  }
}