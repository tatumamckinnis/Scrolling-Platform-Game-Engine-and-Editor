package oogasalad.editor.view;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.resources.EditorResourceLoader;
import oogasalad.fileparser.BlueprintDataParser;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.FrameData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * JavaFX Pane to display available prefabs (Blueprints) loaded from a fixed editor prefab file.
 * Allows users to select a prefab for placement by clicking on its visual representation,
 * or by dragging and dropping it onto the EditorGameView.
 * Implements EditorViewListener to reload the prefab list when notified of changes and log other events.
 * The prefabs are loaded exclusively from the path defined by {@code EDITOR_PREFAB_PATH}.
 *
 * @author Tatum McKinnis
 */
public class PrefabPalettePane extends VBox implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(PrefabPalettePane.class);
  private static final String UI_RESOURCES = "EditorUI";
  private static final int PREFAB_ICON_SIZE = 64;
  private static final String PLACEHOLDER_IMAGE_RESOURCE = "/oogasalad/editor/view/resources/images/placeholder_icon.png";
  private static final String EDITOR_PREFAB_PATH = "data/editorData/prefabricatedData/prefab.xml";

  public static final DataFormat PREFAB_BLUEPRINT_ID = new DataFormat("oogasalad/prefab-blueprint-id");


  private final EditorController controller;
  private final TilePane prefabGrid;
  private final ObjectProperty<BlueprintData> selectedPrefab = new SimpleObjectProperty<>(null);
  private final ResourceBundle uiResources;
  private final Image placeholderImage;
  private final Map<Integer, BlueprintData> loadedPrefabs = new HashMap<>();
  private Node selectedNode = null;

  /**
   * Constructs the PrefabPalettePane. Initializes UI components, loads resources,
   * loads the initial set of prefabs from the fixed path, and registers itself
   * as a listener for editor updates.
   *
   * @param controller The main EditorController instance for communication.
   */
  public PrefabPalettePane(EditorController controller) {
    this.controller = controller;
    this.uiResources = EditorResourceLoader.loadResourceBundle(UI_RESOURCES);
    this.placeholderImage = loadPlaceholderImage();

    setPadding(new Insets(10));
    setSpacing(10);
    getStyleClass().add("prefab-palette");

    Label titleLabel = new Label(uiResources.getString("PrefabPaletteTitle"));
    titleLabel.getStyleClass().add("header-label");

    prefabGrid = new TilePane();
    prefabGrid.setPadding(new Insets(5));
    prefabGrid.setHgap(10);
    prefabGrid.setVgap(10);
    prefabGrid.setPrefColumns(3);
    prefabGrid.getStyleClass().add("prefab-grid");

    ScrollPane scrollPane = new ScrollPane(prefabGrid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.getStyleClass().add("prefab-scroll-pane");
    scrollPane.setStyle("-fx-background-insets: 0; -fx-padding: 0;");

    getChildren().addAll(titleLabel, scrollPane);

    loadAvailablePrefabs();

    controller.registerViewListener(this);
  }

  /**
   * Loads the placeholder image used for prefabs that lack a specific icon.
   * Logs an error if the placeholder image resource cannot be found or loaded.
   *
   * @return The loaded placeholder Image, or null if loading failed.
   */
  private Image loadPlaceholderImage() {
    Image tempPlaceholder = null;
    try (InputStream stream = getClass().getResourceAsStream(PLACEHOLDER_IMAGE_RESOURCE)) {
      if (stream != null) {
        tempPlaceholder = new Image(stream, PREFAB_ICON_SIZE, PREFAB_ICON_SIZE, true, true);
      } else {
        LOG.error("Placeholder image resource not found: {}", PLACEHOLDER_IMAGE_RESOURCE);
      }
    } catch (Exception e) {
      LOG.error("Failed to load placeholder image resource: {}", PLACEHOLDER_IMAGE_RESOURCE, e);
    }
    return tempPlaceholder;
  }


  /**
   * Loads prefabs exclusively from the fixed path defined by {@code EDITOR_PREFAB_PATH}.
   * Clears the existing prefab grid and populates it with the loaded prefabs.
   * If no prefabs are found or the file doesn't exist, displays a corresponding message.
   */
  public void loadAvailablePrefabs() {
    LOG.debug("Loading available prefabs from fixed path: {}", EDITOR_PREFAB_PATH);
    loadedPrefabs.clear();
    prefabGrid.getChildren().clear();
    if (selectedNode != null) {
      selectedNode.getStyleClass().remove("selected-prefab-item");
      selectedNode = null;
    }
    selectedPrefab.set(null);

    Map<Integer, BlueprintData> editorPrefabs = loadPrefabsFromFile(EDITOR_PREFAB_PATH);
    loadedPrefabs.putAll(editorPrefabs);

    if (loadedPrefabs.isEmpty()) {
      LOG.warn("No prefabs found or loaded from {}", EDITOR_PREFAB_PATH);
      prefabGrid.getChildren().add(new Label(uiResources.getString("NoPrefabsFound")));
    } else {
      loadedPrefabs.values().stream()
          .sorted((p1, p2) -> p1.type().compareToIgnoreCase(p2.type()))
          .forEach(this::addPrefabToGrid);
    }
    LOG.info("Loaded {} unique prefabs from {}.", loadedPrefabs.size(), EDITOR_PREFAB_PATH);
  }

  /**
   * Loads BlueprintData records from a specified XML file path.
   * Assumes the XML structure contains a root {@code <prefabs>} element,
   * followed by one or more {@code <game>} elements, each containing
   * {@code <objectGroup>} and {@code <object>} elements as expected by
   * {@link BlueprintDataParser}.
   * Handles file not found errors and XML parsing exceptions gracefully.
   *
   * @param filePath The absolute or relative path to the prefab XML file.
   * @return A Map of blueprint IDs to loaded {@link BlueprintData}, or an empty map if
   * the file doesn't exist or parsing fails.
   */
  private Map<Integer, BlueprintData> loadPrefabsFromFile(String filePath) {
    Map<Integer, BlueprintData> prefabs = new HashMap<>();
    File file = new File(filePath);
    if (!file.exists()) {
      LOG.warn("Prefab file specified does not exist, skipping: {}", filePath);
      return prefabs;
    }
    LOG.debug("Attempting to load prefabs from: {}", filePath);

    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(file);
      doc.getDocumentElement().normalize();

      Element rootElement = doc.getDocumentElement();
      if (!rootElement.getNodeName().equalsIgnoreCase("prefabs")) {
        LOG.error("Invalid prefab file format: Root element must be <prefabs> in {}", filePath);
        return prefabs;
      }

      BlueprintDataParser parser = new BlueprintDataParser();
      prefabs = parser.getBlueprintData(rootElement, new ArrayList<>());

      LOG.info("Successfully parsed {} prefabs from {}", prefabs.size(), filePath);

    } catch (Exception e) {
      LOG.error("Failed to parse prefab file {}: {}", filePath, e.getMessage(), e);
      controller.notifyErrorOccurred(
          "Error loading prefabs from " + file.getName() + ": " + e.getMessage());
    }
    return prefabs;
  }


  /**
   * Creates and adds a visual representation (icon and label) of a single prefab
   * to the {@code prefabGrid}. Sets up mouse click handling for selection and
   * drag detection for drag-and-drop placement.
   *
   * @param prefab The {@link BlueprintData} of the prefab to add to the grid.
   */
  private void addPrefabToGrid(BlueprintData prefab) {
    ImageView imageView = new ImageView();
    imageView.setFitWidth(PREFAB_ICON_SIZE);
    imageView.setFitHeight(PREFAB_ICON_SIZE);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);

    Image prefabImage = loadPrefabImage(prefab);

    imageView.setImage(prefabImage != null ? prefabImage : placeholderImage);

    if (prefabImage == null || prefabImage == placeholderImage) {
      imageView.getStyleClass().add("placeholder-prefab-icon");
    } else {
      imageView.getStyleClass().remove("placeholder-prefab-icon");
    }

    Label nameLabel = new Label(prefab.type());
    nameLabel.setWrapText(true);
    nameLabel.setMaxWidth(PREFAB_ICON_SIZE);
    nameLabel.getStyleClass().add("prefab-name-label");

    VBox prefabContainer = new VBox(imageView, nameLabel);
    prefabContainer.setSpacing(5);
    prefabContainer.getStyleClass().add("prefab-item");
    prefabContainer.setPadding(new Insets(5));
    prefabContainer.setUserData(prefab);

    String tooltipText = String.format("%s\nGroup: %s\nGame: %s", prefab.type(), prefab.group(),
        prefab.gameName());
    Tooltip.install(prefabContainer, new Tooltip(tooltipText));

    prefabContainer.setOnMouseClicked(event -> {
      if (selectedNode != null) {
        selectedNode.getStyleClass().remove("selected-prefab-item");
      }
      selectedNode = prefabContainer;
      selectedNode.getStyleClass().add("selected-prefab-item");

      LOG.debug("Prefab selected: {}", prefab.type());
      selectedPrefab.set(prefab);

      controller.setActiveTool("placePrefabTool");
    });

    prefabContainer.setOnDragDetected(event -> {
      LOG.debug("Drag detected on prefab: {}", prefab.type());
      Dragboard db = prefabContainer.startDragAndDrop(TransferMode.COPY);

      ClipboardContent content = new ClipboardContent();
      content.put(PREFAB_BLUEPRINT_ID, String.valueOf(prefab.blueprintId()));
      db.setContent(content);

      db.setDragView(prefabImage != null ? prefabImage : placeholderImage,
          event.getX(), event.getY());

      event.consume();
    });

    prefabGrid.getChildren().add(prefabContainer);
  }

  /**
   * Attempts to load the display image for a given prefab based on its {@link BlueprintData}.
   * Uses the baseFrame or first frame information from the SpriteData to display the correct
   * portion of the sprite sheet by taking a snapshot of the relevant viewport.
   *
   * @param prefab The {@link BlueprintData} containing sprite and game name information.
   * @return The loaded {@link Image} for the prefab's specific frame, or placeholderImage if loading fails or data is missing.
   */
  private Image loadPrefabImage(BlueprintData prefab) {
    if (prefab.spriteData() == null || prefab.spriteData().spriteFile() == null
        || prefab.spriteData().spriteFile().getPath().isEmpty()
        || (prefab.spriteData().baseImage() == null && (prefab.spriteData().frames() == null || prefab.spriteData().frames().isEmpty()))
        || prefab.gameName() == null || prefab.gameName().isEmpty()) {
      LOG.warn("Cannot load image for prefab '{}': Missing sprite data, game name, baseImage, or frames.", prefab.type());
      return placeholderImage;
    }

    try {
      File imageFile = prefab.spriteData().spriteFile();

      if (imageFile.exists() && imageFile.isFile()) {
        Image sheetImage;
        try (FileInputStream fis = new FileInputStream(imageFile)) {
          sheetImage = new Image(fis);
        } catch (FileNotFoundException e) {
          LOG.warn("Prefab sprite file not found at expected path: {}", imageFile.getAbsolutePath());
          return placeholderImage;
        } catch (IOException e) {
          LOG.error("IO Error loading image file {}", imageFile.getAbsolutePath(), e);
          return placeholderImage;
        }

        FrameData displayFrame = prefab.spriteData().baseImage();
        if (displayFrame == null) {
          if (prefab.spriteData().frames() != null && !prefab.spriteData().frames().isEmpty()) {
            displayFrame = prefab.spriteData().frames().get(0);
          } else {
            LOG.warn("No baseImage or frames available for prefab '{}'. Using placeholder.", prefab.type());
            return placeholderImage;
          }
        }

        if (displayFrame.width() <= 0 || displayFrame.height() <= 0) {
          LOG.warn("Invalid frame dimensions (w={}, h={}) for prefab '{}'. Using placeholder.",
              displayFrame.width(), displayFrame.height(), prefab.type());
          return placeholderImage;
        }

        ImageView tempImageView = new ImageView(sheetImage);
        Rectangle2D viewportRect = new Rectangle2D(
            displayFrame.x(),
            displayFrame.y(),
            displayFrame.width(),
            displayFrame.height()
        );

        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(viewportRect);
        params.setFill(Color.TRANSPARENT); // Ensure transparency is handled correctly

        WritableImage frameImage = tempImageView.snapshot(params, null);

        if (frameImage == null || frameImage.isError() || frameImage.getPixelReader() == null) {
          LOG.error("Failed to create snapshot for prefab '{}'. Using placeholder. Error: {}", prefab.type(), frameImage != null ? frameImage.getException() : "null image");
          return placeholderImage;
        }
        return frameImage;

      } else {
        LOG.warn("Prefab sprite file not found at expected path: {}", imageFile.getAbsolutePath());
        return placeholderImage;
      }
    } catch (Exception e) {
      LOG.error("Error processing image for prefab '{}' (File: {}): {}",
          prefab.type(),
          (prefab.spriteData() != null && prefab.spriteData().spriteFile() != null) ? prefab.spriteData().spriteFile().getPath() : "N/A",
          e.getMessage(), e);
      return placeholderImage;
    }
  }

  /**
   * Retrieves a loaded prefab blueprint by its ID.
   *
   * @param id The blueprint ID.
   * @return The BlueprintData, or null if not found.
   */
  public BlueprintData getPrefabById(int id) {
    return loadedPrefabs.get(id);
  }


  /**
   * Returns the JavaFX property holding the currently selected prefab.
   * Allows other components to bind to or listen for changes in the selected prefab.
   *
   * @return The {@link ObjectProperty} wrapping the selected {@link BlueprintData}.
   */
  public ObjectProperty<BlueprintData> selectedPrefabProperty() {
    return selectedPrefab;
  }

  /**
   * Gets the currently selected prefab instance.
   *
   * @return The selected {@link BlueprintData}, or null if no prefab is currently selected.
   */
  public BlueprintData getSelectedPrefab() {
    return selectedPrefab.get();
  }

  /**
   * Called by the {@link EditorController} when potential changes to prefabs
   * (e.g., external file modification, saving prefabs) might have occurred.
   * Triggers a reload of the prefabs from the fixed {@code EDITOR_PREFAB_PATH}.
   */
  @Override
  public void onPrefabsChanged() {
    LOG.info("PrefabPalettePane notified: Potential prefab changes detected. Reloading palette from fixed path.");
    loadAvailablePrefabs();
  }

  /**
   * Called by the {@link EditorController} when an object is removed from the level data.
   * This pane does not need to react to individual object removals in the level.
   * Logs the event at TRACE level.
   *
   * @param objectId The UUID of the removed object.
   */
  @Override
  public void onObjectRemoved(UUID objectId) {
    LOG.trace("PrefabPalettePane notified: Object removed (ID: {}). No action taken.", objectId);
  }

  /**
   * Called by the {@link EditorController} when an object's data is updated in the level.
   * This pane does not need to react to individual object updates in the level.
   * Logs the event at TRACE level.
   *
   * @param objectId The UUID of the updated object.
   */
  @Override
  public void onObjectUpdated(UUID objectId) {
    LOG.trace("PrefabPalettePane notified: Object updated (ID: {}). No action taken.", objectId);
  }

  /**
   * Called by the {@link EditorController} when the selected object in the level changes.
   * This pane maintains its own prefab selection independent of the level selection.
   * Logs the event at TRACE level.
   *
   * @param selectedObjectId The UUID of the newly selected object, or null if deselected.
   */
  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    LOG.trace("PrefabPalettePane notified: Level selection changed (ID: {}). No action taken.", selectedObjectId);
  }

  /**
   * Called by the {@link EditorController} when an object is added to the level data.
   * This pane does not need to react to individual object additions in the level.
   * Logs the event at TRACE level.
   *
   * @param objectId The UUID of the added object.
   */
  @Override
  public void onObjectAdded(UUID objectId) {
    LOG.trace("PrefabPalettePane notified: Object added (ID: {}). No action taken.", objectId);
  }

  /**
   * Called by the {@link EditorController} when global dynamic variables are changed.
   * This pane does not currently depend on dynamic variables.
   * Logs the event at TRACE level.
   */
  @Override
  public void onDynamicVariablesChanged() {
    LOG.trace("PrefabPalettePane notified: Dynamic variables changed. No action taken.");
  }

  /**
   * Called by the {@link EditorController} when a general error occurs that views
   * should be aware of. Logs the error message.
   *
   * @param errorMessage The error message describing the issue.
   */
  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("PrefabPalettePane notified: An error occurred: {}", errorMessage);
  }
}
