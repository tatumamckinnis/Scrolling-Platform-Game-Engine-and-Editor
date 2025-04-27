package oogasalad.editor.model.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.HitBoxDataParser;
import oogasalad.fileparser.PropertyParser;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;
import oogasalad.fileparser.records.SpriteRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Editor-specific BlueprintDataParser. Parses XML blueprint data into a map of
 * {@link BlueprintData} records. Uses the EditorSpriteParser for sprite data resolution. Handles
 * parser initialization internally.
 *
 * @author Tatum McKinnis
 */
public class EditorBlueprintParser {

  private static final Logger LOG = LogManager.getLogger(EditorBlueprintParser.class);
  private static final String DISPLAYED_PROPERTIES_TAG = "displayedProperties";
  private static final String PROPERTY_LIST_ATTR = "propertyList";

  private String groupName = "";
  private String gameName = "";
  private EditorSpriteParser mySpriteDataParser = null;
  private final HitBoxDataParser myHitBoxDataParser;
  private List<EventData> myEventDataList;
  private final PropertyParser propertyParser;

  private record BasicAttributes(int id, double velocityX, double velocityY, double rotation,
                                 boolean isFlipped, String originalType, String spriteName,
                                 String spriteFileAttr) {

  }

  /**
   * Constructs a new EditorBlueprintParser.
   */
  public EditorBlueprintParser() {
    propertyParser = new PropertyParser();
    myHitBoxDataParser = new HitBoxDataParser();
  }

  /**
   * Lazily initializes the EditorSpriteParser.
   */
  private boolean ensureSpriteParserInitialized() {
    if (mySpriteDataParser == null) {
      try {
        mySpriteDataParser = new EditorSpriteParser();
        LOG.info("EditorSpriteParser initialized successfully.");
      } catch (SpriteParseException e) {
        LOG.fatal("Failed to initialize EditorSpriteParser: {}", e.getMessage(), e);
        return false;
      }
    }
    return true;
  }


  /**
   * Extracts blueprint data from the provided XML root element.
   */
  public Map<Integer, BlueprintData> getBlueprintData(Element root, List<EventData> eventList)
      throws BlueprintParseException, HitBoxParseException, PropertyParsingException, EventParseException {

    if (!ensureSpriteParserInitialized()) {
      LOG.error("EditorSpriteParser could not be initialized. Sprite data will be missing.");
    }
    myEventDataList = eventList != null ? eventList : new ArrayList<>();

    NodeList gameNodes = root.getElementsByTagName("game");
    List<BlueprintData> gameObjectDataList = new ArrayList<>();
    for (int i = 0; i < gameNodes.getLength(); i++) {
      Node node = gameNodes.item(i);
      if (!(node instanceof Element)) {
        continue;
      }
      Element gameElement = (Element) node;
      gameName = gameElement.getAttribute("name");
      try {
        gameObjectDataList.addAll(parseByGame(gameElement));
      } catch (Exception e) {
        LOG.error("Failed to fully parse game element '{}': {}", gameName, e.getMessage(), e);
      }
    }
    return createBlueprintDataMap(gameObjectDataList);
  }

  /**
   * Creates a map from a list of BlueprintData records.
   */
  private Map<Integer, BlueprintData> createBlueprintDataMap(
      List<BlueprintData> blueprintDataList) {
    Map<Integer, BlueprintData> blueprintDataMap = new HashMap<>();
    for (BlueprintData data : blueprintDataList) {
      if (data != null) {
        blueprintDataMap.put(data.blueprintId(), data);
      }
    }
    return blueprintDataMap;
  }

  /**
   * Parses blueprint data within a <game> element.
   */
  private List<BlueprintData> parseByGame(Element gameNode)
      throws BlueprintParseException, HitBoxParseException, PropertyParsingException, EventParseException {
    NodeList objectGroupNodes = gameNode.getElementsByTagName("objectGroup");
    List<BlueprintData> gameObjectsList = new ArrayList<>();
    for (int i = 0; i < objectGroupNodes.getLength(); i++) {
      Node node = objectGroupNodes.item(i);
      if (!(node instanceof Element)) {
        continue;
      }
      Element objectGroup = (Element) node;
      groupName = objectGroup.getAttribute("name");
      gameObjectsList.addAll(parseByObjectGroup(objectGroup));
    }
    return gameObjectsList;
  }

  /**
   * Parses blueprint data within an <objectGroup> element.
   */
  private List<BlueprintData> parseByObjectGroup(Element objectGroupNode) {
    List<BlueprintData> results = new ArrayList<>();
    NodeList nodes = objectGroupNode.getElementsByTagName("object");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node n = nodes.item(i);
      if (n instanceof Element elt) {
        safeParseBlueprint(elt, results);
      }
    }
    return results;
  }

  private void safeParseBlueprint(Element elt, List<BlueprintData> out) {
    try {
      BlueprintData bp = parseGameObjectData(elt);
      if (bp != null) {
        out.add(bp);
      }
    } catch (Exception e) {
      String id = elt.hasAttribute("id") ? elt.getAttribute("id") : "[unknown ID]";
      LOG.error("Skipping object id={} due to parsing error within group '{}': {}", id, groupName,
          e.getMessage(), e);
    }
  }

  /**
   * Parses a single game object node into a BlueprintData record. Uses the EditorSpriteParser and
   * passes the FULL relative path.
   *
   * @param gameObjectNode
   * @return
   */
  private BlueprintData parseGameObjectData(Element gameObjectNode)
      throws BlueprintParseException, HitBoxParseException, PropertyParsingException {

    // 1) read all basic attrs in one place
    BasicAttributes a = extractBasicAttributes(gameObjectNode);

    // 2) resolve SpriteData (with all the try/catch fallback logic)
    SpriteData sprite = resolveSpriteData(a);

    // 3) parse the rest of the pieces
    HitBoxData hitBoxData = myHitBoxDataParser.getHitBoxData(gameObjectNode);
    List<EventData> events = getAssociatedEventData(gameObjectNode);
    Map<String, Double> doubleProps = propertyParser.parseDoubleProperties(gameObjectNode,
        "doubleProperties", "property");
    Map<String, String> stringProps = propertyParser.parseStringProperties(gameObjectNode,
        "stringProperties", "property");
    List<String> displayedProps = getDisplayedProperties(gameObjectNode);

    LOG.debug(
        "Creating BlueprintData for id={}, type={}, sprite='{}', imageFile='{}', baseFrame={}, frames={}, animations={}",
        a.id, a.originalType, sprite.name(),
        sprite.spriteFile() != null ? sprite.spriteFile().getPath() : "null",
        sprite.baseFrame() != null ? sprite.baseFrame().name() : "null", sprite.frames().size(),
        sprite.animations().size());

    return new BlueprintData(a.id, a.velocityX, a.velocityY, a.rotation, a.isFlipped, gameName,
        this.groupName, a.originalType, sprite, hitBoxData, events, stringProps, doubleProps,
        displayedProps);
  }

  /** Pulls out all the primitive attrs and throws immediately on bad format. */
  private BasicAttributes extractBasicAttributes(Element o) {
    int id             = Integer.parseInt(o.getAttribute("id"));
    double vx          = Double.parseDouble(o.getAttribute("velocityX"));
    double vy          = Double.parseDouble(o.getAttribute("velocityY"));
    double rot         = Double.parseDouble(o.getAttribute("rotation"));
    boolean flipped    = Boolean.parseBoolean(o.getAttribute("flipped"));
    String type        = o.getAttribute("type");
    String name        = o.getAttribute("spriteName");
    String fileAttr    = o.getAttribute("spriteFile");
    return new BasicAttributes(id, vx, vy, rot, flipped, type, name, fileAttr);
  }

  private SpriteData resolveSpriteData(BasicAttributes a) {
    if (mySpriteDataParser == null
        || gameName == null || gameName.isBlank()
        || a.spriteName.isBlank()
        || a.spriteFileAttr.isBlank()) {
      LOG.warn("Skipping sprite parsing for blueprint {} (missing parser/info)", a.id);
      return createDefaultSpriteData(a.spriteName);
    }

    SpriteRequest req = new SpriteRequest(
        gameName, groupName, a.originalType, a.spriteName, a.spriteFileAttr);
    try {
      SpriteData sd = mySpriteDataParser.getSpriteData(req);
      if (sd == null
          || sd.baseFrame() == null
          || sd.spriteFile() == null
          || sd.spriteFile().getPath().isEmpty()) {
        LOG.error("Incomplete SpriteData for {} → using default", req);
        return createDefaultSpriteData(a.spriteName);
      }
      return sd;
    } catch (Exception e) {
      LOG.error("Error fetching sprite for {}: {} → using default",
          req, e.getMessage());
      return createDefaultSpriteData(a.spriteName);
    }
  }

  /**
   * Creates a default/empty SpriteData record for fallback
   */
  private SpriteData createDefaultSpriteData(String name) {
    String safeName = (name != null && !name.trim().isEmpty()) ? name : "Unknown";
    return new SpriteData(safeName, new File(""), null, Collections.emptyList(),
        Collections.emptyList());
  }

  /**
   * Creates the event data list...
   */
  private List<EventData> getAssociatedEventData(Element gameObjectNode) {
    String eventIds = gameObjectNode.getAttribute("eventIDs");
    List<EventData> eventDataList = new ArrayList<>();
    if (eventIds != null && !eventIds.trim().isEmpty()) {
      String[] eventIdArray = eventIds.split(",");
      for (String eventId : eventIdArray) {
        String trimmedId = eventId.trim();
        if (!trimmedId.isEmpty()) {
          EventData event = findEventById(trimmedId);
          if (event != null) {
            eventDataList.add(event);
          } else {
            LOG.warn("EventData not found for eventID: '{}' referenced in blueprint.", trimmedId);
          }
        }
      }
    }
    return eventDataList;
  }

  /**
   * Retrieves the EventData matching the provided event ID...
   */
  private EventData findEventById(String id) {
    if (myEventDataList == null) {
      return null;
    }
    for (EventData eventData : myEventDataList) {
      if (Objects.equals(id, eventData.eventId())) {
        return eventData;
      }
    }
    return null;
  }

  /**
   * Retrieves the displayed properties list...
   */
  private List<String> getDisplayedProperties(Element gameObjectNode) {
    NodeList nodes = gameObjectNode.getElementsByTagName(DISPLAYED_PROPERTIES_TAG);
    if (nodes.getLength() > 0 && nodes.item(0) instanceof Element) {
      Element displayedPropertiesElement = (Element) nodes.item(0);
      if (displayedPropertiesElement.hasAttribute(PROPERTY_LIST_ATTR)) {
        String list = displayedPropertiesElement.getAttribute(PROPERTY_LIST_ATTR);
        if (list != null && !list.trim().isEmpty()) {
          return List.of(list.split(","));
        }
      }
    }
    return Collections.emptyList();
  }
}