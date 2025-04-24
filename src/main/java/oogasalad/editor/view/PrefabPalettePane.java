package oogasalad.editor.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.loader.EditorBlueprintParser;
import oogasalad.editor.view.resources.EditorResourceLoader;
import oogasalad.fileparser.BlueprintDataParser;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.FrameData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Displays prefabs, handles selection and drag-and-drop. Includes filtering by game.
 *
 * @author Tatum McKinnis
 */
public class PrefabPalettePane extends VBox implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(PrefabPalettePane.class);
  private static final String UI_RESOURCES = "EditorUI";
  private static final int PREFAB_ICON_SIZE = 64;
  private static final String PLACEHOLDER_IMAGE_RESOURCE = "/oogasalad/editor/view/resources/images/placeholder_icon.png";
  private static final String EDITOR_PREFAB_PATH = "data/editorData/prefabricatedData/prefab.xml";
  private static final String ALL_GAMES_FILTER = "All";

  public static final DataFormat PREFAB_BLUEPRINT_ID = new DataFormat(
      "oogasalad/prefab-blueprint-id");


  private final EditorController controller;
  private final TilePane prefabGrid;
  private final ObjectProperty<BlueprintData> selectedPrefab = new SimpleObjectProperty<>(null);
  private final ResourceBundle uiResources;
  private final Image placeholderImage;
  private final Map<Integer, BlueprintData> allLoadedPrefabs = new HashMap<>();
  private Node selectedNode = null;
  private final FlowPane gameFilterPane;
  private final ToggleGroup gameToggleGroup;
  private String currentGameFilter = ALL_GAMES_FILTER;

  /**
   * Constructs the PrefabPalettePane.
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

    gameFilterPane = new FlowPane();
    gameFilterPane.setHgap(5);
    gameFilterPane.setVgap(5);
    gameFilterPane.setAlignment(Pos.CENTER_LEFT);
    gameToggleGroup = new ToggleGroup();

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
    scrollPane.setStyle(
        "-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

    getChildren().addAll(titleLabel, gameFilterPane, scrollPane);

    loadAvailablePrefabs();

    controller.registerViewListener(this);
  }

  /**
   * Loads the placeholder image.
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
   * Loads all available prefabs from the fixed path, updates the internal map, populates the filter
   * buttons, and displays the prefabs based on the current filter.
   */
  public void loadAvailablePrefabs() {
    LOG.debug("Loading available prefabs from fixed path: {}", EDITOR_PREFAB_PATH);
    allLoadedPrefabs.clear();
    Map<Integer, BlueprintData> editorPrefabs = loadPrefabsFromFile(EDITOR_PREFAB_PATH);
    allLoadedPrefabs.putAll(editorPrefabs);

    updateFilterButtons();
    displayFilteredPrefabs();

    LOG.info("Loaded {} total prefabs from {}.", allLoadedPrefabs.size(), EDITOR_PREFAB_PATH);
  }

  /**
   * Updates the game filter buttons based on the currently loaded prefabs.
   */
  private void updateFilterButtons() {
    gameFilterPane.getChildren().clear();
    gameToggleGroup.getToggles().clear();

    Set<String> gameNames = new TreeSet<>();
    allLoadedPrefabs.values().forEach(p -> gameNames.add(p.gameName()));

    ToggleButton allButton = createFilterButton(ALL_GAMES_FILTER);
    if (currentGameFilter.equals(ALL_GAMES_FILTER)) {
      allButton.setSelected(true);
    }
    gameFilterPane.getChildren().add(allButton);

    for (String gameName : gameNames) {
      ToggleButton gameButton = createFilterButton(gameName);
      if (currentGameFilter.equals(gameName)) {
        gameButton.setSelected(true);
      }
      gameFilterPane.getChildren().add(gameButton);
    }

    if (gameToggleGroup.getSelectedToggle() == null && !allButton.isSelected()) {
      LOG.warn("Current filter '{}' no longer valid, defaulting to '{}'", currentGameFilter,
          ALL_GAMES_FILTER);
      currentGameFilter = ALL_GAMES_FILTER;
      allButton.setSelected(true);
      displayFilteredPrefabs();
    }
  }

  /**
   * Creates a filter toggle button for a game.
   */
  private ToggleButton createFilterButton(String filterName) {
    ToggleButton button = new ToggleButton(filterName);
    button.setToggleGroup(gameToggleGroup);
    button.setUserData(filterName);
    button.setOnAction(e -> {
      if (button.isSelected()) {
        String selectedFilter = (String) button.getUserData();
        LOG.debug("Prefab filter changed to: {}", selectedFilter);
        currentGameFilter = selectedFilter;
        displayFilteredPrefabs();
      } else {

        if (gameToggleGroup.getSelectedToggle() == null) {
          button.setSelected(true);
        }
      }
    });
    return button;
  }


  /**
   * Clears and repopulates the prefab grid based on the currently selected game filter.
   */
  private void displayFilteredPrefabs() {
    prefabGrid.getChildren().clear();
    if (selectedNode != null) {
      selectedNode.getStyleClass().remove("selected-prefab-item");
      selectedNode = null;
    }
    selectedPrefab.set(null);

    List<BlueprintData> filteredPrefabs = allLoadedPrefabs.values().stream()
        .filter(p -> currentGameFilter.equals(ALL_GAMES_FILTER) || p.gameName()
            .equals(currentGameFilter))
        .sorted(Comparator.comparing(BlueprintData::type, String.CASE_INSENSITIVE_ORDER))
        .collect(Collectors.toList());

    if (filteredPrefabs.isEmpty()) {
      String messageKey =
          currentGameFilter.equals(ALL_GAMES_FILTER) ? "NoPrefabsFound" : "NoPrefabsForGame";
      prefabGrid.getChildren()
          .add(new Label(String.format(uiResources.getString(messageKey), currentGameFilter)));
    } else {
      for (BlueprintData prefab : filteredPrefabs) {
        addPrefabToGrid(prefab);
      }
    }
    LOG.info("Displayed {} prefabs for filter '{}'", filteredPrefabs.size(), currentGameFilter);
  }


  /**
   * Loads BlueprintData records from a specified XML file path using the EditorBlueprintParser.
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

      // FIX: Use the editor's blueprint parser
      EditorBlueprintParser parser = new EditorBlueprintParser();
      // Provide an empty list for events, assuming prefabs don't rely on pre-loaded event data
      prefabs = parser.getBlueprintData(rootElement, new ArrayList<>());

      LOG.info("Successfully parsed {} prefabs using EditorBlueprintParser from {}", prefabs.size(), filePath);

    } catch (Exception e) { // Catch broader exceptions from parser or XML processing
      LOG.error("Failed to parse prefab file {} using EditorBlueprintParser: {}", filePath, e.getMessage(), e);
      // Optionally notify the user via controller
      // controller.notifyErrorOccurred("Error loading prefabs: " + e.getMessage());
    }
    return prefabs;
  }



  /**
   * Creates and adds a visual representation (icon and label) of a single prefab to the internal
   * {@code prefabGrid}. Sets up mouse click and drag detection. Centers icon within a fixed-size
   * pane, preserving aspect ratio (may clip).
   *
   * @param prefab The {@link BlueprintData} of the prefab to add to the grid.
   */
  private void addPrefabToGrid(BlueprintData prefab) {
    Image prefabImg = loadPrefabImage(prefab);

    ImageView imageView = new ImageView(prefabImg != null ? prefabImg : placeholderImage);

    StackPane iconContainer = new StackPane();
    iconContainer.setPrefSize(PREFAB_ICON_SIZE, PREFAB_ICON_SIZE);
    iconContainer.setMinSize(PREFAB_ICON_SIZE, PREFAB_ICON_SIZE);
    iconContainer.setMaxSize(PREFAB_ICON_SIZE, PREFAB_ICON_SIZE);
    iconContainer.setAlignment(Pos.CENTER);

    imageView.setPreserveRatio(true);

    imageView.setFitWidth(PREFAB_ICON_SIZE);
    imageView.setFitHeight(PREFAB_ICON_SIZE);

    iconContainer.getChildren().add(imageView);

    Label nameLabel = new Label(prefab.type());
    nameLabel.setWrapText(true);
    nameLabel.setMaxWidth(PREFAB_ICON_SIZE);
    nameLabel.getStyleClass().add("prefab-name-label");

    VBox prefabContainer = new VBox(iconContainer, nameLabel);
    prefabContainer.setSpacing(5);
    prefabContainer.getStyleClass().add("prefab-item");
    prefabContainer.setAlignment(Pos.CENTER);
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

    setupDragDetectionForNode(prefabContainer, prefab, prefabImg);

    prefabGrid.getChildren().add(prefabContainer);
  }

  /**
   * Sets up drag detection for a specific prefab node.
   */
  private void setupDragDetectionForNode(Node node, BlueprintData prefab, Image dragViewImage) {

    node.setOnDragDetected((MouseEvent event) -> {
      LOG.debug("Drag detected on prefab: {}", prefab.type());
      Dragboard db = node.startDragAndDrop(TransferMode.COPY);

      ClipboardContent content = new ClipboardContent();
      content.put(PREFAB_BLUEPRINT_ID, String.valueOf(prefab.blueprintId()));
      db.setContent(content);

      Image imageToShow =
          (dragViewImage != null && !dragViewImage.isError()) ? dragViewImage : placeholderImage;
      if (imageToShow != null) {
        db.setDragView(imageToShow, -(imageToShow.getWidth() / 2), -(imageToShow.getHeight() / 2));
      }
      event.consume();
    });
  }


  /**
   * Attempts to load the display image for a given prefab. (Implementation remains the same as
   * previous version)
   */
  private Image loadPrefabImage(BlueprintData prefab) {

    LOG.debug("Loading prefab image for type: {}", prefab.type());
    oogasalad.fileparser.records.SpriteData spriteDataRecord = prefab.spriteData();

    if (spriteDataRecord == null || spriteDataRecord.spriteFile() == null
        || spriteDataRecord.spriteFile().getPath().isEmpty()
        || (spriteDataRecord.baseImage() == null && (spriteDataRecord.frames() == null
        || spriteDataRecord.frames().isEmpty()))
        || prefab.gameName() == null || prefab.gameName().isEmpty()) {
      LOG.warn(
          "Cannot load image for prefab '{}': Missing sprite path, baseImage/frames, or game name.",
          prefab.type());
      return placeholderImage;
    }

    try {
      File imageFile = spriteDataRecord.spriteFile();
      LOG.debug(
          "Attempting to load image file reference from blueprint: '{}' (Exists: {}, IsFile: {})",
          imageFile != null ? imageFile.getAbsolutePath() : "null",
          imageFile != null && imageFile.exists(),
          imageFile != null && imageFile.isFile());

      if (imageFile != null && imageFile.exists() && imageFile.isFile()) {
        Image sheetImage;
        try (FileInputStream fis = new FileInputStream(imageFile)) {
          sheetImage = new Image(fis);
        } catch (Exception e) {
          LOG.error("Failed to load sheet image {} for prefab '{}': {}", imageFile.getPath(),
              prefab.type(), e.getMessage());
          return placeholderImage;
        }

        FrameData displayFrame = spriteDataRecord.baseImage();
        if (displayFrame == null) {
          List<FrameData> framesList = spriteDataRecord.frames();
          if (framesList != null && !framesList.isEmpty()) {
            displayFrame = framesList.get(0);
            LOG.trace("Using first frame '{}' as display frame for {}", displayFrame.name(),
                prefab.type());
          } else {
            LOG.warn(
                "No baseImage record or frames list available for prefab '{}'. Using placeholder.",
                prefab.type());
            return placeholderImage;
          }
        } else {
          LOG.trace("Using baseImage frame '{}' as display frame for {}", displayFrame.name(),
              prefab.type());
        }

        LOG.debug("Display frame details for {}: Name='{}', x={}, y={}, w={}, h={}",
            prefab.type(), displayFrame.name(), displayFrame.x(), displayFrame.y(),
            displayFrame.width(), displayFrame.height());

        if (displayFrame.width() <= 0 || displayFrame.height() <= 0) {
          LOG.warn("Invalid frame dimensions (w={}, h={}) for prefab '{}'. Using placeholder.",
              displayFrame.width(), displayFrame.height(), prefab.type());
          return placeholderImage;
        }

        ImageView tempImageView = new ImageView(sheetImage);
        Rectangle2D viewportRect = new Rectangle2D(
            displayFrame.x(), displayFrame.y(), displayFrame.width(), displayFrame.height()
        );
        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(viewportRect);
        params.setFill(Color.TRANSPARENT);

        LOG.trace("Taking snapshot for {} with viewport: {}", prefab.type(), viewportRect);
        WritableImage frameImage = tempImageView.snapshot(params, null);

        if (frameImage == null || frameImage.isError() || frameImage.getPixelReader() == null) {
          LOG.error("Failed to create snapshot for prefab '{}'. Using placeholder. Error: {}",
              prefab.type(), frameImage != null ? frameImage.getException() : "null image");
          return placeholderImage;
        }
        LOG.debug("Successfully created snapshot image for {}", prefab.type());
        return frameImage;

      } else {
        LOG.warn("Prefab sprite file reference invalid or file not found: {}",
            imageFile != null ? imageFile.getAbsolutePath() : "null");
        return placeholderImage;
      }
    } catch (Exception e) {
      LOG.error("Error processing image for prefab '{}': {}", prefab.type(), e.getMessage(), e);
      return placeholderImage;
    }
  }

  /**
   * Retrieves a loaded prefab blueprint by its ID.
   */
  public BlueprintData getPrefabById(int id) {
    return allLoadedPrefabs.get(id);
  }

  /**
   * Returns the JavaFX property holding the currently selected prefab.
   */
  public ObjectProperty<BlueprintData> selectedPrefabProperty() {
    return selectedPrefab;
  }

  /**
   * Gets the currently selected prefab instance.
   */
  public BlueprintData getSelectedPrefab() {
    return selectedPrefab.get();
  }


  @Override
  public void onPrefabsChanged() {
    LOG.info("PrefabPalettePane notified: Potential prefab changes detected. Reloading palette.");
    loadAvailablePrefabs();
  }

  /**
   * Called when a sprite template is changed
   */
  @Override
  public void onSpriteTemplateChanged() {
  }

  @Override
  public void onObjectRemoved(UUID objectId) {
  }

  @Override
  public void onObjectUpdated(UUID objectId) {
  }

  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
  }

  @Override
  public void onObjectAdded(UUID objectId) {
  }

  @Override
  public void onDynamicVariablesChanged() {
  }

  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("PrefabPalettePane notified: An error occurred: {}", errorMessage);
  }
}