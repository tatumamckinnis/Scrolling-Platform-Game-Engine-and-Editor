package oogasalad.fileparser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses a sprite XML file and builds a SpriteData object.
 * <p>
 * The file is located using: user directory + graphics data path + game + group + type + sprite
 * file.
 * <p>
 * Example XML file:
 * <pre>
 * &lt;spriteFile imagePath="dinosaurgame-sprites.png" width="1770" height="101"&gt;
 *   &lt;sprite name="Dino" x="944" y="0" w="97" h="101"&gt;
 *     &lt;frames&gt;
 *       &lt;frame name="DinoStart" x="944" y="0" width="97" height="101"/&gt;
 *       &lt;frame name="DinoDead" x="236" y="0" width="86" height="101"/&gt;
 *       &lt;frame name="DinoDuck1" x="354" y="0" width="118" height="60"/&gt;
 *       &lt;frame name="DinoDuck2" x="472" y="0" width="116" height="60"/&gt;
 *       &lt;frame name="DinoJump" x="590" y="0" width="88" height="94"/&gt;
 *       &lt;frame name="DinoRun1" x="708" y="0" width="87" height="94"/&gt;
 *       &lt;frame name="DinoRun2" x="826" y="0" width="88" height="94"/&gt;
 *     &lt;/frames&gt;
 *     &lt;animations&gt;
 *        &lt;animation name="walk" frameLen="0.15" frames="DinoRun1,DinoRun2"/&gt;
 *        &lt;animation name="jump" frameLen="0.15" frames="DinoJump"/&gt;
 *        &lt;animation name="dead" frameLen="0.15" frames="DinoDead"/&gt;
 *        &lt;animation name="duck-walk" frameLen="0.15" frames="DinoDuck1,DinoDuck2"/&gt;
 *     &lt;/animations&gt;
 *   &lt;/sprite&gt;
 * &lt;/spriteFile&gt;
 * </pre>
 *
 * @author Billy McCune
 */
public class SpriteDataParser {


  // Base path to the graphics data.
  private final String pathToGraphicsData;
  private final String pathToSpriteData;


  // Constructor that loads the properties file and sets the graphics data path.
  public SpriteDataParser() {
    this.pathToGraphicsData = loadDataPaths()[0];
    this.pathToSpriteData = loadDataPaths()[1];
  }

  /**
   * Loads the graphics data path from the properties file.
   *
   * @return the full path to the graphics data as a String
   */
  private String[] loadDataPaths() {
    Properties properties = new Properties();
    try (InputStream input = getClass().getClassLoader()
        .getResourceAsStream("oogasalad/file/fileStructure.properties")) {
      properties.load(input);
    } catch (IOException e) {
      e.printStackTrace();
      // Return a default or empty string if loading fails.
      return new String[0];
    }
    // Combine with the user directory if the property is relative.
    String[] paths = new String[properties.size()];
    paths[1] = System.getProperty("user.dir") + File.separator + properties.getProperty(
        "path.to.game.data");
    paths[0] = System.getProperty("user.dir") + File.separator + properties.getProperty(
        "path.to.graphics.data");
    return paths;
  }

  /**
   * Retrieves a SpriteData record from an XML sprite file.
   *
   * @param gameName   the name of the game
   * @param group      the group folder name
   * @param type       the type folder name
   * @param spriteName the name of the sprite to locate
   * @param spriteFile the XML file containing the sprite information
   * @return a SpriteData object, or null if parsing fails
   */
  public SpriteData getSpriteData(String gameName, String group, String type,
      String spriteName, String spriteFile) throws RuntimeException {
    // Build the file path using user directory, graphics data path, and provided folders.
    String filePath = buildFilePath(gameName, group, type, spriteFile);
    try {

      File xmlFile = new File(filePath);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(xmlFile);
      doc.getDocumentElement().normalize();

      // The root element is expected to be <spriteFile>.
      Element spriteFileElement = doc.getDocumentElement();
      // Retrieve the sprite sheet image file from the spriteFile element.
      String imagePath = spriteFileElement.getAttribute("imagePath");
      File spriteSheetFile = new File(pathToGraphicsData + File.separator + gameName, imagePath);

      // Find the <sprite> element with a matching name.
      NodeList spriteNodes = spriteFileElement.getElementsByTagName("sprite");
      Element targetSprite = null;
      for (int i = 0; i < spriteNodes.getLength(); i++) {
        Element spriteElement = (Element) spriteNodes.item(i);
        if (spriteElement.getAttribute("name").equals(spriteName)) {
          targetSprite = spriteElement;
          break;
        }
      }
      if (targetSprite == null) {
        throw new RuntimeException("Sprite with name " + spriteName
            + " not found in file " + filePath);
      }
      // Create a base image from the sprite element's attributes.
      int baseX = Integer.parseInt(targetSprite.getAttribute("x"));
      int baseY = Integer.parseInt(targetSprite.getAttribute("y"));
      int baseWidth = 0;
      int baseHeight = 0;
      if (targetSprite.getAttribute("width") != "") {
        baseWidth = Integer.parseInt(targetSprite.getAttribute("width"));
      }
      if (targetSprite.getAttribute("height") != "") {
        baseHeight = Integer.parseInt(targetSprite.getAttribute("height"));
      }
      FrameData baseImage = new FrameData(spriteName, baseX, baseY, baseWidth, baseHeight,
          spriteSheetFile);

      // Parse frames from the <frames> element.
      List<FrameData> frames = new ArrayList<>();
      NodeList framesNodes = targetSprite.getElementsByTagName("frames");
      if (framesNodes.getLength() > 0) {
        Element framesElement = (Element) framesNodes.item(0);
        NodeList frameNodes = framesElement.getElementsByTagName("frame");
        for (int i = 0; i < frameNodes.getLength(); i++) {
          Element frameElement = (Element) frameNodes.item(i);
          frames.add(parseFrameData(frameElement, spriteSheetFile));
        }
      }

      // Parse animations from the <animations> element.
      List<AnimationData> animations = new ArrayList<>();
      NodeList animationsNodes = targetSprite.getElementsByTagName("animations");
      if (animationsNodes.getLength() > 0) {
        Element animationsElement = (Element) animationsNodes.item(0);
        NodeList animationNodes = animationsElement.getElementsByTagName("animation");
        for (int i = 0; i < animationNodes.getLength(); i++) {
          Element animationElement = (Element) animationNodes.item(i);
          animations.add(parseAnimationData(animationElement));
        }
      }

      // Return the constructed SpriteData record.
      return new SpriteData(spriteName, baseImage, frames, animations);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Builds the file path to the sprite XML file.
   *
   * @param gameName   the game name
   * @param group      the group folder name
   * @param type       the type folder name
   * @param spriteFile the sprite file name
   * @return the full file path as a String
   */
  private String buildFilePath(String gameName, String group, String type, String spriteFile) {
    return pathToSpriteData + File.separator + gameName + File.separator + group
        + File.separator + type + File.separator + spriteFile;
  }

  /**
   * Parses a <frame> element and returns a FrameData record.
   *
   * @param frameElement    the frame element from the XML
   * @param spriteSheetFile the File object for the sprite sheet image
   * @return a FrameData record containing the frame's attributes
   */
  private FrameData parseFrameData(Element frameElement, File spriteSheetFile) {
    String name = frameElement.getAttribute("name");
    int x = Integer.parseInt(frameElement.getAttribute("x"));
    int y = Integer.parseInt(frameElement.getAttribute("y"));
    int width = Integer.parseInt(frameElement.getAttribute("width"));
    int height = Integer.parseInt(frameElement.getAttribute("height"));
    return new FrameData(name, x, y, width, height, spriteSheetFile);
  }

  /**
   * Parses an <animation> element and returns an AnimationData record.
   *
   * @param animationElement the animation element from the XML
   * @return an AnimationData record containing the animation's attributes
   */
  private AnimationData parseAnimationData(Element animationElement) {
    String name = animationElement.getAttribute("name");
    double frameLen = Double.parseDouble(animationElement.getAttribute("frameLen"));
    String framesAttr = animationElement.getAttribute("frames");
    String[] frameNames = framesAttr.split(",");
    List<String> framesList = new ArrayList<>();
    for (String frameName : frameNames) {
      framesList.add(frameName.trim());
    }
    return new AnimationData(name, frameLen, framesList);
  }
}
