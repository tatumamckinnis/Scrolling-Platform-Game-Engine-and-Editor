package oogasalad.editor.view;

import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.resources.EditorResourceLoader;
import oogasalad.editor.view.sprites.SpriteAssetPane;
import oogasalad.editor.view.tools.GameObjectPlacementTool;
import oogasalad.editor.view.tools.ObjectInteractionTool;
import oogasalad.editor.view.tools.SelectionTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory class responsible for creating the main UI components of the Editor scene. Uses external
 * configuration, delegates actions to the EditorController, and registers view components as
 * listeners. (DESIGN-01, DESIGN-09: MVC, DESIGN-15, DESIGN-20: Factory Pattern, Observer Pattern)
 *
 * @author Tatum McKinnis
 */
public class EditorComponentFactory {

  private static final Logger LOG = LogManager.getLogger(EditorComponentFactory.class);

  private static final String EDITOR_PROPERTIES_PATH = "/oogasalad/screens/editorScene.properties";
  private static final String UI_BUNDLE_NAME = "EditorUI";
  private static final String CSS_PATH = "/oogasalad/css/editor/editor.css";

  private static final String PROP_EDITOR_WIDTH = "editor.width";
  private static final String PROP_EDITOR_HEIGHT = "editor.height";
  private static final String PROP_MAP_WIDTH = "editor.map.width";
  private static final String PROP_COMPONENT_WIDTH = "editor.component.width";
  private static final String PROP_CELL_SIZE = "editor.map.cellSize";
  private static final String PROP_ZOOM_SCALE = "editor.map.zoomScale";
  private static final String PROP_ZOOM_STEP = "editor.map.zoomStep";
  private static final String PROP_ENTITY_TYPE = "editor.tool.entity.type";
  private static final String PROP_ENTITY_PREFIX = "editor.tool.entity.prefix";

  private static final String KEY_MAP_TITLE = "mapTitle";
  private static final String KEY_ADD_ENTITY_TOOL = "addEntityTool";
  private static final String KEY_SELECT_TOOL = "selectTool";
  private static final String KEY_PROPERTIES_TITLE = "propertiesTitle";
  private static final String KEY_PROPERTIES_TAB = "propertiesTab";
  private static final String KEY_INPUT_TAB = "inputTab";
  private static final String KEY_PROPERTIES_PLACEHOLDER = "propertiesPlaceholder";

  private static final double DEFAULT_PADDING = 10.0;
  private static final double DEFAULT_SPACING = 10.0;
  private static final double SECTION_PADDING = 20.0;

  private static final String DEFAULT_ENTITY_TYPE = "ENTITY";
  private static final String DEFAULT_ENTITY_PREFIX = "Entity_";

  private final Properties editorProperties;
  private final ResourceBundle uiBundle;
  private final EditorController editorController;
  private final InputTabComponentFactory inputTabFactory;
  private final PropertiesTabComponentFactory propertiesTabFactory;

  private EditorGameView gameView;
  private PrefabPalettePane prefabPalettePane;

  /**
   * Constructs the factory, loading necessary resources and initializing dependencies.
   *
   * @param editorController The main controller for editor actions.
   */
  public EditorComponentFactory(EditorController editorController) {
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");

    try {
      this.editorProperties = EditorResourceLoader.loadProperties(EDITOR_PROPERTIES_PATH);
      this.uiBundle = EditorResourceLoader.loadResourceBundle(UI_BUNDLE_NAME);
    } catch (Exception e) {
      LOG.fatal("Failed to load essential resources. Cannot continue.", e);
      throw new RuntimeException("Failed to load essential resources.", e);
    }

    this.inputTabFactory = new InputTabComponentFactory(editorController);
    editorController.registerViewListener(inputTabFactory);
    this.propertiesTabFactory = new PropertiesTabComponentFactory(editorController);
    editorController.registerViewListener(propertiesTabFactory);
    LOG.info("TabComponentFactories created and registered as listeners.");

    this.prefabPalettePane = createPrefabPalettePane();
    LOG.info("PrefabPalettePane created.");

    LOG.info("EditorComponentFactory initialized.");
  }

  /**
   * Creates the main editor scene, assembling the map pane and component pane.
   *
   * @return The fully assembled editor Scene.
   */
  public Scene createEditorScene() {
    SplitPane root = new SplitPane();
    root.setId("editor-root");
    int editorWidth = getIntProperty(PROP_EDITOR_WIDTH, 1200);
    int editorHeight = getIntProperty(PROP_EDITOR_HEIGHT, 800);

    SplitPane leftSplit = new SplitPane();
    leftSplit.setOrientation(Orientation.VERTICAL);

    Pane mapPane = createMapPane(editorHeight);
    PrefabPalettePane prefabPalettePane = createPrefabPalettePane();
    Pane assetPane = createAssetPane();

    leftSplit.getItems().addAll(mapPane, assetPane);
    leftSplit.setDividerPositions(0.7); // TODO: Make this a property

    Pane componentsPane = createComponentPane(editorHeight);

    root.getItems().addAll(leftSplit, componentsPane);

    root.setDividerPositions(0.7); // TODO: Make this a property

    Scene scene = new Scene(root, editorWidth, editorHeight);
    try {
      String css = Objects.requireNonNull(getClass().getResource(CSS_PATH),
          "CSS file not found: " + CSS_PATH).toExternalForm();
      scene.getStylesheets().add(css);
      LOG.info("Loaded stylesheet: {}", CSS_PATH);
    } catch (RuntimeException e) {
      LOG.error("Failed to load stylesheet from path: {}", CSS_PATH, e);
    }

    LOG.info("Editor scene created with dimensions {}x{}", editorWidth, editorHeight);
    return scene;
  }



  private Pane createMapPane(int height) {
    BorderPane mapPane = new BorderPane();
    mapPane.setId("map-pane");
    int mapPaneWidth = getIntProperty(PROP_MAP_WIDTH, 800);
    mapPane.setPrefSize(mapPaneWidth, height);

    Label mapLabel = createStyledLabel(uiBundle.getString(KEY_MAP_TITLE), "header-label");
    createGameView();
    editorController.registerViewListener(gameView);
    LOG.debug("EditorGameView registered as listener.");

    HBox toolbarBox = createToolbar();

    VBox mapContent = new VBox(DEFAULT_SPACING);
    mapContent.setPadding(new Insets(SECTION_PADDING));
    mapContent.setAlignment(Pos.TOP_CENTER);
    mapContent.getChildren().addAll(mapLabel, toolbarBox, gameView);

    VBox.setVgrow(gameView, Priority.ALWAYS);

    mapPane.setCenter(mapContent);
    LOG.debug("Map pane created.");
    return mapPane;
  }

  /**
   * Creates the interactive game view canvas.
   */
  private void createGameView() {
    int cellSize = getIntProperty(PROP_CELL_SIZE, 32);
    double zoomScale = getDoubleProperty(PROP_ZOOM_SCALE, 1);
    double zoomStep = getDoubleProperty(PROP_ZOOM_STEP, 0.05);

    gameView = new EditorGameView(cellSize, zoomScale, editorController, prefabPalettePane);
    LOG.debug("EditorGameView created with cell size {}", cellSize);
  }

  private PrefabPalettePane createPrefabPalettePane() {
    return new PrefabPalettePane(editorController);
  }

  private Pane createAssetPane() {
    BorderPane assetPane = new BorderPane();
    assetPane.setId("prefab-pane");
    assetPane.setPrefHeight(200);

    TabPane assetTabs = new TabPane();
    assetTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    Tab prefabsTab = new Tab("Prefabs", prefabPalettePane);
    ListView<String> prefabList = new ListView<>();
    prefabList.setPlaceholder(new Label("No prefabs yet"));
    Tab spritesTab = new Tab("Sprites", new SpriteAssetPane(assetPane.getScene() == null ? null : assetPane.getScene().getWindow()));

    assetTabs.getTabs().addAll(prefabsTab, spritesTab);
    assetPane.setCenter(assetTabs);

    LOG.debug("Prefab‑and‑Sprite pane created.");
    return assetPane;
  }


  /**
   * Creates the toolbar with object placement tool selection buttons. Requires gameView to be
   * initialized.
   */
  private HBox createToolbar() {
    if (gameView == null) {
      LOG.error("createToolbar called before createGameView was successfully completed.");
      throw new IllegalStateException("GameView must be created before the toolbar.");
    }

    HBox toolbar = new HBox(DEFAULT_SPACING);
    toolbar.setId("map-toolbar");
    toolbar.setPadding(new Insets(DEFAULT_PADDING));
    toolbar.setAlignment(Pos.CENTER);

    ToggleGroup toolGroup = new ToggleGroup();

    String entityType = editorProperties.getProperty(PROP_ENTITY_TYPE, DEFAULT_ENTITY_TYPE);
    String entityPrefix = editorProperties.getProperty(PROP_ENTITY_PREFIX, DEFAULT_ENTITY_PREFIX);

    ObjectInteractionTool entityTool = new GameObjectPlacementTool(gameView, editorController,
        entityType, entityPrefix);
    ToggleButton entityButton = createToolToggleButton(toolGroup,
        uiBundle.getString(KEY_ADD_ENTITY_TOOL), entityTool, false);

    ObjectInteractionTool selectTool = new SelectionTool(gameView, editorController);
    ToggleButton selectButton = createToolToggleButton(toolGroup,
        uiBundle.getString(KEY_SELECT_TOOL), selectTool, false);

    toolbar.getChildren().addAll(entityButton, selectButton);
    gameView.updateCurrentTool(null);
    LOG.debug("Toolbar created with configured placement tools.");
    return toolbar;
  }

  /**
   * Helper method to create a styled ToggleButton for the toolbar.
   */
  private ToggleButton createToolToggleButton(ToggleGroup group, String text,
      ObjectInteractionTool tool, boolean selected) {
    ToggleButton button = new ToggleButton(text);
    button.setToggleGroup(group);
    button.setSelected(selected);
    button.setOnAction(e -> {
      if (gameView != null && button.isSelected()) {
        gameView.updateCurrentTool(tool);
        LOG.debug("Tool selected: {}", tool.getClass().getSimpleName());
      }
      if (gameView != null && !button.isSelected()) {
        gameView.updateCurrentTool(null);
        LOG.debug("Tool deselected: {}", tool.getClass().getSimpleName());
      }
    });
    button.getStyleClass().add("tool-button");
    return button;
  }

  /**
   * Creates the right-hand pane containing the properties and input tabs.
   */
  private Pane createComponentPane(int height) {
    BorderPane componentPane = new BorderPane();
    componentPane.setId("component-pane");
    int componentPaneWidth = getIntProperty(PROP_COMPONENT_WIDTH, 400);
    componentPane.setPrefSize(componentPaneWidth, height);

    Label componentsLabel = createStyledLabel(uiBundle.getString(KEY_PROPERTIES_TITLE),
        "header-label");

    TabPane tabPane = new TabPane();
    tabPane.setId("component-tab-pane");
    Tab propertiesTab = createPropertiesTab();
    Tab inputTab = createInputTab();
    tabPane.getTabs().addAll(propertiesTab, inputTab);

    VBox componentContent = new VBox(DEFAULT_SPACING);
    componentContent.setPadding(new Insets(SECTION_PADDING));
    componentContent.getChildren().addAll(componentsLabel, tabPane);

    componentPane.setCenter(componentContent);
    LOG.debug("Component pane created.");
    return componentPane;
  }

  /**
   * Creates the "Properties" tab.
   */
  private Tab createPropertiesTab() {
    Tab propertiesTab = new Tab(uiBundle.getString(KEY_PROPERTIES_TAB));
    propertiesTab.setId("properties-tab");
    propertiesTab.setClosable(false);

    ScrollPane propertiesPane = propertiesTabFactory.createPropertiesPane();

    propertiesTab.setContent(propertiesPane);
    LOG.debug("Properties tab created.");
    return propertiesTab;
  }

  /**
   * Creates the "Input" tab using the InputTabComponentFactory.
   */
  private Tab createInputTab() {
    Tab inputTab = new Tab(uiBundle.getString(KEY_INPUT_TAB));
    inputTab.setId("input-tab");
    inputTab.setClosable(false);
    Pane inputPane = inputTabFactory.createInputTabPanel();
    inputTab.setContent(inputPane);
    LOG.debug("Input tab created.");
    return inputTab;
  }


  /**
   * Safely retrieves an integer property from the loaded properties.
   */
  int getIntProperty(String key, int defaultValue) {
    String value = editorProperties.getProperty(key);
    if (value != null) {
      try {
        return Integer.parseInt(value.trim());
      } catch (NumberFormatException e) {
        LOG.warn("Invalid integer format for property key '{}': value='{}', using default {}", key,
            value, defaultValue);
      }
    } else {
      LOG.warn("Property key '{}' not found, using default {}", key, defaultValue);
    }
    return defaultValue;
  }

  /**
   * Safely retrieves an double property from the loaded properties.
   */
  double getDoubleProperty(String key, double defaultValue) {
    String value = editorProperties.getProperty(key);
    if (value != null) {
      try {
        return Double.parseDouble(value.trim());
      } catch (NumberFormatException e) {
        LOG.warn("Invalid double format for property key '{}': value='{}', using default {}", key,
            value, defaultValue);
      }
    } else {
      LOG.warn("Property key '{}' not found, using default {}", key, defaultValue);
    }
    return defaultValue;
  }

  /**
   * Creates a Label and applies a CSS style class.
   */
  private Label createStyledLabel(String text, String styleClass) {
    Label label = new Label(text);
    label.getStyleClass().add(styleClass);
    return label;
  }
}
