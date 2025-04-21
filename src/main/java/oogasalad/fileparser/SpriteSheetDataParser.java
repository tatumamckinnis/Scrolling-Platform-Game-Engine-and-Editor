package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.List;
import oogasalad.exceptions.SpriteSheetLoadException;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteSheetData;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses all sprite sheet related data given an XML root element. Takes the image path, width,
 * height, and a list of all the specified frames and uses it to create a new SpriteSheetData
 * object. Used as a helper for the DefaultFileParser.
 *
 * @author Jacob You
 */
public class SpriteSheetDataParser {

  /**
   * Takes in XML sprite sheet data and converts it into a file parser SpriteSheetData to be sent.
   *
   * @param root the root XML element that contains the camera data
   * @return the SpriteSheetData object that holds the information for the sprite sheet
   */
  public SpriteSheetData getSpriteSheetData(Element root) throws SpriteSheetLoadException {
    try {
      String imagePath = root.getAttribute("imagePath");
      int width = Integer.parseInt(root.getAttribute("width"));
      int height = Integer.parseInt(root.getAttribute("height"));

      List<FrameData> frames = new ArrayList<>();
      NodeList nodes = root.getElementsByTagName("sprite");

      for (int i = 0; i < nodes.getLength(); i++) {
        Element e = (Element) nodes.item(i);
        String name = e.getAttribute("name");
        int x = Integer.parseInt(e.getAttribute("x"));
        int y = Integer.parseInt(e.getAttribute("y"));
        int w = Integer.parseInt(e.getAttribute("width"));
        int h = Integer.parseInt(e.getAttribute("height"));
        frames.add(new FrameData(name, x, y, w, h));
      }

      return new SpriteSheetData(
          imagePath,
          width,
          height,
          frames
      );
    }
    catch (NumberFormatException e) {
      throw new SpriteSheetLoadException(e.getMessage(), e);
    }
  }
}
