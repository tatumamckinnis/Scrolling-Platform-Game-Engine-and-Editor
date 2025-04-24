package oogasalad.userData.parser;

import oogasalad.userData.records.UserLevelData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Converts <level> XML elements into UserLevelData records.
 */
public class UserLevelDataParser {


  public UserLevelData fromElement(Element levelElem) {
    String levelName = getText(levelElem, "levelName");
    String lastPlayed = getText(levelElem, "lastPlayed");

    Element statMapElem = (Element)
        levelElem.getElementsByTagName("levelHighestStatMap").item(0);
    Map<String, String> statMap = new LinkedHashMap<>();
    NodeList stats = statMapElem.getElementsByTagName("stat");
    for (int i = 0; i < stats.getLength(); i++) {
      Element statElem = (Element) stats.item(i);
      String name = statElem.getAttribute("name");
      String value = statElem.getTextContent();
      statMap.put(name, value);
    }

    return new UserLevelData(levelName, lastPlayed, statMap);
  }

  private static String getText(Element parent, String tag) {
    NodeList list = parent.getElementsByTagName(tag);
    return (list.getLength() == 0)
        ? null
        : list.item(0).getTextContent();
  }
}