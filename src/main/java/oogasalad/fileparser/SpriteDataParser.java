package oogasalad.fileparser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteData;
import oogasalad.fileparser.records.SpriteRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses XML sprite files into structured {@link SpriteData} records containing base frame data,
 * frame sequences, and animation sequences.
 *
 * <p>
 * This parser reads sprite data using information provided via a {@link SpriteRequest}, locates the
 * appropriate XML file based on configured file paths, and extracts relevant sprite information
 * including base images, frame lists, and animation timelines.
 * </p>
 *
 * <p>
 * File paths are configured via a {@code fileStructure.properties} file that specifies the
 * locations of sprite and graphics data directories.
 * </p>
 *
 * <p>
 * This class is typically used as part of the level loading or game asset initialization process.
 * </p>
 *
 * @author Billy McCune
 */
public class SpriteDataParser {

  // Base path to the graphics data and sprite data.
  private final String pathToGraphicsData;
  private final String pathToSpriteData;
  private static final String nameAttribute = "name";

  /**
   * Constructs a new {@code SpriteDataParser} by reading the properties file and loading the
   * graphics and game data file paths.
   *
   * @throws SpriteParseException if an error occurs while loading the properties file or required
   *                              properties are missing.
   */
  public SpriteDataParser() throws SpriteParseException {
    Map<String, String> dataPaths = loadDataPaths();
    this.pathToGraphicsData =
        System.getProperty("user.dir") + File.separator + getRequiredProperty(dataPaths,
            "path.to.graphics.data");
    this.pathToSpriteData =
        System.getProperty("user.dir") + File.separator + getRequiredProperty(dataPaths,
            "path.to.game.data");
  }

  /**
   * Loads required data paths from the properties file into a map.
   *
   * @return a map containing the keys "graphicsDataPath" and "spriteDataPath" mapped to their
   * corresponding values.
   * @throws SpriteParseException if the properties file is not found, or required properties are
   *                              missing.
   */
  private Map<String, String> loadDataPaths() throws SpriteParseException {
    Properties properties = new Properties();
    Map<String, String> fileStructureProperties = new HashMap<>();
    try (InputStream input = getClass().getClassLoader()
        .getResourceAsStream("oogasalad/config/file/fileStructure.properties")) {
      if (input == null) {
        throw new SpriteParseException(
            "Properties file 'fileStructure.properties' not found in classpath.");
      }
      properties.load(input);
      for (String key : properties.stringPropertyNames()) {
        fileStructureProperties.put(key, properties.getProperty(key));
      }
    } catch (IOException e) {
      throw new SpriteParseException("Error reading properties file: " + e.getMessage(), e);
    }

    return fileStructureProperties;
  }

  /**
   * Retrieves a required property from the specified properties map and ensures it is present and
   * non-empty.
   *
   * @param properties the map containing properties loaded from the file.
   * @param key        the key whose corresponding property value is required.
   * @return the non-null, trimmed property value associated with the given key.
   * @throws SpriteParseException if the property for the given key is missing or its value is
   *                              empty.
   */
  private String getRequiredProperty(Map<String, String> properties, String key)
      throws SpriteParseException {
    String value = properties.get(key);
    if (value == null || value.trim().isEmpty()) {
      throw new SpriteParseException("Required property '" + key + "' is missing or empty.");
    }
    return value.trim();
  }


  /**
   * Retrieves a {@link SpriteData} record from an XML sprite file.
   *
   * <p>
   * Builds the file path for the sprite XML file, loads and parses the document, and extracts the
   * sprite, frame, and animation information from it.
   * </p>
   *
   * @param request a {@link SpriteRequest} object containing all required sprite parameters.
   * @return a {@link SpriteData} object representing the parsed sprite data.
   * @throws SpriteParseException if an error occurs during parsing or if the specified sprite is
   *                              not found.
   */
  public SpriteData getSpriteData(SpriteRequest request) throws SpriteParseException {
    String filePath = buildFilePath(
        request.gameName(),
        request.group(),
        request.type(),
        request.spriteFile()
    );

    Document doc;
    File spriteFile = new File(filePath);

    if (!spriteFile.exists() || !spriteFile.isFile()) {

      filePath = buildFilePath(request.gameName(), null, null, request.spriteFile());
      doc = loadDocument(filePath);

    } else {

      doc = loadDocument(filePath);

    }

    Element spriteFileElement = doc.getDocumentElement();

    File spriteSheetFile = getSpriteSheetFile(spriteFileElement, request.gameName());

    Element targetSprite = getTargetSprite(spriteFileElement, request.spriteName());
    if (targetSprite == null) {
      throw new SpriteParseException(
          "Sprite with name " + request.spriteName() + " not found in file " + filePath);
    }

    FrameData baseImage = parseBaseImage(targetSprite, request.spriteName());
    List<FrameData> frames = parseFrames(targetSprite);
    List<AnimationData> animations = parseAnimations(targetSprite);

    return new SpriteData(request.spriteName(), spriteSheetFile, baseImage, frames, animations);
  }


  /**
   * Builds the file path to the sprite XML file.
   *
   * @param gameName   the game name.
   * @param group      the group folder name.
   * @param type       the type folder name.
   * @param spriteFile the sprite file name.
   * @return the full file path as a {@code String}.
   */
  private String buildFilePath(String gameName, String group, String type, String spriteFile) {
    if (type == null & group == null) {
      return pathToSpriteData + File.separator + gameName + File.separator + spriteFile;
    }
    if (type == null || type.trim().isEmpty()) {
      return pathToSpriteData + File.separator + gameName + File.separator + group
          + File.separator + spriteFile;
    }
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
    } catch (ParserConfigurationException | IOException | SAXException e) {
      throw new SpriteParseException(e.getMessage(), e);
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
   * Searches for and returns the <code>&lt;sprite&gt;</code> element with the given spriteName.
   *
   * @param spriteFileElement the root element of the sprite file.
   * @param spriteName        the name of the sprite.
   * @return the matching sprite element, or null if not found.
   */
  private Element getTargetSprite(Element spriteFileElement, String spriteName) {
    NodeList spriteNodes = spriteFileElement.getElementsByTagName("sprite");
    for (int i = 0; i < spriteNodes.getLength(); i++) {
      Element spriteElement = (Element) spriteNodes.item(i);
      if (spriteName.equals(spriteElement.getAttribute(nameAttribute))) {
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
   * Parses all frame elements within the <code>&lt;frames&gt;</code>element.
   *
   * @param targetSprite the sprite element containing the frames.
   * @return a list of FrameData records.
   */
  private List<FrameData> parseFrames(Element targetSprite) {
    List<FrameData> frames = new ArrayList<>();
    NodeList framesNodes = targetSprite.getElementsByTagName("frames");
    if (framesNodes.getLength() > 0) {
      Element framesElement = (Element) framesNodes.item(0);
      NodeList frameNodes = framesElement.getElementsByTagName("frame");
      for (int i = 0; i < frameNodes.getLength(); i++) {
        Element frameElement = (Element) frameNodes.item(i);
        frames.add(parseFrameData(frameElement));
      }
    }
    return frames;
  }

  /**
   * Parses a <code>&lt;frames&gt;</code> element and returns a FrameData record.
   *
   * @param frameElement the frame element from the XML.
   * @return a FrameData record containing the frame's attributes.
   */
  private FrameData parseFrameData(Element frameElement) {
    String name = frameElement.getAttribute(nameAttribute);
    int x = Integer.parseInt(frameElement.getAttribute("x"));
    int y = Integer.parseInt(frameElement.getAttribute("y"));
    int width = Integer.parseInt(frameElement.getAttribute("width"));
    int height = Integer.parseInt(frameElement.getAttribute("height"));
    return new FrameData(name, x, y, width, height);
  }

  /**
   * Parses all animation elements within the <code>&lt;animations&gt;</code> element.
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
   * Parses an <code>&lt;animation&gt;</code> element and returns an AnimationData record.
   *
   * @param animationElement the animation element from the XML.
   * @return an AnimationData record containing the animation's attributes.
   */
  private AnimationData parseAnimationData(Element animationElement) {
    String name = animationElement.getAttribute(nameAttribute);
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
