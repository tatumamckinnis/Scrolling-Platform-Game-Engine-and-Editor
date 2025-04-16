package oogasalad.editor.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.resources.EditorResourceLoader;
import oogasalad.fileparser.BlueprintDataParser;
import oogasalad.fileparser.records.BlueprintData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * JavaFX Pane to display available prefabs (Blueprints) loaded from game-specific
 * and global files. Allows users to select a prefab for placement.
 * Implements EditorViewListener to reload when prefabs are saved.
 *
 */
public class PrefabPalettePane extends VBox implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(PrefabPalettePane.class);
  private static final String DEFAULT_RESOURCE_PACKAGE = "oogasalad.editor.view.resources.";
  private static final String UI_RESOURCES = "EditorUI";
  private static final int PREFAB_ICON_SIZE = 64;
  private static final String PLACEHOLDER_IMAGE_RESOURCE = "/oogasalad/editor/view/resources/images/placeholder_icon.png";

  private final EditorController controller;
  private final TilePane prefabGrid;
  private final ObjectProperty<BlueprintData> selectedPrefab = new SimpleObjectProperty<>(null);
  private final ResourceBundle uiResources;
  private final Image placeholderImage;

  private final Map<Integer, BlueprintData> loadedPrefabs = new HashMap<>();
  private Node selectedNode = null;

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
   * Loads prefabs from game-specific and global files.
   */
  public void loadAvailablePrefabs() {
    LOG.debug("Loading available prefabs...");
    loadedPrefabs.clear();
    prefabGrid.getChildren().clear();
    if (selectedNode != null) {
      selectedNode.getStyleClass().remove("selected-prefab-item");
      selectedNode = null;
    }
    selectedPrefab.set(null);

    String currentGameDirectory = controller.getEditorDataAPI().getCurrentGameDirectoryPath();
    if (currentGameDirectory == null || currentGameDirectory.isEmpty()) {
      LOG.error("Cannot load prefabs: Current game directory path is not set.");
      prefabGrid.getChildren().add(new Label(uiResources.getString("ErrorLoadingPrefabs")));
      return;
    }
    String gameSpecificPath = Paths.get(currentGameDirectory, "prefabs.xml").toString();
    String globalPath = "data/editorData/global_prefabs.xml";

    Map<Integer, BlueprintData> gamePrefabs = loadPrefabsFromFile(gameSpecificPath);
    Map<Integer, BlueprintData> globalPrefabs = loadPrefabsFromFile(globalPath);

    loadedPrefabs.putAll(globalPrefabs);
    loadedPrefabs.putAll(gamePrefabs);

    if (loadedPrefabs.isEmpty()) {
      prefabGrid.getChildren().add(new Label(uiResources.getString("NoPrefabsFound")));
    } else {
      loadedPrefabs.values().stream()
          .sorted((p1, p2) -> p1.type().compareToIgnoreCase(p2.type()))
          .forEach(this::addPrefabToGrid);
    }
    LOG.info("Loaded {} unique prefabs.", loadedPrefabs.size());
  }

  /**
   * Loads BlueprintData from a dedicated prefab XML file.
   * Assumes the file structure is <prefabs><game name="..."><objectGroup name="..."><object .../></objectGroup></game></prefabs>
   * or simpler <prefabs><object .../></prefabs> if not grouped.
   */
  private Map<Integer, BlueprintData> loadPrefabsFromFile(String filePath) {
    Map<Integer, BlueprintData> prefabs = new HashMap<>();
    File file = new File(filePath);
    if (!file.exists()) {
      LOG.info("Prefab file does not exist, skipping: {}", filePath);
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
      controller.notifyErrorOccurred("Error loading prefabs from " + file.getName() + ": " + e.getMessage());
    }
    return prefabs;
  }


  /**
   * Adds a visual representation of a single prefab to the grid.
   */
  private void addPrefabToGrid(BlueprintData prefab) {
    ImageView imageView = new ImageView();
    imageView.setFitWidth(PREFAB_ICON_SIZE);
    imageView.setFitHeight(PREFAB_ICON_SIZE);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);

    Image prefabImage = loadPrefabImage(prefab);
    imageView.setImage(prefabImage != null ? prefabImage : placeholderImage);
    if (prefabImage == null) {
      imageView.getStyleClass().add("placeholder-prefab-icon");
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

    String tooltipText = String.format("%s\nGroup: %s\nGame: %s", prefab.type(), prefab.group(), prefab.gameName());
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

    prefabGrid.getChildren().add(prefabContainer);
  }

  /**
   * Attempts to load the image for a prefab based on its SpriteData.
   */
  private Image loadPrefabImage(BlueprintData prefab) {
    if (prefab.spriteData() == null || prefab.spriteData().spriteFile() == null || prefab.spriteData().spriteFile().getPath().isEmpty()) {
      return null;
    }

    try {
      String gameAssetBasePath = "data/gameData/" + prefab.gameName() + "/sprites/";

      String relativePath = prefab.spriteData().spriteFile().getPath();
      File imageFile = Paths.get(gameAssetBasePath, relativePath).toFile();

      if (imageFile.exists() && imageFile.isFile()) {
        try (FileInputStream fis = new FileInputStream(imageFile)) {
          return new Image(fis, PREFAB_ICON_SIZE, PREFAB_ICON_SIZE, true, true);
        }
      } else {
        LOG.trace("Prefab sprite file not found at expected path: {}", imageFile.getAbsolutePath());
        return null;
      }
    } catch (Exception e) {
      LOG.error("Error loading image for prefab '{}' (Path: {}): {}",
          prefab.type(), prefab.spriteData().spriteFile().getPath(), e.getMessage());
      return null;
    }
  }

  public ObjectProperty<BlueprintData> selectedPrefabProperty() {
    return selectedPrefab;
  }

  public BlueprintData getSelectedPrefab() {
    return selectedPrefab.get();
  }

  // --- EditorViewListener Implementation ---

  public void onPrefabsChanged() {
    LOG.info("PrefabPalettePane notified: Prefabs changed. Reloading palette.");
    loadAvailablePrefabs();
  }

  @Override public void onObjectRemoved(UUID objectId) {}
  @Override public void onObjectUpdated(UUID objectId) {}
  @Override public void onSelectionChanged(UUID selectedObjectId) {}

  @Override
  public void onObjectAdded(UUID objectId) {

  }

  @Override public void onDynamicVariablesChanged() {}
  @Override public void onErrorOccurred(String errorMessage) {}
}
