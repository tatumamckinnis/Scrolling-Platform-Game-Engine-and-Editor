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
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;
import oogasalad.fileparser.records.SpriteRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Editor-specific BlueprintDataParser.
 * Parses XML blueprint data into a map of {@link BlueprintData} records.
 * Uses the EditorSpriteParser for sprite data resolution.
 * Handles parser initialization internally.
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

  /** Constructs a new EditorBlueprintParser. */
  public EditorBlueprintParser() {
    propertyParser = new PropertyParser();
    myHitBoxDataParser = new HitBoxDataParser();
  }

  /** Lazily initializes the EditorSpriteParser. */
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


  /** Extracts blueprint data from the provided XML root element. */
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
      if (!(node instanceof Element)) { continue; }
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

  /** Creates a map from a list of BlueprintData records. */
  private Map<Integer, BlueprintData> createBlueprintDataMap(List<BlueprintData> blueprintDataList) {
    Map<Integer, BlueprintData> blueprintDataMap = new HashMap<>();
    for (BlueprintData data : blueprintDataList) {
      if(data != null) blueprintDataMap.put(data.blueprintId(), data);
    }
    return blueprintDataMap;
  }

  /** Parses blueprint data within a <game> element. */
  private List<BlueprintData> parseByGame(Element gameNode)
      throws BlueprintParseException, HitBoxParseException, PropertyParsingException, EventParseException {
    NodeList objectGroupNodes = gameNode.getElementsByTagName("objectGroup");
    List<BlueprintData> gameObjectsList = new ArrayList<>();
    for (int i = 0; i < objectGroupNodes.getLength(); i++) {
      Node node = objectGroupNodes.item(i);
      if (!(node instanceof Element)) { continue; }
      Element objectGroup = (Element) node;
      groupName = objectGroup.getAttribute("name");
      gameObjectsList.addAll(parseByObjectGroup(objectGroup));
    }
    return gameObjectsList;
  }

  /** Parses blueprint data within an <objectGroup> element. */
  private List<BlueprintData> parseByObjectGroup(Element objectGroupNode)
      throws BlueprintParseException, HitBoxParseException, PropertyParsingException, EventParseException {
    List<BlueprintData> gameObjectsGroupList = new ArrayList<>();
    NodeList gameObjectNodes = objectGroupNode.getElementsByTagName("object");
    for (int i = 0; i < gameObjectNodes.getLength(); i++) {
      Node node = gameObjectNodes.item(i);
      if (!(node instanceof Element)) { continue; }
      Element gameObjectNode = (Element) node;
      try {
        BlueprintData bpData = parseGameObjectData(gameObjectNode);
        if(bpData != null) gameObjectsGroupList.add(bpData);
      } catch (Exception e) {
        String idAttr = gameObjectNode.hasAttribute("id") ? gameObjectNode.getAttribute("id") : "[unknown ID]";
        LOG.error("Skipping object id={} due to parsing error within group '{}': {}", idAttr, groupName, e.getMessage(), e);
      }
    }
    return gameObjectsGroupList;
  }

  /**
   * Parses a single game object node into a BlueprintData record.
   * Uses the EditorSpriteParser and passes the FULL relative path.
   */
  private BlueprintData parseGameObjectData(Element gameObjectNode)
      throws BlueprintParseException, HitBoxParseException, PropertyParsingException {

    int id = -1;
    String originalType = "";
    String spriteName = "";
    String currentGroupName = this.groupName;

    try {
      id = Integer.parseInt(gameObjectNode.getAttribute("id"));
      double velocityX = Double.parseDouble(gameObjectNode.getAttribute("velocityX"));
      double velocityY = Double.parseDouble(gameObjectNode.getAttribute("velocityY"));
      double rotation = Double.parseDouble(gameObjectNode.getAttribute("rotation"));
      boolean isFlipped = Boolean.parseBoolean(gameObjectNode.getAttribute("flipped"));
      originalType = gameObjectNode.getAttribute("type");
      spriteName = gameObjectNode.getAttribute("spriteName");
      String spriteFileAttr = gameObjectNode.getAttribute("spriteFile");

      SpriteData spriteData = null;

      if (mySpriteDataParser != null &&
          gameName != null && !gameName.isEmpty() &&
          spriteName != null && !spriteName.isEmpty() &&
          spriteFileAttr != null && !spriteFileAttr.isEmpty()) {

        SpriteRequest request = new SpriteRequest(gameName, currentGroupName, originalType, spriteName, spriteFileAttr);
        LOG.debug("Attempting sprite request for blueprint {} with: {}", id, request);

        try {
          spriteData = mySpriteDataParser.getSpriteData(request);

          if (spriteData == null || spriteData.baseFrame() == null || spriteData.spriteFile() == null || spriteData.spriteFile().getPath().isEmpty()) {
            LOG.error("EditorSpriteParser returned invalid/incomplete data for request {}. Creating default.", request);
            spriteData = createDefaultSpriteData(spriteName);
          }

        } catch (SpriteParseException spe) {
          LOG.error("Failed to parse sprite data for blueprint {} (Request: {}): {}", id, request, spe.getMessage());
          spriteData = createDefaultSpriteData(spriteName);
        } catch (Exception e) {
          LOG.error("Unexpected error getting sprite data for blueprint {} (Request: {}): {}", id, request, e.getMessage(), e);
          spriteData = createDefaultSpriteData(spriteName);
        }
      } else {
        if (mySpriteDataParser == null) { LOG.error("Skipping sprite data parsing for blueprint {} because EditorSpriteParser failed.", id);}
        else { LOG.warn("Skipping sprite data parsing for blueprint {} due to missing info...", id); }
        spriteData = createDefaultSpriteData(spriteName != null ? spriteName : "Unknown_" + id);
      }

      HitBoxData hitBoxData = myHitBoxDataParser.getHitBoxData(gameObjectNode);
      List<EventData> eventDataList = getAssociatedEventData(gameObjectNode);
      Map<String, Double> doubleProperties = propertyParser.parseDoubleProperties(gameObjectNode, "doubleProperties", "property");
      Map<String, String> stringProperties = propertyParser.parseStringProperties(gameObjectNode, "stringProperties", "property");
      List<String> displayedProperties = getDisplayedProperties(gameObjectNode);

      LOG.debug("Creating BlueprintData for id={}, type={}, sprite='{}', imageFile='{}', baseFrame={}, frames={}, animations={}",
          id, originalType, spriteData.name(),
          spriteData.spriteFile() != null ? spriteData.spriteFile().getPath() : "null",
          spriteData.baseFrame() != null ? spriteData.baseFrame().name() : "null",
          spriteData.frames().size(), spriteData.animations().size());

      return new BlueprintData(
          id, velocityX, velocityY, rotation, isFlipped,
          gameName, currentGroupName, originalType,
          spriteData,
          hitBoxData, eventDataList,
          stringProperties, doubleProperties, displayedProperties
      );
    } catch (NumberFormatException e) {
      throw new BlueprintParseException("Blueprint attribute has invalid number format for id=" + id + ": " + e.getMessage(), e);
    } catch (HitBoxParseException | PropertyParsingException e) {
      throw e;
    } catch (Exception e) {
      throw new BlueprintParseException("Unexpected error parsing blueprint object id=" + id + ": " + e.getMessage(), e);
    }
  }

  /** Creates a default/empty SpriteData record for fallback */
  private SpriteData createDefaultSpriteData(String name) {
    String safeName = (name != null && !name.trim().isEmpty()) ? name : "Unknown";
    return new SpriteData(safeName, new File(""), null, Collections.emptyList(), Collections.emptyList());
  }

  /** Creates the event data list... */
  private List<EventData> getAssociatedEventData(Element gameObjectNode) {
    String eventIds = gameObjectNode.getAttribute("eventIDs");
    List<EventData> eventDataList = new ArrayList<>();
    if (eventIds != null && !eventIds.trim().isEmpty()) {
      String[] eventIdArray = eventIds.split(",");
      for (String eventId : eventIdArray) {
        String trimmedId = eventId.trim();
        if (!trimmedId.isEmpty()) {
          EventData event = findEventById(trimmedId);
          if (event != null) { eventDataList.add(event); }
          else { LOG.warn("EventData not found for eventID: '{}' referenced in blueprint.", trimmedId); }
        }
      }
    }
    return eventDataList;
  }

  /** Retrieves the EventData matching the provided event ID... */
  private EventData findEventById(String id) {
    if (myEventDataList == null) return null;
    for (EventData eventData : myEventDataList) {
      if (Objects.equals(id, eventData.eventId())) { return eventData; }
    }
    return null;
  }

  /** Retrieves the displayed properties list... */
  private List<String> getDisplayedProperties(Element gameObjectNode) {
    NodeList nodes = gameObjectNode.getElementsByTagName(DISPLAYED_PROPERTIES_TAG);
    if (nodes.getLength() > 0 && nodes.item(0) instanceof Element) {
      Element displayedPropertiesElement = (Element) nodes.item(0);
      if (displayedPropertiesElement.hasAttribute(PROPERTY_LIST_ATTR)) {
        String list = displayedPropertiesElement.getAttribute(PROPERTY_LIST_ATTR);
        if (list != null && !list.trim().isEmpty()) { return List.of(list.split(",")); }
      }
    }
    return Collections.emptyList();
  }
}