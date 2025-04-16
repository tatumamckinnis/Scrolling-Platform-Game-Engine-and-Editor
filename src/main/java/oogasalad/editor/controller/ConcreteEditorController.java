package oogasalad.editor.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
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
import oogasalad.editor.model.EditorObjectPopulator;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.model.saver.BlueprintBuilder;
import oogasalad.editor.view.EditorViewListener;
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
 * Concrete implementation of the EditorController interface. Acts as a mediator between the editor
 * view and the editor data backend (EditorDataAPI). Manages listeners and notifies them of changes.
 * Implements the updated EditorController interface with refactored event handling methods.
 *
 * @author Tatum McKinnis, Jacob You
 */
public class ConcreteEditorController implements EditorController {

  private static final Logger LOG = LogManager.getLogger(ConcreteEditorController.class);

  private final EditorDataAPI editorDataAPI;
  private UUID currentSelectedObjectId;
  private String activeToolName = "selectionTool";

  private final List<EditorViewListener> viewListeners = new CopyOnWriteArrayList<>();

  public ConcreteEditorController(EditorDataAPI editorDataAPI) {
    this.editorDataAPI = Objects.requireNonNull(editorDataAPI, "EditorDataAPI cannot be null.");
    LOG.info("ConcreteEditorController initialized.");
  }
  @Override
  public void notifyPrefabsChanged() {
    LOG.debug("Notifying listeners: Prefabs changed");
    for (EditorViewListener listener : viewListeners) {
      listener.onPrefabsChanged();
    }
  }
  @Override
  public UUID getCurrentSelectedObjectId() {
    return currentSelectedObjectId;
  }

  @Override
  public void notifyErrorOccurred(String errorMessage) { // Change to public
    LOG.debug("Notifying listeners: Error occurred - {}", errorMessage);
    for (EditorViewListener listener : viewListeners) {
      listener.onErrorOccurred(errorMessage);
    }
  }
  @Override
  public void requestPrefabPlacement(BlueprintData prefabData, double worldX, double worldY) {

    Objects.requireNonNull(prefabData, "PrefabData cannot be null for placement request");
    LOG.info("Processing prefab placement request: Type='{}', Pos=({},{})", prefabData.type(), worldX, worldY);
    UUID newObjectId = null;
    try {
      newObjectId = editorDataAPI.createEditorObject();
      EditorObject newObject = editorDataAPI.getEditorObject(newObjectId);

      if (newObject == null) {
        throw new IllegalStateException("Failed to create or retrieve new EditorObject with ID: " + newObjectId);
      }

      EditorObjectPopulator.populateFromBlueprint(newObject, prefabData, editorDataAPI);

      if (!Double.isFinite(worldX) || !Double.isFinite(worldY)) {
        throw new IllegalArgumentException("Invalid placement coordinates: (" + worldX + ", " + worldY + ")");
      }
      int floorWorldX = (int) Math.floor(worldX);
      int floorWorldY = (int) Math.floor(worldY);

      editorDataAPI.getSpriteDataAPI().setX(newObjectId, floorWorldX);
      editorDataAPI.getSpriteDataAPI().setY(newObjectId, floorWorldY);

      int hitboxX = floorWorldX;
      int hitboxY = floorWorldY;
      if (prefabData.hitBoxData() != null) {
        hitboxX += prefabData.hitBoxData().spriteDx();
        hitboxY += prefabData.hitBoxData().spriteDy();
      }
      editorDataAPI.getHitboxDataAPI().setX(newObjectId, hitboxX);
      editorDataAPI.getHitboxDataAPI().setY(newObjectId, hitboxY);

      String uniqueName = String.format("%s_%d_%d_%s",
          prefabData.type(), floorWorldX, floorWorldY,
          UUID.randomUUID().toString().substring(0, 4));
      editorDataAPI.getIdentityDataAPI().setName(newObjectId, uniqueName);

      LOG.debug("Object {} created and populated from prefab.", newObjectId);
      notifyObjectAdded(newObjectId);
      notifyObjectSelected(newObjectId);

    } catch (Exception e) {
      LOG.error("Failed during prefab placement: {}", e.getMessage(), e);
      if (newObjectId != null) {
        try {
          editorDataAPI.removeEditorObject(newObjectId);
        } catch (Exception removeEx) {
          LOG.error("Failed to clean up object {} after placement error: {}", newObjectId, removeEx.getMessage(), removeEx);
        }
      }
      notifyErrorOccurred("Failed to place prefab: " + e.getMessage());
    }
  }

  @Override
  public void requestSaveAsPrefab(EditorObject objectToSave) {
    Objects.requireNonNull(objectToSave, "Object to save as prefab cannot be null");
    LOG.info("Processing request to save object {} as prefab", objectToSave.getId());
    try {
      BlueprintData newPrefabData = BlueprintBuilder.fromEditorObject(objectToSave);

      // --- Determine Target File Path ---
      String currentGameDirectory = editorDataAPI.getCurrentGameDirectoryPath();
      if (currentGameDirectory == null || currentGameDirectory.isEmpty()) {
        LOG.error("Cannot save prefab: Current game directory path is not set.");
        notifyErrorOccurred("Cannot save prefab: Game path unknown.");
        return;
      }
      String prefabFilePath = Paths.get(currentGameDirectory, "prefabs.xml").toString();
      File prefabFile = new File(prefabFilePath);

      // --- Load Existing Prefabs ---
      Map<Integer, BlueprintData> existingPrefabs = loadPrefabsFromFile(prefabFile); // Use implemented method

      // --- Check for Duplicates ---
      boolean duplicateFound = existingPrefabs.values().stream()
          .anyMatch(existing -> existing.equals(newPrefabData));

      if (duplicateFound) {
        // TODO: Implement user confirmation dialog (Overwrite / Cancel)
        LOG.warn("Duplicate prefab definition found based on content for type: {}. Skipping save.", newPrefabData.type());
        notifyErrorOccurred("Prefab with similar properties already exists. Not saved.");
        return;
      }

      // --- Assign a new unique ID ---
      int maxId = existingPrefabs.keySet().stream().max(Integer::compare).orElse(0);
      BlueprintData finalPrefabData = newPrefabData.withId(maxId + 1);

      existingPrefabs.put(finalPrefabData.blueprintId(), finalPrefabData);

      // --- Save Updated Prefabs Map ---
      savePrefabsToFile(prefabFile, existingPrefabs); // Use implemented method

      LOG.info("Prefab '{}' (ID: {}) saved to {}", finalPrefabData.type(), finalPrefabData.blueprintId(), prefabFilePath);

      // --- Notify Palette to Reload ---
      notifyPrefabsChanged();

    } catch (Exception e) {
      LOG.error("Failed to save object {} as prefab: {}", objectToSave.getId(), e.getMessage(), e);
      notifyErrorOccurred("Failed to save as prefab: " + e.getMessage());
    }
  }

  private Map<Integer, BlueprintData> loadPrefabsFromFile(File prefabFile) {
    Map<Integer, BlueprintData> prefabs = new HashMap<>();
    if (!prefabFile.exists() || !prefabFile.isFile()) {
      LOG.info("Prefab file does not exist or is not a file, skipping: {}", prefabFile.getPath());
      return prefabs;
    }
    LOG.debug("Loading prefabs from: {}", prefabFile.getPath());

    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(prefabFile);
      doc.getDocumentElement().normalize();

      Element rootElement = doc.getDocumentElement();
      if (!rootElement.getNodeName().equalsIgnoreCase("prefabs")) {
        LOG.error("Invalid prefab file format: Root element must be <prefabs> in {}", prefabFile.getPath());
        return prefabs; // Return empty map on format error
      }

      BlueprintDataParser parser = new BlueprintDataParser();
      // Pass root element, parser iterates through <game>, <objectGroup>, <object>
      prefabs = parser.getBlueprintData(rootElement, new ArrayList<>()); // Empty event list ok

    } catch (ParserConfigurationException | SAXException | IOException | RuntimeException |
             BlueprintParseException | SpriteParseException | HitBoxParseException |
             PropertyParsingException | EventParseException e) { // Catch potential parsing exceptions
      LOG.error("Failed to parse prefab file {}: {}", prefabFile.getPath(), e.getMessage(), e);
      notifyErrorOccurred("Error loading prefabs from " + prefabFile.getName() + ": " + e.getMessage());
      return new HashMap<>(); // Return empty map on error
    }
    return prefabs;
  }

  /**
   * Saves the provided map of prefabs to the specified XML file.
   * Creates the <prefabs><game><objectGroup><object>...</objectGroup></game></prefabs> structure.
   */
  private void savePrefabsToFile(File prefabFile, Map<Integer, BlueprintData> prefabsMap)
      throws ParserConfigurationException, IOException, TransformerException, TransformerException {
    LOG.debug("Saving {} prefabs to {}", prefabsMap.size(), prefabFile.getPath());

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.newDocument();

    // Root element <prefabs>
    Element rootElement = doc.createElement("prefabs");
    doc.appendChild(rootElement);

    // Group prefabs by game name, then by group name
    Map<String, Map<String, List<BlueprintData>>> groupedByGame = prefabsMap.values().stream()
        .collect(Collectors.groupingBy(BlueprintData::gameName,
            Collectors.groupingBy(BlueprintData::group)));

    // Iterate through games
    for (Map.Entry<String, Map<String, List<BlueprintData>>> gameEntry : groupedByGame.entrySet()) {
      String gameName = gameEntry.getKey();
      Map<String, List<BlueprintData>> groupsInGame = gameEntry.getValue();

      Element gameElement = doc.createElement("game");
      gameElement.setAttribute("name", gameName);
      rootElement.appendChild(gameElement);

      // Iterate through groups within the game
      for (Map.Entry<String, List<BlueprintData>> groupEntry : groupsInGame.entrySet()) {
        String groupName = groupEntry.getKey();
        List<BlueprintData> blueprintsInGroup = groupEntry.getValue();

        Element groupElement = doc.createElement("objectGroup");
        groupElement.setAttribute("name", groupName);
        gameElement.appendChild(groupElement);

        // Iterate through blueprints within the group
        for (BlueprintData bp : blueprintsInGroup) {
          Element objectElement = createObjectElement(doc, bp);
          groupElement.appendChild(objectElement);
        }
      }
    }
    // Write the DOM document to the file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Pretty print
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); // Indentation amount
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(prefabFile);

    transformer.transform(source, result);
    LOG.info("Successfully saved prefabs to {}", prefabFile.getPath());
  }
  /**
   * Helper method to create an <object> Element from BlueprintData.
   * Mirrors the structure expected by BlueprintDataParser and generated by XmlBlueprintsWriter.
   */
  private Element createObjectElement(Document doc, BlueprintData bp) {
    Element objectElement = doc.createElement("object");

    // --- Basic Attributes ---
    objectElement.setAttribute("id", String.valueOf(bp.blueprintId()));
    objectElement.setAttribute("type", bp.type());
    objectElement.setAttribute("velocityX", String.format("%.2f", bp.velocityX()));
    objectElement.setAttribute("velocityY", String.format("%.2f", bp.velocityY()));
    objectElement.setAttribute("rotation", String.format("%.2f", bp.rotation()));

    // --- Sprite Attributes (if sprite data exists) ---
    SpriteData sprite = bp.spriteData();
    if (sprite != null && sprite.spriteFile() != null) {
      objectElement.setAttribute("spriteName", sprite.name() != null ? sprite.name() : "");
      objectElement.setAttribute("spriteFile", sprite.spriteFile().getPath() != null ? sprite.spriteFile().getPath() : "");
    } else {
      objectElement.setAttribute("spriteName", "");
      objectElement.setAttribute("spriteFile", "");
    }

    // --- Hitbox Attributes (if hitbox data exists) ---
    HitBoxData hitbox = bp.hitBoxData();
    if (hitbox != null) {
      objectElement.setAttribute("hitBoxWidth", String.valueOf(hitbox.hitBoxWidth()));
      objectElement.setAttribute("hitBoxHeight", String.valueOf(hitbox.hitBoxHeight()));
      objectElement.setAttribute("hitBoxShape", hitbox.shape() != null ? hitbox.shape() : "RECTANGLE"); // Default shape
      objectElement.setAttribute("spriteDx", String.valueOf(hitbox.spriteDx()));
      objectElement.setAttribute("spriteDy", String.valueOf(hitbox.spriteDy()));
    } else {
      // Default hitbox attributes if none provided? Or omit them? Omitting for now.
      LOG.warn("No hitbox data found for blueprint ID {}, omitting hitbox attributes.", bp.blueprintId());
    }

    // --- Event IDs Attribute ---
    String eventIdsString = bp.eventDataList().stream()
        .map(EventData::eventId)
        .filter(Objects::nonNull)
        .filter(id -> !id.isEmpty())
        .collect(Collectors.joining(","));
    objectElement.setAttribute("eventIDs", eventIdsString);

    // --- Properties Element ---
    Element propertiesElement = doc.createElement("properties");
    objectElement.appendChild(propertiesElement);

    // String Properties
    Element stringPropsElement = doc.createElement("stringProperties");
    if (bp.stringProperties() != null) {
      bp.stringProperties().forEach((key, value) -> {
        Element prop = doc.createElement("property");
        prop.setAttribute("key", key);
        prop.setAttribute("value", value);
        stringPropsElement.appendChild(prop);
      });
    }
    propertiesElement.appendChild(stringPropsElement);

    // Double Properties
    Element doublePropsElement = doc.createElement("doubleProperties");
    if (bp.doubleProperties() != null) {
      bp.doubleProperties().forEach((key, value) -> {
        Element prop = doc.createElement("property");
        prop.setAttribute("key", key);
        prop.setAttribute("value", String.format("%.2f", value)); // Format double
        doublePropsElement.appendChild(prop);
      });
    }
    propertiesElement.appendChild(doublePropsElement);

    // Displayed Properties (Optional, add if needed)
    // Element displayedPropsElement = doc.createElement("displayedProperties");
    // displayedPropsElement.setAttribute("propertyList", String.join(",", bp.displayedProperties()));
    // propertiesElement.appendChild(displayedPropsElement);

    return objectElement;
  }
  // --- Tool Management ---
  @Override
  public void setActiveTool(String toolName) { // Implementation for new method
    if (toolName != null && !toolName.equals(this.activeToolName)) {
      this.activeToolName = toolName;
      LOG.info("Controller: Active tool changed to {}", toolName);
      // Optionally notify the view if it needs to update tool button states
      // notifyToolChanged(toolName); // Requires adding to listener interface
    }
  }


    /**
     * Registers a new view listener if it is not already registered and is not null.
     * This listener will be notified of changes in the editor state.
     *
     * @param listener the {@link EditorViewListener} to register.
     */
  @Override
  public void registerViewListener(EditorViewListener listener) {
    if (listener != null && !viewListeners.contains(listener)) {
      viewListeners.add(listener);
      LOG.debug("Registered view listener: {}", listener.getClass().getSimpleName());
    }
  }

  /**
   * Unregisters a previously registered view listener if it is not null.
   * The listener will no longer receive editor state change notifications.
   *
   * @param listener the {@link EditorViewListener} to unregister.
   */
  @Override
  public void unregisterViewListener(EditorViewListener listener) {
    if (listener != null) {
      boolean removed = viewListeners.remove(listener);
      if (removed) {
        LOG.debug("Unregistered view listener: {}", listener.getClass().getSimpleName());
      }
    }
  }

  /**
   * Gets the {@link EditorDataAPI} instance used by this controller, providing access to underlying data managers.
   *
   * @return the EditorDataAPI instance.
   */
  @Override
  public EditorDataAPI getEditorDataAPI() {
    return editorDataAPI;
  }

  /**
   * Notifies all registered view listeners that a new object has been added to the model.
   *
   * @param objectId the UUID of the object that was added.
   */
  private void notifyObjectAdded(UUID objectId) {
    LOG.debug("Notifying listeners: Object added {}", objectId);
    for (EditorViewListener listener : viewListeners) {
      listener.onObjectAdded(objectId);
    }
  }

  /**
   * Notifies all registered view listeners that an object has been removed from the model.
   *
   * @param objectId the UUID of the object that was removed.
   */
  private void notifyObjectRemoved(UUID objectId) {
    LOG.debug("Notifying listeners: Object removed {}", objectId);
    for (EditorViewListener listener : viewListeners) {
      listener.onObjectRemoved(objectId);
    }
  }


  /**
   * Notifies all registered view listeners that an object's data has been updated in the model.
   *
   * @param objectId the UUID of the object that was updated.
   */
  private void notifyObjectUpdated(UUID objectId) {
    LOG.debug("Notifying listeners: Object updated {}", objectId);
    for (EditorViewListener listener : viewListeners) {
      listener.onObjectUpdated(objectId);
    }
  }

  /**
   * Notifies all registered view listeners that the currently selected object has changed.
   *
   * @param selectedObjectId the UUID of the newly selected object, or null if no object is selected.
   */
  private void notifySelectionChanged(UUID selectedObjectId) {
    LOG.debug("Notifying listeners: Selection changed {}", selectedObjectId);
    for (EditorViewListener listener : viewListeners) {
      listener.onSelectionChanged(selectedObjectId);
    }
  }

  /**
   * Notifies all registered view listeners that the set of available dynamic variables has potentially changed.
   */
  private void notifyDynamicVariablesChanged() {
    LOG.debug("Notifying listeners: Dynamic variables changed");
    for (EditorViewListener listener : viewListeners) {
      listener.onDynamicVariablesChanged();
    }
  }


  /**
   * Requests the placement of a new object within the editor at the specified world coordinates.
   * Delegates creation and property setting to the {@link EditorDataAPI}. Notifies listeners of the
   * object's addition and selection. Handles potential errors during the process.
   *
   * @param objectGroup      the group name to assign to the new object.
   * @param objectNamePrefix the prefix used to generate the object’s name.
   * @param worldX           the x-coordinate (in world units) where the object should be placed.
   * @param worldY           the y-coordinate (in world units) where the object should be placed.
   * @param cellSize         the size of a grid cell, used for default object sizing.
   */
  @Override
  public void requestObjectPlacement(String objectGroup, String objectNamePrefix, double worldX,
      double worldY, int cellSize) {
    LOG.info("Processing object placement request: Group='{}', Prefix='{}', Pos=({},{})",
        objectGroup, objectNamePrefix, worldX, worldY);
    UUID newObjectId = null;
    try {
      newObjectId = editorDataAPI.createEditorObject();
      int floorWorldX = (int) Math.floor(worldX);
      int floorWorldY = (int) Math.floor(worldY);
      String objectName = String.format("%s%d_%d", objectNamePrefix, floorWorldX, floorWorldY);

      editorDataAPI.getIdentityDataAPI().setName(newObjectId, objectName);
      editorDataAPI.getIdentityDataAPI().setGroup(newObjectId, objectGroup);
      editorDataAPI.getSpriteDataAPI().setX(newObjectId, floorWorldX);
      editorDataAPI.getSpriteDataAPI().setY(newObjectId, floorWorldY);
      editorDataAPI.getHitboxDataAPI().setX(newObjectId, floorWorldX);
      editorDataAPI.getHitboxDataAPI().setY(newObjectId, floorWorldY);
      editorDataAPI.getHitboxDataAPI().setWidth(newObjectId, cellSize);
      editorDataAPI.getHitboxDataAPI().setHeight(newObjectId, cellSize);
      editorDataAPI.getHitboxDataAPI().setShape(newObjectId, "RECTANGLE");

      LOG.debug("Object {} data set via EditorDataAPI.", newObjectId);
      notifyObjectAdded(newObjectId);
      notifyObjectSelected(newObjectId);
    } catch (Exception e) {
      LOG.error("Failed during object placement: {}", e.getMessage(), e);
      if (newObjectId != null) {
        editorDataAPI.removeEditorObject(newObjectId);
      }
      notifyErrorOccurred("Failed to place object: " + e.getMessage());
    }
  }

  /**
   * Processes a request to remove an object identified by its UUID.
   * Delegates the removal operation to the {@link EditorDataAPI}. Notifies listeners about the outcome
   * (object removal or error) and clears the selection if the removed object was selected.
   *
   * @param objectId the UUID of the object to be removed. Must not be null.
   * @throws NullPointerException if the objectId is null.
   */
  @Override
  public void requestObjectRemoval(UUID objectId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removal request");
    LOG.info("Processing object removal request: ID={}", objectId);
    try {
      boolean removed = editorDataAPI.removeEditorObject(objectId);
      if (removed) {
        LOG.debug("Successfully processed removal for object {}", objectId);
        notifyObjectRemoved(objectId);
        if (objectId.equals(currentSelectedObjectId)) {
          notifyObjectSelected(null);
        }
      } else {
        LOG.warn("Removal request processed but object {} was not found or not removed by backend.", objectId);
        notifyErrorOccurred("Object with ID " + objectId + " could not be removed (not found).");
      }
    } catch (Exception e) {
      LOG.error("Error during object removal for ID {}: {}", objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to remove object: " + e.getMessage());
    }
  }

  /**
   * Processes a request to update an existing object with the provided data.
   * Delegates the update operation to the {@link EditorDataAPI}. Notifies listeners about the outcome.
   *
   * @param updatedObject the object containing the updated data. Must not be null and must have a valid UUID.
   * @throws NullPointerException if updatedObject or its ID is null.
   */
  @Override
  public void requestObjectUpdate(EditorObject updatedObject) {
    Objects.requireNonNull(updatedObject, "Updated object cannot be null");
    UUID objectId = updatedObject.getId();
    Objects.requireNonNull(objectId, "Object ID cannot be null for update request");
    LOG.info("Processing object update request: ID={}", objectId);
    try {
      boolean updated = editorDataAPI.updateEditorObject(updatedObject);
      if (updated) {
        LOG.debug("Successfully processed update for object {}", objectId);
        notifyObjectUpdated(objectId);
      } else {
        LOG.warn("Update request processed but object {} was not found or not updated by backend.", objectId);
        notifyErrorOccurred("Object with ID " + objectId + " could not be updated (not found).");
      }
    } catch (Exception e) {
      LOG.error("Error during object update for ID {}: {}", objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to update object: " + e.getMessage());
    }
  }

  /**
   * Notifies the controller that an object has been selected in the view.
   * Updates the internal selection state and notifies listeners if the selection has changed.
   *
   * @param objectId the UUID of the selected object, or null to clear the selection.
   */
  @Override
  public void notifyObjectSelected(UUID objectId) {
    if (!Objects.equals(this.currentSelectedObjectId, objectId)) {
      this.currentSelectedObjectId = objectId;
      LOG.info("Controller state updated: Object selected: {}", objectId);
      notifySelectionChanged(this.currentSelectedObjectId);
    }
  }

  /**
   * Notifies the controller that object selection has been cleared in the view.
   * Equivalent to calling {@code notifyObjectSelected(null)}.
   */
  @Override
  public void notifyObjectDeselected() {
    notifyObjectSelected(null);
  }

  /**
   * Retrieves the {@link EditorObject} associated with the specified UUID from the data API.
   * Handles potential exceptions during retrieval.
   *
   * @param objectId The UUID of the editor object to be retrieved.
   * @return The corresponding {@link EditorObject}, or null if not found, ID is null, or an error occurs.
   */
  @Override
  public EditorObject getEditorObject(UUID objectId) {
    if (objectId == null) {
      LOG.trace("getEditorObject called with null ID.");
      return null;
    }
    try {
      return editorDataAPI.getEditorObject(objectId);
    } catch (Exception e) {
      LOG.error("Error retrieving object {}: {}", objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to get object data: " + e.getMessage());
      return null;
    }
  }

  /**
   * Retrieves the UUID of the object located at the specified grid coordinates.
   * Checks object hitboxes and considers layer priority for overlapping objects.
   * Handles potential exceptions during the check.
   *
   * @param gridX The X coordinate of the grid location to check.
   * @param gridY The Y coordinate of the grid location to check.
   * @return The UUID of the object at the location, or null if no object exists there or an error occurs.
   */
  @Override
  public UUID getObjectIDAt(double gridX, double gridY) {
    try {
      List<UUID> hitCandidates = new ArrayList<>();
      for (Map.Entry<UUID, EditorObject> entry : editorDataAPI.getLevel().getObjectDataMap().entrySet()) {
        UUID id = entry.getKey();
        EditorObject obj = entry.getValue();
        if (obj != null && ifCollidesObject(obj, gridX, gridY)) {
          hitCandidates.add(id);
        }
      }
      hitCandidates.sort((a, b) ->
          Integer.compare(editorDataAPI.getIdentityDataAPI().getLayerPriority(b),
              editorDataAPI.getIdentityDataAPI().getLayerPriority(a))
      );
      return hitCandidates.isEmpty() ? null : hitCandidates.get(0);
    } catch (Exception e) {
      LOG.error("Error checking object ID at ({}, {}): {}", gridX, gridY, e.getMessage(), e);
      notifyErrorOccurred("Error during object selection: " + e.getMessage());
      return null;
    }
  }

  /**
   * Checks if an object's hitbox collides with (contains) the given world coordinates.
   * Assumes hitbox coordinates represent the top-left corner.
   *
   * @param obj    The {@link EditorObject} to check. Can be null.
   * @param worldX The world X coordinate.
   * @param worldY The world Y coordinate.
   * @return {@code true} if the point is within the object's hitbox, {@code false} otherwise or if object/hitbox is null.
   */
  private boolean ifCollidesObject(EditorObject obj, double worldX, double worldY) {
    if (obj == null || obj.getHitboxData() == null) {
      return false;
    }
    double x = obj.getHitboxData().getX();
    double y = obj.getHitboxData().getY();
    int width = obj.getHitboxData().getWidth();
    int height = obj.getHitboxData().getHeight();

    return (worldX >= x && worldX < x + width) && (worldY >= y && worldY < y + height);
  }


  /**
   * Adds a new event definition to the specified object by delegating to the InputDataManager.
   * Notifies listeners of the object update. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier for the new event. Must not be null or empty.
   */
  @Override
  public void addEvent(UUID objectId, String eventId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for addEvent");
    Objects.requireNonNull(eventId, "Event ID cannot be null for addEvent");
    if (eventId.trim().isEmpty()) {
      notifyErrorOccurred("Event ID cannot be empty.");
      return;
    }
    LOG.debug("Controller delegating add event '{}' to object {}", eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().addEvent(objectId, eventId);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to add event '{}' to object {}: {}", eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to add event: " + e.getMessage());
    }
  }

  /**
   * Removes an event definition from the specified object by delegating to the InputDataManager.
   * Notifies listeners of the object update. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the event to remove. Must not be null.
   */
  @Override
  public void removeEvent(UUID objectId, String eventId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removeEvent");
    Objects.requireNonNull(eventId, "Event ID cannot be null for removeEvent");
    LOG.debug("Controller delegating remove event '{}' from object {}", eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().removeEvent(objectId, eventId);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to remove event '{}' from object {}: {}", eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to remove event: " + e.getMessage());
    }
  }

  /**
   * Gets all events associated with the specified object ID by delegating to the InputDataManager.
   * Handles potential exceptions.
   *
   * @param objectId UUID of the target object.
   * @return A Map where keys are event IDs (String) and values are {@link EditorEvent} objects. Returns an empty map on failure or if objectId is null.
   */
  @Override
  public Map<String, EditorEvent> getEventsForObject(UUID objectId) {
    if (objectId == null) {
      return Collections.emptyMap();
    }
    try {
      Map<String, EditorEvent> events = editorDataAPI.getInputDataAPI().getEvents(objectId);
      return (events != null) ? events : Collections.emptyMap();
    } catch (Exception e) {
      LOG.error("Failed to get events for object {}: {}", objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to get event data: " + e.getMessage());
      return Collections.emptyMap();
    }
  }

  /**
   * Adds an empty condition group (OR block) to the specified event by delegating to the InputDataManager.
   * Notifies listeners of the object update. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   */
  @Override
  public void addConditionGroup(UUID objectId, String eventId) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for addConditionGroup");
    Objects.requireNonNull(eventId, "Event ID cannot be null for addConditionGroup");
    LOG.debug("Controller delegating add condition group to event '{}' on object {}", eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().addConditionGroup(objectId, eventId);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to add condition group to event '{}' on object {}: {}", eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to add condition group: " + e.getMessage());
    }
  }

  /**
   * Adds a condition of a specific type to a specified group within an event by delegating to the InputDataManager.
   * Notifies listeners of the object update. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param groupIndex Index of the condition group to add to (non-negative).
   * @param conditionType String identifier of the condition type. Must not be null.
   */
  @Override
  public void addEventCondition(UUID objectId, String eventId, int groupIndex, String conditionType) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for addEventCondition");
    Objects.requireNonNull(eventId, "Event ID cannot be null for addEventCondition");
    Objects.requireNonNull(conditionType, "Condition type cannot be null for addEventCondition");
    LOG.debug("Controller delegating add condition '{}' to group {} of event '{}' on object {}", conditionType, groupIndex, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().addEventCondition(objectId, eventId, groupIndex, conditionType);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to add condition '{}' to group {} of event '{}' on object {}: {}", conditionType, groupIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to add condition: " + e.getMessage());
    }
  }

  /**
   * Removes a condition at a specific index within a specific group of an event by delegating to the InputDataManager.
   * Notifies listeners of the object update. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param groupIndex Index of the condition group (non-negative).
   * @param conditionIndex Index of the condition within the group to remove (non-negative).
   */
  @Override
  public void removeEventCondition(UUID objectId, String eventId, int groupIndex, int conditionIndex) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removeEventCondition");
    Objects.requireNonNull(eventId, "Event ID cannot be null for removeEventCondition");
    LOG.debug("Controller delegating remove condition [{},{}] from event '{}' on object {}", groupIndex, conditionIndex, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().removeEventCondition(objectId, eventId, groupIndex, conditionIndex);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to remove condition [{},{}] from event '{}' on object {}: {}", groupIndex, conditionIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to remove condition: " + e.getMessage());
    }
  }

  /**
   * Removes an entire condition group from an event by delegating to the InputDataManager.
   * Notifies listeners of the object update. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param groupIndex Index of the condition group to remove (non-negative).
   */
  @Override
  public void removeConditionGroup(UUID objectId, String eventId, int groupIndex) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removeConditionGroup");
    Objects.requireNonNull(eventId, "Event ID cannot be null for removeConditionGroup");
    LOG.debug("Controller delegating remove condition group {} from event '{}' on object {}", groupIndex, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().removeConditionGroup(objectId, eventId, groupIndex);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to remove condition group {} from event '{}' on object {}: {}", groupIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to remove condition group: " + e.getMessage());
    }
  }

  /**
   * Sets a String parameter for a specific condition within an event by delegating to the InputDataManager.
   * Does not notify listeners as change should be reflected directly in the edit UI. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param groupIndex Index of the condition group (non-negative).
   * @param conditionIndex Index of the condition within the group (non-negative).
   * @param paramName Name of the parameter to set. Must not be null.
   * @param value The String value (can be null).
   */
  @Override
  public void setEventConditionStringParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, String value) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for setEventConditionStringParameter");
    Objects.requireNonNull(eventId, "Event ID cannot be null for setEventConditionStringParameter");
    Objects.requireNonNull(paramName, "Parameter name cannot be null for setEventConditionStringParameter");
    LOG.trace("Controller delegating set condition String param '{}'='{}' at [{},{}] on event '{}', object {}", paramName, value, groupIndex, conditionIndex, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().setEventConditionStringParameter(objectId, eventId, groupIndex, conditionIndex, paramName, value);
    } catch (Exception e) {
      LOG.error("Failed to set condition String param '{}' at [{},{}] on event '{}', object {}: {}", paramName, groupIndex, conditionIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to set condition parameter: " + e.getMessage());
    }
  }

  /**
   * Sets a Double parameter for a specific condition within an event by delegating to the InputDataManager.
   * Does not notify listeners. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param groupIndex Index of the condition group (non-negative).
   * @param conditionIndex Index of the condition within the group (non-negative).
   * @param paramName Name of the parameter to set. Must not be null.
   * @param value The Double value (can be null).
   */
  @Override
  public void setEventConditionDoubleParameter(UUID objectId, String eventId, int groupIndex, int conditionIndex, String paramName, Double value) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for setEventConditionDoubleParameter");
    Objects.requireNonNull(eventId, "Event ID cannot be null for setEventConditionDoubleParameter");
    Objects.requireNonNull(paramName, "Parameter name cannot be null for setEventConditionDoubleParameter");
    LOG.trace("Controller delegating set condition Double param '{}'={} at [{},{}] on event '{}', object {}", paramName, value, groupIndex, conditionIndex, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().setEventConditionDoubleParameter(objectId, eventId, groupIndex, conditionIndex, paramName, value);
    } catch (Exception e) {
      LOG.error("Failed to set condition Double param '{}' at [{},{}] on event '{}', object {}: {}", paramName, groupIndex, conditionIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to set condition parameter: " + e.getMessage());
    }
  }

  /**
   * Gets all condition groups and their conditions for a specific event by delegating to the InputDataManager.
   * Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @return A List of condition groups (List<List<ExecutorData>>). Returns an empty list on failure or if object/event ID is null.
   */
  @Override
  public List<List<ExecutorData>> getEventConditions(UUID objectId, String eventId) {
    if (objectId == null || eventId == null) {
      return Collections.emptyList();
    }
    try {
      List<List<ExecutorData>> conditions = editorDataAPI.getInputDataAPI().getEventConditions(objectId, eventId);
      return (conditions != null) ? conditions : Collections.emptyList();
    } catch (Exception e) {
      LOG.error("Failed to get conditions for event '{}' on object {}: {}", eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to get condition data: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Gets a specific condition group from an event by delegating to the InputDataManager.
   * Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param groupIndex Index of the condition group (non-negative).
   * @return A List of {@link ExecutorData} for the specified group, or null if not found or an error occurs.
   */
  @Override
  public List<ExecutorData> getEventConditionGroup(UUID objectId, String eventId, int groupIndex) {
    if (objectId == null || eventId == null) {
      return null;
    }
    try {
      return editorDataAPI.getInputDataAPI().getEventConditionGroup(objectId, eventId, groupIndex);
    } catch (Exception e) {
      LOG.error("Failed to get condition group {} for event '{}' on object {}: {}", groupIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to get condition group data: " + e.getMessage());
      return null;
    }
  }

  /**
   * Adds an outcome of a specific type to an event by delegating to the InputDataManager.
   * Notifies listeners of the object update. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param outcomeType String identifier of the outcome type. Must not be null.
   */
  @Override
  public void addEventOutcome(UUID objectId, String eventId, String outcomeType) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for addEventOutcome");
    Objects.requireNonNull(eventId, "Event ID cannot be null for addEventOutcome");
    Objects.requireNonNull(outcomeType, "Outcome type cannot be null for addEventOutcome");
    LOG.debug("Controller delegating add outcome '{}' to event '{}' on object {}", outcomeType, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().addEventOutcome(objectId, eventId, outcomeType);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to add outcome '{}' to event '{}' on object {}: {}", outcomeType, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to add outcome: " + e.getMessage());
    }
  }

  /**
   * Removes an outcome at a specific index from an event by delegating to the InputDataManager.
   * Notifies listeners of the object update. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param outcomeIndex Index of the outcome to remove (non-negative).
   */
  @Override
  public void removeEventOutcome(UUID objectId, String eventId, int outcomeIndex) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for removeEventOutcome");
    Objects.requireNonNull(eventId, "Event ID cannot be null for removeEventOutcome");
    LOG.debug("Controller delegating remove outcome at index {} from event '{}' on object {}", outcomeIndex, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().removeEventOutcome(objectId, eventId, outcomeIndex);
      notifyObjectUpdated(objectId);
    } catch (Exception e) {
      LOG.error("Failed to remove outcome at index {} from event '{}' on object {}: {}", outcomeIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to remove outcome: " + e.getMessage());
    }
  }

  /**
   * Sets a String parameter for a specific outcome within an event by delegating to the InputDataManager.
   * Does not notify listeners. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param outcomeIndex Index of the outcome (non-negative).
   * @param paramName Name of the parameter to set. Must not be null.
   * @param value The String value (can be null).
   */
  @Override
  public void setEventOutcomeStringParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, String value) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for setEventOutcomeStringParameter");
    Objects.requireNonNull(eventId, "Event ID cannot be null for setEventOutcomeStringParameter");
    Objects.requireNonNull(paramName, "Parameter name cannot be null for setEventOutcomeStringParameter");
    LOG.trace("Controller delegating set outcome String param '{}'='{}' at index {} on event '{}', object {}", paramName, value, outcomeIndex, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().setEventOutcomeStringParameter(objectId, eventId, outcomeIndex, paramName, value);
    } catch (Exception e) {
      LOG.error("Failed to set outcome String param '{}' at index {} on event '{}', object {}: {}", paramName, outcomeIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to set outcome parameter: " + e.getMessage());
    }
  }

  /**
   * Sets a Double parameter for a specific outcome within an event by delegating to the InputDataManager.
   * Does not notify listeners. Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param outcomeIndex Index of the outcome (non-negative).
   * @param paramName Name of the parameter to set. Must not be null.
   * @param value The Double value (can be null).
   */
  @Override
  public void setEventOutcomeDoubleParameter(UUID objectId, String eventId, int outcomeIndex, String paramName, Double value) {
    Objects.requireNonNull(objectId, "Object ID cannot be null for setEventOutcomeDoubleParameter");
    Objects.requireNonNull(eventId, "Event ID cannot be null for setEventOutcomeDoubleParameter");
    Objects.requireNonNull(paramName, "Parameter name cannot be null for setEventOutcomeDoubleParameter");
    LOG.trace("Controller delegating set outcome Double param '{}'={} at index {} on event '{}', object {}", paramName, value, outcomeIndex, eventId, objectId);
    try {
      editorDataAPI.getInputDataAPI().setEventOutcomeDoubleParameter(objectId, eventId, outcomeIndex, paramName, value);
    } catch (Exception e) {
      LOG.error("Failed to set outcome Double param '{}' at index {} on event '{}', object {}: {}", paramName, outcomeIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to set outcome parameter: " + e.getMessage());
    }
  }

  /**
   * Gets all outcomes for a specific event by delegating to the InputDataManager.
   * Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @return A List of {@link ExecutorData} representing the outcomes. Returns an empty list on failure or if object/event ID is null.
   */
  @Override
  public List<ExecutorData> getEventOutcomes(UUID objectId, String eventId) {
    if (objectId == null || eventId == null) {
      return Collections.emptyList();
    }
    try {
      List<ExecutorData> outcomes = editorDataAPI.getInputDataAPI().getEventOutcomes(objectId, eventId);
      return (outcomes != null) ? outcomes : Collections.emptyList();
    } catch (Exception e) {
      LOG.error("Failed to get outcomes for event '{}' on object {}: {}", eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to get outcome data: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Gets the data for a specific outcome at a given index within an event by delegating to the InputDataManager.
   * Handles potential exceptions.
   *
   * @param objectId UUID of the target object. Must not be null.
   * @param eventId String identifier of the target event. Must not be null.
   * @param outcomeIndex Index of the desired outcome (non-negative).
   * @return The {@link ExecutorData} for the outcome, or null if not found or an error occurs.
   */
  @Override
  public ExecutorData getEventOutcomeData(UUID objectId, String eventId, int outcomeIndex) {
    if (objectId == null || eventId == null) {
      return null;
    }
    try {
      return editorDataAPI.getInputDataAPI().getEventOutcomeData(objectId, eventId, outcomeIndex);
    } catch (Exception e) {
      LOG.error("Failed to get outcome data at index {} for event '{}' on object {}: {}", outcomeIndex, eventId, objectId, e.getMessage(), e);
      notifyErrorOccurred("Failed to get outcome data: " + e.getMessage());
      return null;
    }
  }


  /**
   * Adds a new dynamic variable to the global container by delegating to the {@link DynamicVariableContainer}.
   * Notifies listeners upon successful addition. Handles potential exceptions.
   *
   * @param variable The {@link DynamicVariable} to add. Must not be null.
   * @throws NullPointerException if variable is null.
   * @throws IllegalArgumentException if the variable name/type is invalid or already exists.
   */
  @Override
  public void addDynamicVariable(DynamicVariable variable) {
    Objects.requireNonNull(variable, "DynamicVariable cannot be null");
    LOG.debug("Controller adding dynamic variable '{}'", variable.getName());
    try {
      DynamicVariableContainer container = editorDataAPI.getDynamicVariableContainer();
      if (container != null) {
        container.addVariable(variable);
        notifyDynamicVariablesChanged();
      } else {
        LOG.error("DynamicVariableContainer is null in EditorDataAPI. Cannot add variable.");
        notifyErrorOccurred("Internal error: Cannot access variable storage.");
      }
    } catch (IllegalArgumentException e) {
      LOG.warn("Failed to add dynamic variable '{}' due to invalid input or duplicate: {}",
          variable.getName(), e.getMessage());
      notifyErrorOccurred("Failed to add variable: " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Unexpected error adding dynamic variable '{}': {}", variable.getName(), e.getMessage(), e);
      notifyErrorOccurred("Failed to add variable: " + e.getMessage());
    }
  }

  /**
   * Retrieves all available dynamic variables from the global {@link DynamicVariableContainer}.
   * The provided objectId is currently ignored, assuming a global scope for variables.
   * Handles potential exceptions during retrieval.
   *
   * @param objectId The ID of the context object (currently ignored).
   * @return A list of {@link DynamicVariable} objects available. Returns an empty list on failure or if the container is inaccessible.
   */
  @Override
  public List<DynamicVariable> getAvailableDynamicVariables(UUID objectId) {
    LOG.trace("Retrieving available dynamic variables (context objectId: {})", objectId);
    try {
      DynamicVariableContainer container = editorDataAPI.getDynamicVariableContainer();
      if (container != null) {
        Collection<DynamicVariable> varsCollection = container.getAllVariables();
        return (varsCollection != null) ? new ArrayList<>(varsCollection) : Collections.emptyList();
      } else {
        LOG.warn("DynamicVariableContainer is null in EditorDataAPI.");
        return Collections.emptyList();
      }
    } catch (Exception e) {
      LOG.error("Failed to retrieve dynamic variables: {}", e.getMessage(), e);
      notifyErrorOccurred("Failed to retrieve variable list: " + e.getMessage());
      return Collections.emptyList();
    }
  }

}
