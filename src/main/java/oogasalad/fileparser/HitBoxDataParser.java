package oogasalad.fileparser;

import oogasalad.exceptions.HitBoxParseException;
import oogasalad.fileparser.records.HitBoxData;
import org.w3c.dom.Element;

/**
 * @author Billy McCune
 */
public class HitBoxDataParser {

  /**
   * Getter for retrieving the HitBox data
   *
   * @param objectNode the node to retrieve
   * @return a new HitBoxData object
   */
  public HitBoxData getHitBoxData(Element objectNode) throws HitBoxParseException {
    try {
      String shape = objectNode.getAttribute("hitBoxShape");
      int hitBoxWidth = Integer.parseInt(objectNode.getAttribute("hitBoxWidth"));
      int hitBoxHeight = Integer.parseInt(objectNode.getAttribute("hitBoxHeight"));
      int spriteDx = Integer.parseInt(objectNode.getAttribute("spriteDx"));
      int spriteDy = Integer.parseInt(objectNode.getAttribute("spriteDy"));
      return new HitBoxData(shape, hitBoxWidth, hitBoxHeight, spriteDx, spriteDy);
    } catch (NumberFormatException | NullPointerException e) {
      throw new HitBoxParseException(e.getMessage());
    }
  }

}
