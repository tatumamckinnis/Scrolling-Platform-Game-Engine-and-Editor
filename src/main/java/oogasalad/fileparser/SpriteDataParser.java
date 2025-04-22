package oogasalad.fileparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteData;
import oogasalad.fileparser.records.SpriteRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Parses sprite data including frames and animations from XML.
 * Handles different sprite naming conventions ('name', 'n', frame name) and path structures.
 * Ensures base frame and frames list are populated.
 *
 * @author Billy
 */
public class SpriteDataParser {

  private static final Logger LOG = LogManager.getLogger(SpriteDataParser.class);
  private final Properties fileStructureProperties;
  private final String pathToSpriteData;
  private final String pathToGraphicsData;

  /** Initializes the parser. */
  public SpriteDataParser() throws SpriteParseException {
    fileStructureProperties = loadProperties("oogasalad/file/fileStructure.properties");
    pathToSpriteData = trimSeparators(getRequiredProperty(fileStructureProperties, "path.to.game.data"));
    pathToGraphicsData = trimSeparators(getRequiredProperty(fileStructureProperties, "path.to.graphics.data"));
    LOG.debug("SpriteDataParser initialized. SpriteData Path Root: '{}', Graphics Path Root: '{}'",
        System.getProperty("user.dir") + File.separator + pathToSpriteData,
        System.getProperty("user.dir") + File.separator + pathToGraphicsData);
  }

  private String trimSeparators(String path) {
    if (path == null) return null; path = path.trim();
    if (path.startsWith(File.separator)) path = path.substring(1);
    if (path.endsWith(File.separator)) path = path.substring(0, path.length() - 1);
    return path;
  }

  /** Main method to get SpriteData based on a request. */
  public SpriteData getSpriteData(SpriteRequest request) throws SpriteParseException {

    String filePath = buildFilePath(request.gameName(), request.group(), request.type(), request.spriteFile());
    LOG.debug("Attempting to load sprite definition from calculated path: {}", filePath);

    Document doc = loadDocument(filePath);
    Element rootElement = doc.getDocumentElement();

    File spriteSheetImageFile = getSpriteSheetImageFile(rootElement, request.gameName());


    Element targetSpriteElement = getTargetSpriteElement(rootElement, request.spriteName());

    FrameData baseFrame = null;
    List<FrameData> frames = new ArrayList<>();
    List<AnimationData> animations = new ArrayList<>();
    String finalSpriteName = request.spriteName();

    if (targetSpriteElement != null) {

      LOG.debug("Found target sprite element for '{}'. Parsing its data.", request.spriteName());
      baseFrame = parseBaseImage(targetSpriteElement, request.spriteName());
      if (baseFrame != null) {
        finalSpriteName = baseFrame.name();
        frames = parseFrames(targetSpriteElement);
        animations = parseAnimations(targetSpriteElement);
      } else {
        LOG.error("Could not parse base frame attributes from sprite element '{}'.", request.spriteName());

        throw new SpriteParseException("Invalid base frame attributes for sprite: " + request.spriteName());
      }
    } else {

      LOG.warn("No <sprite> element found matching name/n='{}'. Checking for <frame> with that name...", request.spriteName());
      baseFrame = findFrameByName(rootElement, request.spriteName());
      if (baseFrame != null) {
        LOG.debug("Found matching <frame name='{}'>. Using it as base frame definition.", request.spriteName());
        finalSpriteName = baseFrame.name();

        frames.add(baseFrame);
      } else {

        throw new SpriteParseException(String.format("Neither <sprite> nor <frame> with name/n '%s' found in file %s", request.spriteName(), filePath));
      }
    }


    if (frames.isEmpty() && baseFrame != null) {
      LOG.debug("Adding base frame '{}' to initially empty frames list for sprite '{}'.", baseFrame.name(), finalSpriteName);
      frames.add(baseFrame);
    }

    return new SpriteData(finalSpriteName, spriteSheetImageFile, baseFrame, frames, animations);
  }

  /** Attempts to find a <frame> element by its name attribute within the document. */
  private FrameData findFrameByName(Element rootElement, String frameName) {

    NodeList frameNodes = rootElement.getElementsByTagName("frame");
    for (int i = 0; i < frameNodes.getLength(); i++) {
      if (frameNodes.item(i) instanceof Element frameElement) {
        if (frameName.equals(frameElement.getAttribute("name"))) {
          try { return parseFrameElement(frameElement); }
          catch (SpriteParseException e) { LOG.error("Error parsing frame element found by name '{}': {}", frameName, e.getMessage()); return null; }
        }
      }
    }
    return null;
  }

  /** Finds the specific sprite element within the XML document by checking 'name' then 'n' attribute. */
  private Element getTargetSpriteElement(Element rootElement, String spriteName) {

    NodeList spriteNodes = rootElement.getElementsByTagName("sprite");
    for (int i = 0; i < spriteNodes.getLength(); i++) {
      if (spriteNodes.item(i) instanceof Element spriteElement) {
        String nameAttr = spriteElement.getAttribute("name");
        if (spriteName.equals(nameAttr)) { return spriteElement; }
        if (nameAttr == null || nameAttr.isEmpty()) {
          String nAttr = spriteElement.getAttribute("n");
          if (spriteName.equals(nAttr)) {
            LOG.trace("Found sprite '{}' using 'n' attribute.", spriteName);
            return spriteElement;
          }
        }
      }
    }
    return null;
  }

  /** Parses the base image frame data from a sprite element's attributes. Uses 'name' or 'n'. */
  private FrameData parseBaseImage(Element spriteElement, String defaultName) throws SpriteParseException {

    String name = spriteElement.getAttribute("name");
    if (name == null || name.isEmpty()) {
      name = spriteElement.getAttribute("n");
      if (name == null || name.isEmpty()){
        name = defaultName;
        LOG.trace("Sprite element missing name/n attribute, using requested name '{}' for base frame.", name);
      } else {
        LOG.trace("Using 'n' attribute '{}' as name for base frame.", name);
      }
    }

    try {
      int x = Integer.parseInt(getAttributeOrDefault(spriteElement, "x", "0"));
      int y = Integer.parseInt(getAttributeOrDefault(spriteElement, "y", "0"));
      int width = Integer.parseInt(getAttributeOrDefault(spriteElement, "width", getAttributeOrDefault(spriteElement, "w", "0")));
      int height = Integer.parseInt(getAttributeOrDefault(spriteElement, "height", getAttributeOrDefault(spriteElement, "h", "0")));
      if (width <= 0 || height <= 0) {
        LOG.warn("Invalid dimensions (w={}, h={}) found for base sprite '{}'. Returning null base frame.", width, height, name);
        return null;
      }
      return new FrameData(name, x, y, width, height);
    } catch (NumberFormatException e) {
      throw new SpriteParseException("Invalid number format for sprite attributes for '" + name + "'", e);
    }
  }

  /** Helper to get attribute or default value */
  private String getAttributeOrDefault(Element element, String attributeName, String defaultValue) {

    return element.hasAttribute(attributeName) ? element.getAttribute(attributeName) : defaultValue;
  }

  /** Parses frame data from <frame> elements nested within a sprite element. */
  private List<FrameData> parseFrames(Element spriteElement) throws SpriteParseException {

    List<FrameData> frames = new ArrayList<>();
    NodeList frameNodes = spriteElement.getElementsByTagName("frame");
    for (int i = 0; i < frameNodes.getLength(); i++) {
      if (frameNodes.item(i) instanceof Element frameElement) {
        frames.add(parseFrameElement(frameElement));
      }
    }
    return frames;
  }

  /** Parses a single <frame> element. */
  private FrameData parseFrameElement(Element frameElement) throws SpriteParseException {

    String name = frameElement.getAttribute("name");
    if (name == null || name.isEmpty()) throw new SpriteParseException("Frame element missing required 'name' attribute.");
    try {
      int x = Integer.parseInt(getAttributeOrDefault(frameElement, "x", "0"));
      int y = Integer.parseInt(getAttributeOrDefault(frameElement, "y", "0"));
      int width = Integer.parseInt(getAttributeOrDefault(frameElement, "width", getAttributeOrDefault(frameElement, "w", "0")));
      int height = Integer.parseInt(getAttributeOrDefault(frameElement, "height", getAttributeOrDefault(frameElement, "h", "0")));
      if (width <= 0 || height <= 0) throw new SpriteParseException("Frame '" + name + "' has invalid dimensions.");
      return new FrameData(name, x, y, width, height);
    } catch (NumberFormatException e) {
      throw new SpriteParseException("Invalid number format for frame attributes for '" + name + "'", e);
    }
  }

  /** Parses animation data... */
  private List<AnimationData> parseAnimations(Element spriteElement) throws SpriteParseException {

    List<AnimationData> animations = new ArrayList<>();
    NodeList animationNodes = spriteElement.getElementsByTagName("animation");
    for (int i = 0; i < animationNodes.getLength(); i++) {
      if (animationNodes.item(i) instanceof Element animationElement) {
        String name = animationElement.getAttribute("name");
        String frameLenStr = animationElement.getAttribute("frameLen");
        String framesStr = animationElement.getAttribute("frames");
        if (name.isEmpty() || frameLenStr.isEmpty() || framesStr.isEmpty()) {
          throw new SpriteParseException("Animation element missing required attributes (name, frameLen, frames).");
        }
        try {
          double frameLen = Double.parseDouble(frameLenStr);
          List<String> frameNames = List.of(framesStr.split(",\\s*"));
          animations.add(new AnimationData(name, frameLen, frameNames));
        } catch (NumberFormatException e) {
          throw new SpriteParseException("Invalid frameLen format for animation '" + name + "'", e);
        }
      }
    }
    return animations;
  }


  /**
   * Constructs the full path to the sprite definition XML file using properties file paths.
   * CORRECTED: Reliably joins base path, game name, and the full relative spriteFile path.
   */
  private String buildFilePath(String gameName, String group, String type, String spriteFile) throws SpriteParseException {

    String baseSpritePath = System.getProperty("user.dir") + File.separator + pathToSpriteData;



    try {

      Path fullPathObject = Paths.get(baseSpritePath, gameName, spriteFile).normalize();
      String fullPath = fullPathObject.toString();

      LOG.trace("Constructed full XML path: {}", fullPath);
      return fullPath;

    } catch (InvalidPathException e) {
      LOG.error("Failed to construct valid path from components: Base='{}', Game='{}', SpriteFile='{}'", baseSpritePath, gameName, spriteFile, e);
      throw new SpriteParseException("Failed to construct valid path from components: " + e.getMessage(), e);
    }
  }


  /** Loads the XML document from the given file path. */
  private Document loadDocument(String filePath) throws SpriteParseException {

    try {
      File xmlFile = new File(filePath);
      LOG.debug("Attempting to parse XML document: {}", xmlFile.getAbsolutePath());
      if (!xmlFile.exists() || !xmlFile.isFile()) {
        throw new FileNotFoundException(filePath + " (No such file or directory)");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(xmlFile);
      LOG.debug("Successfully parsed XML document: {}", filePath);
      doc.getDocumentElement().normalize();
      return doc;
    } catch (FileNotFoundException e) {
      LOG.error("XML file not found at path: {}", filePath);
      throw new SpriteParseException(e.getMessage(), e);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new SpriteParseException("Failed to parse XML sprite definition file: " + filePath, e);
    } catch (Exception e) {
      throw new SpriteParseException("Unexpected error loading/parsing XML document: " + filePath, e);
    }
  }

  /** Extracts the image file path from the root element and resolves it. */
  private File getSpriteSheetImageFile(Element rootElement, String gameName) throws SpriteParseException {

    String imagePathAttr = rootElement.getAttribute("imagePath");
    if (imagePathAttr.isEmpty()) { throw new SpriteParseException("Missing 'imagePath' attribute in root element."); }
    String baseGraphicsPath = System.getProperty("user.dir") + File.separator + pathToGraphicsData + File.separator + gameName;
    String fullImagePath = Paths.get(baseGraphicsPath, imagePathAttr).normalize().toString();
    LOG.trace("Resolving image file path: {}", fullImagePath);
    File imageFile = new File(fullImagePath);
    if (!imageFile.exists() || !imageFile.isFile()) {
      LOG.error("Referenced sprite sheet image file not found: {}", fullImagePath);
      return new File("");
    }
    return imageFile;
  }


  /** Loads properties file from classpath. */
  private Properties loadProperties(String resourcePath) throws SpriteParseException {

    Properties props = new Properties();
    try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      if (input == null) { throw new SpriteParseException("Cannot find resource file: " + resourcePath); }
      props.load(input);
    } catch (IOException e) { throw new SpriteParseException("Could not load resource file: " + resourcePath, e); }
    return props;
  }

  /** Gets a required property or throws exception. */
  private String getRequiredProperty(Properties props, String key) throws SpriteParseException {

    String value = props.getProperty(key);
    if (value == null || value.trim().isEmpty()) { throw new SpriteParseException("Missing required property '" + key + "' in fileStructure.properties");}
    return value.trim();
  }
}