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
import oogasalad.exceptions.SpriteParseException;

/**
 * Parses a sprite XML file and builds a SpriteData object.
 * <p>
 * The file is located using: user directory + graphics data path + game + group + type + sprite file.
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
 * @author ...
 */
public class SpriteDataParser {

  // Base path to the graphics data and sprite data.
  private final String pathToGraphicsData;
  private final String pathToSpriteData;

  // Constructor that loads the properties file and sets the paths.
  public SpriteDataParser() {
    String[] paths = loadDataPaths();
    if (paths.length < 2) {
      // Handle error appropriately; here we default to empty paths.
      this.pathToGraphicsData = "";
      this.pathToSpriteData = "";
    } else {
      this.pathToGraphicsData = paths[0];
      this.pathToSpriteData = paths[1];
    }
  }

  /**
   * Loads the required data paths from the properties file.
   *
   * @return an array where index 0 is the graphics data path and index 1 is the sprite data path.
   */
  private String[] loadDataPaths() {
    Properties properties = new Properties();
    try (InputStream input = getClass().getClassLoader()
        .getResourceAsStream("oogasalad/file/fileStructure.properties")) {
      properties.load(input);
    } catch (IOException e) {
      e.printStackTrace();
      // Return an empty array if loading fails.
      return new String[0];
    }
    String[] paths = new String[2];
    paths[0] = System.getProperty("user.dir") + File.separator + properties.getProperty("path.to.graphics.data");
    paths[1] = System.getProperty("user.dir") + File.separator + properties.getProperty("path.to.game.data");
    return paths;
  }

  /**
   * Retrieves a SpriteData record from an XML sprite file.
   *
   * @param gameName   the name of the game.
   * @param group      the group folder name.
   * @param type       the type folder name.
   * @param spriteName the name of the sprite to locate.
   * @param spriteFile the XML file containing the sprite information.
   * @return a SpriteData object.
   * @throws SpriteParseException if parsing fails.
   */
  public SpriteData getSpriteData(String gameName, String group, String type,
      String spriteName, String spriteFile) throws SpriteParseException {
    // Build the file path.
    String filePath = buildFilePath(gameName, group, type, spriteFile);
    Document doc = loadDocument(filePath);
    Element spriteFileElement = doc.getDocumentElement();

    // Retrieve the sprite sheet file.
    File spriteSheetFile = getSpriteSheetFile(spriteFileElement, gameName);

    // Locate the target sprite in the XML.
    Element targetSprite = getTargetSprite(spriteFileElement, spriteName);
    if (targetSprite == null) {
      throw new SpriteParseException("Sprite with name " + spriteName + " not found in file " + filePath);
    }

    // Parse base image data, frames, and animations.
    FrameData baseImage = parseBaseImage(targetSprite, spriteName);
    List<FrameData> frames = parseFrames(targetSprite, spriteSheetFile);
    List<AnimationData> animations = parseAnimations(targetSprite);

    return new SpriteData(spriteName, spriteSheetFile, baseImage, frames, animations);
  }

  /**
   * Builds the file path to the sprite XML file.
   *
   * @param gameName   the game name.
   * @param group      the group folder name.
   * @param type       the type folder name.
   * @param spriteFile the sprite file name.
   * @return the full file path as a String.
   */
  private String buildFilePath(String gameName, String group, String type, String spriteFile) {
    return pathToSpriteData + File.separator + gameName + File.separator + group
        + File.separator + type + File.separator + spriteFile;
  }

  /**
   * Loads an XML Document from the given file path.
   *
   * @param filePath the path to the XML file.
   * @return the loaded Document.
   * @throws SpriteParseException if an error occurs while loading the document.
   */
  private Document loadDocument(String filePath) throws SpriteParseException {
    try {
      File xmlFile = new File(filePath);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(xmlFile);
      doc.getDocumentElement().normalize();
      return doc;
    } catch (Exception e) {
      throw new SpriteParseException("Error loading document from file " + filePath + ": " + e.getMessage());
    }
  }

  /**
   * Retrieves the sprite sheet file from the spriteFile element's imagePath attribute.
   *
   * @param spriteFileElement the root element of the sprite file.
   * @param gameName          the name of the game.
   * @return the sprite sheet File object.
   */
  private File getSpriteSheetFile(Element spriteFileElement, String gameName) {
    String imagePath = spriteFileElement.getAttribute("imagePath");
    return new File(pathToGraphicsData + File.separator + gameName, imagePath);
  }

  /**
   * Searches for and returns the <sprite> element with the given spriteName.
   *
   * @param spriteFileElement the root element of the sprite file.
   * @param spriteName        the name of the sprite.
   * @return the matching sprite element, or null if not found.
   */
  private Element getTargetSprite(Element spriteFileElement, String spriteName) {
    NodeList spriteNodes = spriteFileElement.getElementsByTagName("sprite");
    for (int i = 0; i < spriteNodes.getLength(); i++) {
      Element spriteElement = (Element) spriteNodes.item(i);
      if (spriteName.equals(spriteElement.getAttribute("name"))) {
        return spriteElement;
      }
    }
    return null;
  }

  /**
   * Parses the base frame data (base image) from the target sprite element.
   *
   * @param targetSprite the sprite element representing the target sprite.
   * @param spriteName   the name of the sprite.
   * @return a FrameData record for the base image.
   */
  private FrameData parseBaseImage(Element targetSprite, String spriteName) {
    int baseX = Integer.parseInt(targetSprite.getAttribute("x"));
    int baseY = Integer.parseInt(targetSprite.getAttribute("y"));
    int baseWidth = 0;
    int baseHeight = 0;
    String widthAttr = targetSprite.getAttribute("width");
    String heightAttr = targetSprite.getAttribute("height");
    if (!widthAttr.isEmpty()) {
      baseWidth = Integer.parseInt(widthAttr);
    }
    if (!heightAttr.isEmpty()) {
      baseHeight = Integer.parseInt(heightAttr);
    }
    return new FrameData(spriteName, baseX, baseY, baseWidth, baseHeight);
  }

  /**
   * Parses all frame elements within the <frames> element.
   *
   * @param targetSprite    the sprite element containing the frames.
   * @param spriteSheetFile the sprite sheet image file.
   * @return a list of FrameData records.
   */
  private List<FrameData> parseFrames(Element targetSprite, File spriteSheetFile) {
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
    return frames;
  }

  /**
   * Parses a <frame> element and returns a FrameData record.
   *
   * @param frameElement    the frame element from the XML.
   * @param spriteSheetFile the sprite sheet image file.
   * @return a FrameData record containing the frame's attributes.
   */
  private FrameData parseFrameData(Element frameElement, File spriteSheetFile) {
    String name = frameElement.getAttribute("name");
    int x = Integer.parseInt(frameElement.getAttribute("x"));
    int y = Integer.parseInt(frameElement.getAttribute("y"));
    int width = Integer.parseInt(frameElement.getAttribute("width"));
    int height = Integer.parseInt(frameElement.getAttribute("height"));
    return new FrameData(name, x, y, width, height);
  }

  /**
   * Parses all animation elements within the <animations> element.
   *
   * @param targetSprite the sprite element containing the animations.
   * @return a list of AnimationData records.
   */
  private List<AnimationData> parseAnimations(Element targetSprite) {
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
    return animations;
  }

  /**
   * Parses an <animation> element and returns an AnimationData record.
   *
   * @param animationElement the animation element from the XML.
   * @return an AnimationData record containing the animation's attributes.
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
