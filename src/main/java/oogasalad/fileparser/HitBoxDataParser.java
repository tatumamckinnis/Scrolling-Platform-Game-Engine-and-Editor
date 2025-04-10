package oogasalad.fileparser;

import oogasalad.exceptions.HitBoxParseException;
import oogasalad.fileparser.records.HitBoxData;
import org.w3c.dom.Element;

/**
 * A parser for extracting hitbox data from an XML element.
 * <p>
 * This class is responsible for reading and converting hitbox properties from the provided XML node
 * into a {@link HitBoxData} object.
 * </p>
 *
 * @author Billy
 */
public class HitBoxDataParser {

  /**
   * Retrieves the hitbox data from the given XML element.
   * <p>
   * This method reads the hitbox attributes including
   * shape, width, height, and sprite offsets from
   * the provided XML element and creates a corresponding {@link HitBoxData} object.
   * </p>
   *
   * @param objectNode the XML element that contains hitbox data.
   * @return a new {@link HitBoxData} object constructed from the provided element.
   * @throws HitBoxParseException if parsing hitbox attributes fails.
   */
  public HitBoxData getHitBoxData(Element objectNode) throws HitBoxParseException {
    try {
      String shape = objectNode.getAttribute("hitBoxShape");
      int hitBoxWidth = Integer.parseInt(objectNode.getAttribute("hitBoxWidth"));
      int hitBoxHeight = Integer.parseInt(objectNode.getAttribute("hitBoxHeight"));
      int spriteDx = Integer.parseInt(objectNode.getAttribute("spriteDx"));
      int spriteDy = Integer.parseInt(objectNode.getAttribute("spriteDy"));
      return new HitBoxData(shape, hitBoxWidth, hitBoxHeight, spriteDx, spriteDy);
    } catch (NumberFormatException e) {
      throw new HitBoxParseException(e.getMessage(), e);
    }
  }
}
