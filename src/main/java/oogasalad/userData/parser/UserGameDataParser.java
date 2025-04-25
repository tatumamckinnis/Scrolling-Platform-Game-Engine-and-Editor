package oogasalad.userData.parser;

import oogasalad.userData.records.UserGameData;
import oogasalad.userData.records.UserLevelData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Converts <userGameData> XML elements into UserGameData records.
 */
public class UserGameDataParser {

  UserLevelDataParser myUserLevelDataParser = new UserLevelDataParser();
  public UserGameData fromElement(Element ugdElem) {
    String gameName   = getText(ugdElem, "gameName");
    String lastPlayed = getText(ugdElem, "lastPlayed");

    // parse playerHighestGameStatMap
    Element highElem = (Element)
        ugdElem.getElementsByTagName("playerHighestGameStatMap").item(0);
    Map<String, Double> highMap = new LinkedHashMap<>();
    NodeList highStats = highElem.getElementsByTagName("stat");
    for (int i = 0; i < highStats.getLength(); i++) {
      Element s = (Element) highStats.item(i);
      highMap.put(
          s.getAttribute("name"),
          Double.parseDouble(s.getTextContent())
      );
    }

    // parse playerLevelStatMap
    Element lvlListElem = (Element)
        ugdElem.getElementsByTagName("playerLevelStatMap").item(0);
    NodeList levels = lvlListElem.getElementsByTagName("level");
    Map<String, UserLevelData> levelMap = new LinkedHashMap<>();
    for (int i = 0; i < levels.getLength(); i++) {
      Element lvlElem = (Element) levels.item(i);
      UserLevelData uld = myUserLevelDataParser.fromElement(lvlElem);
      levelMap.put(uld.levelName(), uld);
    }

    return new UserGameData(gameName, lastPlayed, highMap, levelMap);
  }

  private static String getText(Element parent, String tag) {
    NodeList list = parent.getElementsByTagName(tag);
    return (list.getLength() == 0)
        ? null
        : list.item(0).getTextContent();
  }
}