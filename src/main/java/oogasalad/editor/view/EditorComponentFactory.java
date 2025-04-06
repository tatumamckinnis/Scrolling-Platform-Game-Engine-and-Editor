package oogasalad.editor.view;

import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.resources.EditorResourceLoader;
import oogasalad.editor.view.tools.GameObjectPlacementTool;
import oogasalad.editor.view.tools.ObjectPlacementTool;
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
  private static final String PROP_ZOOM = "editor.map.zoom";
  private static final String PROP_PLAYER_TYPE = "editor.tool.player.type";
  private static final String PROP_PLAYER_PREFIX = "editor.tool.player.prefix";
  private static final String PROP_ENEMY_TYPE = "editor.tool.enemy.type";
  private static final String PROP_ENEMY_PREFIX = "editor.tool.enemy.prefix";

  private static final String KEY_MAP_TITLE = "mapTitle";
  private static final String KEY_ADD_ENTITY_TOOL = "addEntityTool";
  private static final String KEY_ADD_ENEMY_TOOL = "addEnemyTool";
  private static final String KEY_PROPERTIES_TITLE = "propertiesTitle";
  private static final String KEY_PROPERTIES_TAB = "propertiesTab";
  private static final String KEY_INPUT_TAB = "inputTab";
  private static final String KEY_PROPERTIES_PLACEHOLDER = "propertiesPlaceholder";

  private static final double DEFAULT_PADDING = 10.0;
  private static final double DEFAULT_SPACING = 10.0;
  private static final double SECTION_PADDING = 20.0;

  private static final String DEFAULT_PLAYER_TYPE = "PLAYER";
  private static final String DEFAULT_PLAYER_PREFIX = "Player_";
  private static final String DEFAULT_ENEMY_TYPE = "ENEMY";
  private static final String DEFAULT_ENEMY_PREFIX = "Enemy_";

  private final Properties editorProperties;
  private final ResourceBundle uiBundle;
  private final EditorController editorController; // Use the controller interface
  private final InputTabComponentFactory inputTabFactory;

  private EditorGameView gameView;

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
    LOG.info("InputTabComponentFactory created and registered as listener.");

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
    Pane prefabPane = createPrefabPane();

    leftSplit.getItems().addAll(mapPane, prefabPane);
    leftSplit.setDividerPositions(0.7); // TODO: Make this a property

    Pane componentsPane = createComponentPane(editorHeight);

    root.getItems().addAll(leftSplit, componentsPane);
    root.setDividerPositions(0.6); // TODO: Make this a property

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

  /**
   * Creates the left pane containing the map view and toolbar.
   */
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
    double zoomScale = getDoubleProperty(PROP_ZOOM, 1);

    gameView = new EditorGameView(cellSize, zoomScale, editorController);
    LOG.debug("EditorGameView created with cell size {}", cellSize);
  }

  private Pane createPrefabPane() {
    VBox prefabPane = new VBox();
    prefabPane.setId("prefab-pane");
    prefabPane.setPrefHeight(200);
    // TODO: Actually put some content later. For now, do nothing.

    LOG.debug("Prefab pane created (empty).");
    return prefabPane;
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

    String playerType = editorProperties.getProperty(PROP_PLAYER_TYPE, DEFAULT_PLAYER_TYPE);
    String playerPrefix = editorProperties.getProperty(PROP_PLAYER_PREFIX, DEFAULT_PLAYER_PREFIX);
    String enemyType = editorProperties.getProperty(PROP_ENEMY_TYPE, DEFAULT_ENEMY_TYPE);
    String enemyPrefix = editorProperties.getProperty(PROP_ENEMY_PREFIX, DEFAULT_ENEMY_PREFIX);

    ObjectPlacementTool entityTool = new GameObjectPlacementTool(gameView, editorController,
        playerType, playerPrefix);
    ObjectPlacementTool enemyTool = new GameObjectPlacementTool(gameView, editorController,
        enemyType, enemyPrefix);

    ToggleButton entityButton = createToolToggleButton(toolGroup,
        uiBundle.getString(KEY_ADD_ENTITY_TOOL), entityTool, true);
    ToggleButton enemyButton = createToolToggleButton(toolGroup,
        uiBundle.getString(KEY_ADD_ENEMY_TOOL), enemyTool, false);

    toolbar.getChildren().addAll(entityButton, enemyButton);
    gameView.setCurrentTool(entityTool); // Set default tool
    LOG.debug("Toolbar created with configured placement tools.");
    return toolbar;
  }

  /**
   * Helper method to create a styled ToggleButton for the toolbar.
   */
  private ToggleButton createToolToggleButton(ToggleGroup group, String text,
      ObjectPlacementTool tool, boolean selected) {
    ToggleButton button = new ToggleButton(text);
    button.setToggleGroup(group);
    button.setSelected(selected);
    button.setOnAction(e -> {
      if (gameView != null && button.isSelected()) {
        gameView.setCurrentTool(tool);
        LOG.debug("Tool selected: {}", tool.getClass().getSimpleName());
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
    Pane propertiesPane = createPropertiesPane();
    propertiesTab.setContent(propertiesPane);
    LOG.debug("Properties tab created.");
    return propertiesTab;
  }

  /**
   * Creates the content pane for the "Properties" tab (currently a placeholder).
   */
  private Pane createPropertiesPane() {
    VBox propertiesPane = new VBox(DEFAULT_SPACING);
    propertiesPane.setPadding(new Insets(SECTION_PADDING));
    propertiesPane.setId("properties-pane-content"); // Apply CSS ID
    propertiesPane.setAlignment(Pos.CENTER); // Center placeholder

    // TODO: Implement the actual property editing UI here.
    Label placeholderLabel = new Label(uiBundle.getString(KEY_PROPERTIES_PLACEHOLDER));
    placeholderLabel.getStyleClass().add("placeholder-label"); // Apply CSS Class
    propertiesPane.getChildren().add(placeholderLabel);

    LOG.debug("Properties pane content created (placeholder).");
    return propertiesPane;
  }

  /**
   * Creates the "Input" tab using the InputTabComponentFactory.
   */
  private Tab createInputTab() {
    Tab inputTab = new Tab(uiBundle.getString(KEY_INPUT_TAB));
    inputTab.setId("input-tab"); // Apply CSS ID
    inputTab.setClosable(false);
    Pane inputPane = inputTabFactory.createInputTabPanel();
    inputTab.setContent(inputPane);
    LOG.debug("Input tab created.");
    return inputTab;
  }


  /**
   * Safely retrieves an integer property from the loaded properties.
   */
  private int getIntProperty(String key, int defaultValue) {
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
  private double getDoubleProperty(String key, double defaultValue) {
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