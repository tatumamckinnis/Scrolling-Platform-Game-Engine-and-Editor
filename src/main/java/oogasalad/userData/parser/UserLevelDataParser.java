package oogasalad.userData.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import oogasalad.userData.records.UserLevelData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Converts <level> XML elements into UserLevelData records.
 * Parses level name, last played timestamp, and highest stats map.
 *
 * @author Billy McCune
 */
public class UserLevelDataParser {

  /**
   * Parses a <level> element into a UserLevelData record.
   *
   * @param levelElem the XML Element representing a <level>
   * @return a UserLevelData record populated with levelName, lastPlayed, and highest stat map
   */
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