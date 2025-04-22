package oogasalad.fileparser;

import java.io.File;
import java.nio.file.Paths;
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
 * The BlueprintDataParser class is responsible for parsing XML blueprint data into a map of
 * {@link BlueprintData} records. Correctly parses associated SpriteData, ensuring frame data exists.
 * Handles SpriteDataParser initialization internally.
 *
 * @author Billy
 */
public class BlueprintDataParser {

  private static final Logger LOG = LogManager.getLogger(BlueprintDataParser.class);
  private static final String DISPLAYED_PROPERTIES_TAG = "displayedProperties";
  private static final String PROPERTY_LIST_ATTR = "propertyList";

  private String groupName = "";
  private String gameName = "";
  private SpriteDataParser mySpriteDataParser = null;
  private final HitBoxDataParser myHitBoxDataParser;
  private List<EventData> myEventDataList;
  private final PropertyParser propertyParser;

  /**
   * Constructs a new BlueprintDataParser and initializes some required parsers.
   * SpriteDataParser is initialized lazily on first use.
   */
  public BlueprintDataParser() {
    propertyParser = new PropertyParser();
    myHitBoxDataParser = new HitBoxDataParser();
  }

  /**
   * Lazily initializes the SpriteDataParser if it hasn't been already.
   * @return true if the parser is ready, false if initialization failed.
   */
  private boolean ensureSpriteParserInitialized() {
    if (mySpriteDataParser == null) {
      try {
        mySpriteDataParser = new SpriteDataParser();
        LOG.info("SpriteDataParser initialized successfully.");
      } catch (SpriteParseException e) {
        LOG.fatal("Failed to initialize SpriteDataParser needed by BlueprintDataParser: {}", e.getMessage(), e);
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
      LOG.error("SpriteDataParser could not be initialized. Sprite data will not be loaded for blueprints.");

    }

    myEventDataList = eventList;
    NodeList gameNodes = root.getElementsByTagName("game");
    List<BlueprintData> gameObjectDataList = new ArrayList<>();
    for (int i = 0; i < gameNodes.getLength(); i++) {
      Node node = gameNodes.item(i);
      if (!(node instanceof Element)) { continue; }
      Element gameElement = (Element) node;
      gameName = gameElement.getAttribute("name");
      try {
        gameObjectDataList.addAll(parseByGame(gameElement));
      } catch (BlueprintParseException | HitBoxParseException | PropertyParsingException | EventParseException e) {
        LOG.error("Failed to parse game element '{}': {}", gameName, e.getMessage());
      }
    }
    return createBlueprintDataMap(gameObjectDataList);
  }

  /** Creates a map from a list of BlueprintData records. */
  private Map<Integer, BlueprintData> createBlueprintDataMap(List<BlueprintData> blueprintDataList) {
    Map<Integer, BlueprintData> blueprintDataMap = new HashMap<>();
    for (BlueprintData data : blueprintDataList) {
      blueprintDataMap.put(data.blueprintId(), data);
    }
    return blueprintDataMap;
  }

  /** Parses the blueprint data contained within a <game> element. */
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

  /** Parses the blueprint data contained within an <objectGroup> element. */
  private List<BlueprintData> parseByObjectGroup(Element objectGroupNode)
      throws BlueprintParseException, HitBoxParseException, PropertyParsingException, EventParseException {
    List<BlueprintData> gameObjectsGroupList = new ArrayList<>();
    NodeList gameObjectNodes = objectGroupNode.getElementsByTagName("object");
    for (int i = 0; i < gameObjectNodes.getLength(); i++) {
      Node node = gameObjectNodes.item(i);
      if (!(node instanceof Element)) { continue; }
      Element gameObjectNode = (Element) node;
      try {
        gameObjectsGroupList.add(parseGameObjectData(gameObjectNode));
      } catch (BlueprintParseException | HitBoxParseException | PropertyParsingException e) {
        LOG.error("Failed to parse object element within group '{}': {}", groupName, e.getMessage());
      } catch (Exception e) {
        LOG.error("Unexpected error parsing object element within group '{}': {}", groupName, e.getMessage(), e);
      }
    }
    return gameObjectsGroupList;
  }

  /**
   * Parses a single game object node into a BlueprintData record.
   * Ensures associated SpriteData is fully parsed and populated if possible.
   * Adjusts SpriteRequest based on group name structure.
   */
  private BlueprintData parseGameObjectData(Element gameObjectNode)
      throws BlueprintParseException, HitBoxParseException, PropertyParsingException {

    int id = -1;
    try {
      id = Integer.parseInt(gameObjectNode.getAttribute("id"));
      double velocityX = Double.parseDouble(gameObjectNode.getAttribute("velocityX"));
      double velocityY = Double.parseDouble(gameObjectNode.getAttribute("velocityY"));
      double rotation = Double.parseDouble(gameObjectNode.getAttribute("rotation"));
      boolean isFlipped = Boolean.parseBoolean(gameObjectNode.getAttribute("flipped"));
      String type = gameObjectNode.getAttribute("type");
      String spriteName = gameObjectNode.getAttribute("spriteName");
      String spriteFileAttr = gameObjectNode.getAttribute("spriteFile");


      SpriteData spriteData = null;
      if (mySpriteDataParser != null &&
          gameName != null && !gameName.isEmpty() &&
          spriteName != null && !spriteName.isEmpty() &&
          spriteFileAttr != null && !spriteFileAttr.isEmpty()) {


        String typeForRequest = type;

        if (groupName != null && (groupName.contains("/") || groupName.contains("\\"))) {

          LOG.trace("Group name '{}' contains path separator. Setting type=null for SpriteRequest path building.", groupName);
          typeForRequest = null;
        }

        String spriteFileNameForRequest = Paths.get(spriteFileAttr).getFileName().toString();

        SpriteRequest request = new SpriteRequest(gameName, groupName, typeForRequest, spriteName, spriteFileNameForRequest);
        LOG.debug("Requesting sprite data for blueprint {} with request: {}", id, request);

        try {
          spriteData = mySpriteDataParser.getSpriteData(request);

          if (spriteData != null) {
            FrameData baseFrame = spriteData.baseFrame();
            List<FrameData> frames = new ArrayList<>(spriteData.frames());

            if (baseFrame == null) {
              LOG.warn("SpriteDataParser did not return a baseFrame record for {}. Sprite might not display correctly.", spriteName);

              FrameData foundBase = null;
              for(FrameData frame : frames){
                if(frame.name().equals(spriteName)){
                  foundBase = frame;
                  break;
                }
              }
              if (foundBase != null) {
                LOG.debug("Setting base frame heuristically to frame found by name: '{}'", foundBase.name());

                spriteData = new SpriteData(spriteData.name(), spriteData.spriteFile(), foundBase, frames, spriteData.animations());
                baseFrame = foundBase;
              } else if (!frames.isEmpty()) {

                foundBase = frames.get(0);
                LOG.warn("No frame matching sprite name '{}' found. Setting base frame to first frame: '{}'", spriteName, foundBase.name());
                spriteData = new SpriteData(spriteData.name(), spriteData.spriteFile(), foundBase, frames, spriteData.animations());
                baseFrame = foundBase;
              } else {
                LOG.error("No base frame record and no frames found for sprite '{}'. Creating empty sprite data.", spriteName);
                spriteData = createDefaultSpriteData(spriteName);
              }
            }

            if (spriteData.frames().isEmpty() && spriteData.baseFrame() != null) {
              LOG.debug("SpriteData for {} had no explicit <frames>, adding base frame '{}' to frames list.", spriteName, spriteData.baseFrame().name());
              frames.add(spriteData.baseFrame());
              spriteData = new SpriteData(spriteData.name(), spriteData.spriteFile(), spriteData.baseFrame(), frames, spriteData.animations());
            }

          } else {
            LOG.error("SpriteDataParser returned null for request {}. Creating empty sprite data for blueprint {}", request, id);
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
        if (mySpriteDataParser == null) {
          LOG.error("Skipping sprite data parsing for blueprint {} because SpriteDataParser failed to initialize.", id);
        } else {
          LOG.warn("Skipping sprite data parsing for blueprint {} due to missing info (game='{}', spriteName='{}', spriteFile='{}')",
              id, gameName, spriteName, spriteFileAttr);
        }
        spriteData = createDefaultSpriteData(spriteName != null ? spriteName : "Unknown_" + id);
      }


      HitBoxData hitBoxData = myHitBoxDataParser.getHitBoxData(gameObjectNode);
      List<EventData> eventDataList = getAssociatedEventData(gameObjectNode);
      Map<String, Double> doubleProperties = propertyParser.parseDoubleProperties(gameObjectNode, "doubleProperties", "property");
      Map<String, String> stringProperties = propertyParser.parseStringProperties(gameObjectNode, "stringProperties", "property");
      List<String> displayedProperties = getDisplayedProperties(gameObjectNode);


      LOG.debug("Creating BlueprintData for id={}, type={}, sprite='{}', imageFile='{}', baseFrame={}, frames={}, animations={}",
          id, type, spriteData.name(),
          spriteData.spriteFile() != null ? spriteData.spriteFile().getPath() : "null",
          spriteData.baseFrame() != null ? spriteData.baseFrame().name() : "null",
          spriteData.frames().size(), spriteData.animations().size());


      return new BlueprintData(
          id, velocityX, velocityY, rotation, isFlipped,
          gameName, groupName, type,
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

  /** Creates the event data list by retrieving events associated with the game object's eventIDs. */
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

  /** Retrieves the EventData matching the provided event ID from the master event list. */
  private EventData findEventById(String id) {
    if (myEventDataList == null) return null;
    for (EventData eventData : myEventDataList) {
      if (Objects.equals(id, eventData.eventId())) {
        return eventData;
      }
    }
    return null;
  }

  /** Retrieves the displayed properties list from the XML node. */
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