package oogasalad.editor.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import oogasalad.editor.view.panes.chat.ChatBotPane;
import oogasalad.editor.view.panes.properties.PropertiesTabComponentFactory;
import oogasalad.editor.view.panes.spriteProperties.SpriteTabComponentFactory;
import oogasalad.editor.view.resources.EditorResourceLoader;
import oogasalad.editor.view.panes.spriteCreation.SpriteAssetPane;
import oogasalad.editor.view.tools.ClearAllTool;
import oogasalad.editor.view.tools.DeleteTool;
import oogasalad.editor.view.tools.GameObjectPlacementTool;
import oogasalad.editor.view.tools.ObjectInteractionTool;
import oogasalad.editor.view.tools.OnClickTool;
import oogasalad.editor.view.tools.SelectionTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory class responsible for creating the main UI components of the Editor scene. Uses external
 * configuration for identifiers, properties, UI text, and styling. Delegates actions to the
 * EditorController and registers view components as listeners.
 *
 * @author Tatum McKinnis
 */
public class EditorComponentFactory {

  private static final Logger LOG = LogManager.getLogger(EditorComponentFactory.class);

  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/config/editor/resources/editor_component_factory_identifiers.properties";

  private final Properties editorProperties;
  private final Properties identifierProps;
  private ResourceBundle uiBundle;
  private final EditorController editorController;
  private final InputTabComponentFactory inputTabFactory;
  private final PropertiesTabComponentFactory propertiesTabFactory;
  private final SpriteTabComponentFactory spriteTabFactory;

  private EditorGameView gameView;
  private PrefabPalettePane prefabPalettePane;

  /**
   * Constructs the factory, loading necessary resources and initializing dependencies. Loads core
   * editor properties and UI text using {@link EditorResourceLoader}. Loads internal identifiers
   * (keys, IDs, paths) from its own properties file.
   *
   * @param editorController The main controller for editor actions.
   * @throws RuntimeException     if essential resources (editor properties, UI bundle, identifiers)
   *                              cannot be loaded.
   * @throws NullPointerException if editorController is null.
   */
  public EditorComponentFactory(EditorController editorController) {
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null. Cannot proceed.");

    this.identifierProps = loadIdentifierProperties();

    Properties tempEditorProperties = null;
    ResourceBundle tempUiBundle = null;
    String errorMessage = "Fatal: Failed to load essential editor resources.";

    try {
      String editorPropsPath = getId("editor.properties.path");
      String uiBundleBaseName = getId("ui.bundle.name");
      tempEditorProperties = EditorResourceLoader.loadProperties(editorPropsPath);
      tempUiBundle = EditorResourceLoader.loadResourceBundle(uiBundleBaseName);
      this.editorProperties = tempEditorProperties;
      this.uiBundle = tempUiBundle;
    } catch (Exception e) {
      LOG.fatal(errorMessage, e);
      throw new RuntimeException(errorMessage, e);
    }

    this.inputTabFactory = new InputTabComponentFactory(editorController);
    editorController.registerViewListener(inputTabFactory);
    this.propertiesTabFactory = new PropertiesTabComponentFactory(editorController);
    editorController.registerViewListener(propertiesTabFactory);
    this.spriteTabFactory = new SpriteTabComponentFactory(editorController);
    editorController.registerViewListener(spriteTabFactory);
    LOG.info("TabComponentFactories created and registered as listeners.");

    this.prefabPalettePane = createPrefabPalettePane();
    editorController.registerViewListener(prefabPalettePane);
    LOG.info("PrefabPalettePane created and registered.");

    LOG.info("EditorComponentFactory initialized.");
  }

  /**
   * Loads the identifier strings (keys, CSS classes, IDs, paths) from the properties file.
   * @return A Properties object containing the loaded identifiers.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadIdentifierProperties() {
    Properties props = new Properties();
    try (InputStream input = EditorComponentFactory.class.getResourceAsStream(
        IDENTIFIERS_PROPERTIES_PATH)) {
      if (input == null) {
        LOG.error("CRITICAL: Unable to find identifiers properties file: {}",
            IDENTIFIERS_PROPERTIES_PATH);
        throw new RuntimeException(
            "Missing required identifiers properties file: " + IDENTIFIERS_PROPERTIES_PATH);
      }
      props.load(input);
    } catch (IOException ex) {
      LOG.error("CRITICAL: Error loading identifiers properties file: {}",
          IDENTIFIERS_PROPERTIES_PATH, ex);
      throw new RuntimeException("Error loading identifiers properties file", ex);
    }
    return props;
  }

  /**
   * Retrieves an identifier value from the loaded identifier properties.
   *
   * @param key The key for the identifier.
   * @return The identifier string.
   * @throws RuntimeException If the key is not found.
   */
  private String getId(String key) {
    String value = identifierProps.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOG.error("Missing identifier in properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in properties file for key: " + key);
    }
    return value;
  }


  /**
   * Creates the main editor scene, assembling the major layout panes (map, assets, components)
   * using SplitPanes. Dimensions and styling are controlled by external properties and CSS.
   *
   * @return The fully assembled editor Scene object, ready to be set on the stage.
   */
  public Scene createEditorScene() {
    SplitPane root = new SplitPane();
    root.setId(getId("id.editor.root"));
    int editorWidth = getIntProperty(getId("prop.editor.width"),
        getDefaultInt("default.editor.width"));
    int editorHeight = getIntProperty(getId("prop.editor.height"),
        getDefaultInt("default.editor.height"));

    // Middle column: Map above Assets
    SplitPane middleSplit = new SplitPane();
    middleSplit.setOrientation(Orientation.VERTICAL);
    Pane mapPane   = createMapPane(editorHeight);
    Pane assetPane = createAssetPane();
    middleSplit.getItems().addAll(mapPane, assetPane);
    middleSplit.setDividerPositions(
        getDoubleProperty(getId("layout.left.split.divider"), 0.7)
    );

    // Left column: Chat
    Pane chatContainer = createChatPane();

    // Right column: Components
    Pane componentsContainer = createComponentPane(editorHeight);

    // Assemble root: Chat | (Map/Assets) | Components
    root.getItems().addAll(chatContainer, middleSplit, componentsContainer);
    root.setDividerPositions(0.2, 0.8);

    // Scene & CSS
    Scene scene = new Scene(root, editorWidth, editorHeight);
    String cssPath = getId("css.path");
    String css     = Objects.requireNonNull(
        getClass().getResource(cssPath)
    ).toExternalForm();
    scene.getStylesheets().add(css);

    LOG.info("Editor scene created with dimensions {}x{}", editorWidth, editorHeight);
    return scene;
  }


  /**
   * Creates the main map viewing and interaction area, including the title, toolbar, and the game
   * view canvas.
   *
   * @param height The suggested initial height, primarily used for context if needed.
   * @return A Pane containing the map section components.
   */
  private Pane createMapPane(int height) {
    BorderPane mapPane = new BorderPane();
    mapPane.setId(getId("id.map.pane"));

    Label mapLabel = createStyledLabel(uiBundle.getString(getId("key.map.title")),
        getId("style.header.label"));
    mapLabel.setId(getId("id.map.label"));

    createGameView(uiBundle);
    editorController.registerViewListener(gameView);
    LOG.debug("EditorGameView registered as listener.");

    HBox toolbarBox = createToolbar();

    VBox mapContent = new VBox();
    mapContent.setId(getId("id.map.content.vbox"));
    mapContent.setAlignment(Pos.TOP_CENTER);
    mapContent.getChildren().addAll(mapLabel, toolbarBox, gameView);

    VBox.setVgrow(gameView, Priority.ALWAYS);

    mapPane.setCenter(mapContent);
    LOG.debug("Map pane created.");
    return mapPane;
  }

  /**
   * Creates and initializes the {@link EditorGameView} instance using configuration values loaded
   * from properties and passes necessary dependencies.
   *
   * @param resourceBundle The UI ResourceBundle for localized text (e.g., error messages).
   */
  private void createGameView(ResourceBundle resourceBundle) { // Add parameter
    int cellSize = getIntProperty(getId("prop.cell.size"), getDefaultInt("default.cell.size"));
    double zoomScale = getDoubleProperty(getId("prop.zoom.scale"),
        getDefaultDouble("default.zoom.scale"));
    gameView = new EditorGameView(cellSize, zoomScale, editorController, prefabPalettePane,
        resourceBundle);
    LOG.debug("EditorGameView created with cell size {}", cellSize);
  }

  /**
   * Creates the {@link PrefabPalettePane} instance.
   *
   * @return A new PrefabPalettePane.
   */
  private PrefabPalettePane createPrefabPalettePane() {
    return new PrefabPalettePane(editorController);
  }

  /**
   * Creates the asset pane located below the map pane, containing tabs for prefabs and sprites.
   *
   * @return A Pane containing the asset tabs.
   */
  private Pane createAssetPane() {
    BorderPane assetPane = new BorderPane();
    assetPane.setId(getId("id.prefab.pane"));
    assetPane.setPrefHeight(getDoubleProperty(getId("layout.asset.pane.height"), 200.0));

    TabPane assetTabs = new TabPane();
    assetTabs.setId(getId("id.asset.tabs"));
    assetTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    Tab prefabsTab = new Tab(uiBundle.getString(getId("key.prefabs.tab.title")),
        this.prefabPalettePane);
    prefabsTab.setId(getId("id.prefabs.tab"));

    SpriteAssetPane spriteAssetPane = new SpriteAssetPane(editorController, null);
    Tab spritesTab = new Tab(uiBundle.getString(getId("key.sprites.tab.title")), spriteAssetPane);
    spritesTab.setId(getId("id.sprites.tab"));

    assetTabs.getTabs().addAll(prefabsTab, spritesTab);
    assetPane.setCenter(assetTabs);

    LOG.debug("Asset pane (Prefabs/Sprites) created.");
    return assetPane;
  }

  private Pane createChatPane() {
    // Header label
    Label chatLabel = createStyledLabel(
        uiBundle.getString(getId("key.chatbot.tab.title")),
        getId("style.header.label")
    );
    chatLabel.setId(getId("id.chat.label"));

    // Chat pane UI
    ChatBotPane chatBotPane = new ChatBotPane(editorController, /* ownerWindow */ null);
    chatBotPane.setId(getId("id.chat.pane"));
    chatBotPane.setPrefHeight(
        getDoubleProperty(getId("layout.chat.pane.height"), 200.0)
    );

    // Container VBox
    VBox container = new VBox(5, chatLabel, chatBotPane);
    container.setId(getId("id.chat.container"));
    VBox.setVgrow(chatBotPane, Priority.ALWAYS);
    return container;
  }



  /**
   * Creates the toolbar HBox containing toggle buttons for selecting interaction tools (placement,
   * selection, deletion). Relies on the {@code gameView} instance having been created previously.
   *
   * @return HBox node representing the toolbar.
   * @throws IllegalStateException if {@code gameView} has not been initialized.
   */
  private HBox createToolbar() {
    if (gameView == null) {
      String errorMsg = uiBundle.getString(getId("key.error.gameview.needed"));
      LOG.error(errorMsg);
      throw new IllegalStateException(errorMsg);
    }

    HBox toolbar = new HBox();
    toolbar.setId(getId("id.map.toolbar"));
    toolbar.setAlignment(Pos.CENTER);

    ToggleGroup toolGroup = new ToggleGroup();

    String entityType = editorProperties.getProperty(getId("prop.entity.type"),
        getId("default.entity.type"));
    String entityPrefix = editorProperties.getProperty(getId("prop.entity.prefix"),
        getId("default.entity.prefix"));

    ObjectInteractionTool entityTool = new GameObjectPlacementTool(gameView, editorController,
        entityType, entityPrefix);
    ToggleButton entityButton = createToolToggleButton(toolGroup,
        uiBundle.getString(getId("key.add.entity.tool")), entityTool, false);

    ObjectInteractionTool selectTool = new SelectionTool(gameView, editorController);
    ToggleButton selectButton = createToolToggleButton(toolGroup,
        uiBundle.getString(getId("key.select.tool")), selectTool, false);

    ObjectInteractionTool deleteTool = new DeleteTool(gameView, editorController);
    ToggleButton deleteButton = createToolToggleButton(toolGroup,
        uiBundle.getString(getId("key.delete.tool")), deleteTool, false);

    OnClickTool clearAllTool = new ClearAllTool(gameView, editorController);
    Button clearAllObjectsButton = createOnClickToolToggleButton(
        uiBundle.getString(getId("key.clear.all.tool")), clearAllTool);

    toolbar.getChildren().addAll(entityButton, selectButton, deleteButton, clearAllObjectsButton);
    gameView.updateCurrentTool(null);
    LOG.debug("Toolbar created with configured placement tools.");
    return toolbar;
  }

  /**
   * Helper method to create a styled ToggleButton for the toolbar, linking it to a specific tool
   * and adding it to a ToggleGroup. Sets up the action to update the current tool in the game
   * view.
   *
   * @param group    The ToggleGroup the button belongs to.
   * @param text     The text label for the button.
   * @param tool     The {@link ObjectInteractionTool} activated by this button.
   * @param selected Whether this button should be initially selected.
   * @return The configured ToggleButton.
   */
  private ToggleButton createToolToggleButton(ToggleGroup group, String text,
      ObjectInteractionTool tool, boolean selected) {
    ToggleButton button = new ToggleButton(text);
    button.setToggleGroup(group);
    button.setSelected(selected);
    button.setOnAction(e -> {
      if (gameView != null) {
        if (button.isSelected()) {
          gameView.updateCurrentTool(tool);
          LOG.debug(String.format(uiBundle.getString(getId("key.log.tool.selected")),
              tool.getClass().getSimpleName()));
        } else {
          if (group.getSelectedToggle() == null) {
            gameView.updateCurrentTool(null);
            LOG.debug(String.format(uiBundle.getString(getId("key.log.tool.deselected")),
                tool.getClass().getSimpleName()));
          }
        }
      }
    });
    button.getStyleClass().add(getId("style.tool.button"));
    return button;
  }

  /**
   * Helper method to create a styled Button for the toolbar, linking it to a specific tool
   *
   * @param text The text label for the button.
   * @param tool The {@link OnClickTool} activated by this button.}
   * @return a new Button
   */
  private Button createOnClickToolToggleButton(String text, OnClickTool tool) {
    Button button = new Button(text);
    button.setOnAction(e -> {
      if (gameView != null) {
        tool.execute();
      }
    });
    button.getStyleClass().add(getId("style.tool.button"));
    return button;
  }

  /**
   * Creates the right-hand component pane containing tabs for Properties and Input configuration.
   *
   * @param height The suggested initial height, primarily used for context if needed.
   * @return A Pane containing the component tabs.
   */
  private Pane createComponentPane(int height) {
    BorderPane componentPane = new BorderPane();
    componentPane.setId(getId("id.component.pane"));

    Label componentsLabel = createStyledLabel(uiBundle.getString(getId("key.properties.title")),
        getId("style.header.label"));
    componentsLabel.setId(getId("id.components.label"));

    TabPane tabPane = new TabPane();
    tabPane.setId(getId("id.component.tab.pane"));
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    Tab propertiesTab = createPropertiesTab();
    Tab inputTab = createInputTab();
    Tab spriteTab = createSpriteTab();
    tabPane.getTabs().addAll(propertiesTab, inputTab, spriteTab);

    VBox componentContent = new VBox();
    componentContent.setId(getId("id.component.content.vbox"));
    componentContent.getChildren().addAll(componentsLabel, tabPane);
    VBox.setVgrow(tabPane, Priority.ALWAYS);

    componentPane.setCenter(componentContent);
    LOG.debug("Component pane created.");
    return componentPane;
  }

  /**
   * Creates the "Properties" tab using the {@link PropertiesTabComponentFactory}.
   *
   * @return The configured Properties Tab.
   */
  private Tab createPropertiesTab() {
    Tab propertiesTab = new Tab(uiBundle.getString(getId("key.properties.tab")));
    propertiesTab.setId(getId("id.properties.tab"));
    propertiesTab.setClosable(false);

    ScrollPane propertiesPane = propertiesTabFactory.createPropertiesPane();

    propertiesTab.setContent(propertiesPane);
    LOG.debug("Properties tab created.");
    return propertiesTab;
  }

  /**
   * Creates the "Input" tab using the {@link InputTabComponentFactory}.
   *
   * @return The configured Input Tab.
   */
  private Tab createInputTab() {
    Tab inputTab = new Tab(uiBundle.getString(getId("key.input.tab")));
    inputTab.setId(getId("id.input.tab"));
    inputTab.setClosable(false);

    Pane inputPane = inputTabFactory.createInputTabPanel();
    inputTab.setContent(inputPane);
    LOG.debug("Input tab created.");
    return inputTab;
  }

  /**
   * Creates the "Sprites" tab using the {@link SpriteTabComponentFactory}.
   *
   * @return The configured Sprite Tab
   */
  private Tab createSpriteTab() {
    Tab spriteTab = new Tab(uiBundle.getString(getId("key.spriteProperty.tab")));
    spriteTab.setId(getId("id.spriteProperty.tab"));
    spriteTab.setClosable(false);

    ScrollPane spritePane = spriteTabFactory.createSpritePane();
    spriteTab.setContent(spritePane);
    LOG.debug("Sprite tab created.");
    return spriteTab;
  }


  /**
   * Safely retrieves an integer property value from the loaded editor properties file. Uses a
   * default value if the key is not found or the value is not a valid integer.
   *
   * @param key          The property key (retrieved via getId).
   * @param defaultValue The default integer value to use if lookup or parsing fails.
   * @return The integer value from properties or the default value.
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
   * Safely retrieves an integer property value from the loaded identifier properties file. Used for
   * default integer values originally defined as constants.
   *
   * @param idKey The identifier key for the default value.
   * @return The integer value from identifier properties.
   * @throws RuntimeException if the identifier key is not found or parsing fails.
   */
  int getDefaultInt(String idKey) {
    String value = getId(idKey);
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      LOG.error("Invalid integer format for identifier key '{}': value='{}'", idKey, value, e);
      throw new RuntimeException("Invalid integer format for identifier key '" + idKey + "'", e);
    }
  }

  /**
   * Safely retrieves a double property value from the loaded editor properties file. Uses a default
   * value if the key is not found or the value is not a valid double.
   *
   * @param key          The property key (retrieved via getId).
   * @param defaultValue The default double value to use if lookup or parsing fails.
   * @return The double value from properties or the default value.
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
   * Safely retrieves a double property value from the loaded identifier properties file. Used for
   * default double values originally defined as constants.
   *
   * @param idKey The identifier key for the default value.
   * @return The double value from identifier properties.
   * @throws RuntimeException if the identifier key is not found or parsing fails.
   */
  double getDefaultDouble(String idKey) {
    String value = getId(idKey);
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      LOG.error("Invalid double format for identifier key '{}': value='{}'", idKey, value, e);
      throw new RuntimeException("Invalid double format for identifier key '" + idKey + "'", e);
    }
  }


  /**
   * Creates a Label node with the specified text and applies a given CSS style class.
   *
   * @param text       The text content for the label.
   * @param styleClass The CSS style class name to apply to the label.
   * @return The configured Label node.
   */
  private Label createStyledLabel(String text, String styleClass) {
    Label label = new Label(text);
    if (styleClass != null && !styleClass.trim().isEmpty()) {
      label.getStyleClass().add(styleClass);
    } else {
      LOG.warn("Attempted to apply null or empty style class to label with text: {}", text);
    }
    return label;
  }
}