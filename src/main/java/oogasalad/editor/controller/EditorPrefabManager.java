package oogasalad.editor.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.loader.EditorBlueprintParser;
import oogasalad.editor.model.saver.BlueprintBuilder;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.BlueprintDataParser;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Manages the creation, saving, and loading of prefabs (Blueprints) to/from XML files.
 * @author Tatum McKinnis
 */
public class EditorPrefabManager {

  private static final Logger LOG = LogManager.getLogger(EditorPrefabManager.class);
  private static final String PREFAB_FILENAME = "prefabs.xml";
  private static final String PREFAB_ROOT_ELEMENT = "prefabs";
  private static final String GAME_ELEMENT = "game";
  private static final String GROUP_ELEMENT = "objectGroup";
  private static final String OBJECT_ELEMENT = "object";
  private static final String PROPERTIES_ELEMENT = "properties";
  private static final String STRING_PROPS_ELEMENT = "stringProperties";
  private static final String DOUBLE_PROPS_ELEMENT = "doubleProperties";
  private static final String PROPERTY_ELEMENT = "property";


  private final EditorDataAPI editorDataAPI;
  private final EditorListenerNotifier notifier;

  /**
   * Constructs an EditorPrefabManager.
   *
   * @param editorDataAPI The data API to access game directory path.
   * @param notifier      The notifier to signal prefab changes and errors.
   */
  public EditorPrefabManager(EditorDataAPI editorDataAPI, EditorListenerNotifier notifier) {
    this.editorDataAPI = editorDataAPI;
    this.notifier = notifier;
  }

  /**
   * Creates a prefab (BlueprintData) from an existing EditorObject and saves it
   * to the prefabs XML file in the current game directory. Handles loading existing
   * prefabs, checking for duplicates, assigning a new ID, and saving the updated list.
   *
   * @param objectToSave The EditorObject to convert and save as a prefab.
   */
  public void saveAsPrefab(EditorObject objectToSave) {
    Objects.requireNonNull(objectToSave, "Object to save as prefab cannot be null");
    LOG.info("Processing request to save object {} as prefab", objectToSave.getId());
    try {
      BlueprintData newPrefabData = BlueprintBuilder.fromEditorObject(objectToSave);

      String prefabFilePath = getPrefabFilePath();
      if (prefabFilePath == null) {
        return;
      }
      File prefabFile = new File(prefabFilePath);

      Map<Integer, BlueprintData> existingPrefabs = loadPrefabsFromFile(prefabFilePath);

      if (isDuplicatePrefab(newPrefabData, existingPrefabs)) {
        LOG.warn("Duplicate prefab definition found based on content for type: {}. Skipping save.", newPrefabData.type());
        notifier.notifyErrorOccurred("Prefab with similar properties already exists. Not saved.");
        return;
      }

      BlueprintData finalPrefabData = assignNewPrefabId(newPrefabData, existingPrefabs);
      existingPrefabs.put(finalPrefabData.blueprintId(), finalPrefabData);

      savePrefabsToFile(prefabFile, existingPrefabs);

      LOG.info("Prefab '{}' (ID: {}) saved to {}", finalPrefabData.type(), finalPrefabData.blueprintId(), prefabFilePath);
      notifier.notifyPrefabsChanged();

    } catch (Exception e) {
      LOG.error("Failed to save object {} as prefab: {}", objectToSave.getId(), e.getMessage(), e);
      notifier.notifyErrorOccurred("Failed to save as prefab: " + e.getMessage());
    }
  }

  /**
   * Retrieves the full path to the prefab XML file within the current game directory.
   *
   * @return The absolute path string, or null if the game directory is not set.
   */
  private String getPrefabFilePath() {
    String currentGameDirectory = editorDataAPI.getCurrentGameDirectoryPath();
    if (currentGameDirectory == null || currentGameDirectory.isEmpty()) {
      LOG.error("Cannot access prefabs: Current game directory path is not set.");
      notifier.notifyErrorOccurred("Cannot access prefabs: Game path unknown.");
      return null;
    }
    return Paths.get(currentGameDirectory, PREFAB_FILENAME).toString();
  }

  /** Loads BlueprintData records using the EditorBlueprintParser. */
  private Map<Integer, BlueprintData> loadPrefabsFromFile(String filePath) {
    Map<Integer, BlueprintData> prefabs = new HashMap<>();
    File file = new File(filePath);
    if (!file.exists()) { /* ... handle file not found ... */ }
    LOG.debug("Attempting to load prefabs from: {}", filePath);

    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(file);
      doc.getDocumentElement().normalize();
      Element rootElement = doc.getDocumentElement();
      if (!rootElement.getNodeName().equalsIgnoreCase("prefabs")) { /* ... handle wrong root ... */ }

      // FIX: Use the editor's blueprint parser
      EditorBlueprintParser parser = new EditorBlueprintParser();
      // Pass an empty list or load actual events if needed by parser/prefabs
      prefabs = parser.getBlueprintData(rootElement, new ArrayList<>());

      LOG.info("Successfully parsed {} prefabs using EditorBlueprintParser from {}", prefabs.size(), filePath);

    } catch (Exception e) {
      LOG.error("Failed to parse prefab file {} using EditorBlueprintParser: {}", filePath, e.getMessage(), e);
    }
    return prefabs;
  }

  private boolean isDuplicatePrefab(BlueprintData newPrefabData, Map<Integer, BlueprintData> existingPrefabs) {
    return existingPrefabs.values().stream()
        .anyMatch(existing -> existing.equals(newPrefabData));
  }

  private BlueprintData assignNewPrefabId(BlueprintData newPrefabData, Map<Integer, BlueprintData> existingPrefabs) {
    int maxId = existingPrefabs.keySet().stream().max(Integer::compare).orElse(0);
    return newPrefabData.withId(maxId + 1);
  }

  private void savePrefabsToFile(File prefabFile, Map<Integer, BlueprintData> prefabsMap)
      throws ParserConfigurationException, IOException, TransformerException {
    LOG.debug("Saving {} prefabs to {}", prefabsMap.size(), prefabFile.getPath());

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.newDocument();

    Element rootElement = doc.createElement(PREFAB_ROOT_ELEMENT);
    doc.appendChild(rootElement);

    Map<String, Map<String, List<BlueprintData>>> groupedByGame = groupPrefabs(prefabsMap);
    buildGameAndGroupElements(doc, rootElement, groupedByGame);
    writeXmlToFile(doc, prefabFile);

    LOG.info("Successfully saved prefabs to {}", prefabFile.getPath());
  }

  private Map<String, Map<String, List<BlueprintData>>> groupPrefabs(Map<Integer, BlueprintData> prefabsMap) {
    return prefabsMap.values().stream()
        .collect(Collectors.groupingBy(BlueprintData::gameName,
            Collectors.groupingBy(BlueprintData::group)));
  }

  private void buildGameAndGroupElements(Document doc, Element rootElement, Map<String, Map<String, List<BlueprintData>>> groupedByGame) {
    for (Map.Entry<String, Map<String, List<BlueprintData>>> gameEntry : groupedByGame.entrySet()) {
      Element gameElement = doc.createElement(GAME_ELEMENT);
      gameElement.setAttribute("name", gameEntry.getKey());
      rootElement.appendChild(gameElement);

      for (Map.Entry<String, List<BlueprintData>> groupEntry : gameEntry.getValue().entrySet()) {
        Element groupElement = doc.createElement(GROUP_ELEMENT);
        groupElement.setAttribute("name", groupEntry.getKey());
        gameElement.appendChild(groupElement);

        for (BlueprintData bp : groupEntry.getValue()) {
          Element objectElement = createObjectElement(doc, bp);
          groupElement.appendChild(objectElement);
        }
      }
    }
  }

  private void writeXmlToFile(Document doc, File file) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(file);
    transformer.transform(source, result);
  }

  private Element createObjectElement(Document doc, BlueprintData bp) {
    Element objectElement = doc.createElement(OBJECT_ELEMENT);
    setBasicObjectAttributes(objectElement, bp);
    setSpriteAttributes(objectElement, bp.spriteData());
    setHitboxAttributes(objectElement, bp.hitBoxData());
    setEventIdsAttribute(objectElement, bp.eventDataList());

    Element propertiesElement = createPropertiesElement(doc, bp);
    objectElement.appendChild(propertiesElement);

    return objectElement;
  }

  private void setBasicObjectAttributes(Element element, BlueprintData bp) {
    element.setAttribute("id", String.valueOf(bp.blueprintId()));
    element.setAttribute("type", bp.type());
    element.setAttribute("velocityX", String.format("%.2f", bp.velocityX()));
    element.setAttribute("velocityY", String.format("%.2f", bp.velocityY()));
    element.setAttribute("rotation", String.format("%.2f", bp.rotation()));
  }

  private void setSpriteAttributes(Element element, SpriteData sprite) {
    if (sprite != null && sprite.spriteFile() != null) {
      element.setAttribute("spriteName", Objects.toString(sprite.name(), ""));
      element.setAttribute("spriteFile", Objects.toString(sprite.spriteFile().getPath(), ""));
    } else {
      element.setAttribute("spriteName", "");
      element.setAttribute("spriteFile", "");
    }
  }

  private void setHitboxAttributes(Element element, HitBoxData hitbox) {
    if (hitbox != null) {
      element.setAttribute("hitBoxWidth", String.valueOf(hitbox.hitBoxWidth()));
      element.setAttribute("hitBoxHeight", String.valueOf(hitbox.hitBoxHeight()));
      element.setAttribute("hitBoxShape", Objects.toString(hitbox.shape(), "RECTANGLE"));
      element.setAttribute("spriteDx", String.valueOf(hitbox.spriteDx()));
      element.setAttribute("spriteDy", String.valueOf(hitbox.spriteDy()));
    } else {
      LOG.warn("No hitbox data found for blueprint ID {}, omitting hitbox attributes.", element.getAttribute("id"));
    }
  }

  private void setEventIdsAttribute(Element element, List<EventData> eventDataList) {
    String eventIdsString = eventDataList.stream()
        .map(EventData::eventId)
        .filter(Objects::nonNull)
        .filter(id -> !id.isEmpty())
        .collect(Collectors.joining(","));
    element.setAttribute("eventIDs", eventIdsString);
  }

  private Element createPropertiesElement(Document doc, BlueprintData bp) {
    Element propertiesElement = doc.createElement(PROPERTIES_ELEMENT);

    Element stringPropsElement = createPropertySubElement(doc, STRING_PROPS_ELEMENT, bp.stringProperties());
    propertiesElement.appendChild(stringPropsElement);

    Element doublePropsElement = createPropertySubElement(doc, DOUBLE_PROPS_ELEMENT, bp.doubleProperties());
    propertiesElement.appendChild(doublePropsElement);

    return propertiesElement;
  }

  private <T> Element createPropertySubElement(Document doc, String elementName, Map<String, T> properties) {
    Element subElement = doc.createElement(elementName);
    if (properties != null) {
      properties.forEach((key, value) -> {
        Element prop = doc.createElement(PROPERTY_ELEMENT);
        prop.setAttribute("key", key);
        String valueString = (value instanceof Double) ? String.format("%.2f", value) : Objects.toString(value, "");
        prop.setAttribute("value", valueString);
        subElement.appendChild(prop);
      });
    }
    return subElement;
  }
}
