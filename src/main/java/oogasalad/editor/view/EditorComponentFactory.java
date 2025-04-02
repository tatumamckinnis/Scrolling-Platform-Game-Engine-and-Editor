package oogasalad.editor.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.EditorDataAPI;

import oogasalad.editor.view.tools.EnemyObjectPlacementTool;
import oogasalad.editor.view.tools.EntityObjectPlacementTool;
import oogasalad.editor.view.tools.ObjectPlacementTool;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;


/**
 * The EditorComponentFactory creates UI panes for the editor manager to load.
 * This class has been extended to include property and input tab panels.
 *
 * @author Luke Nam,Tatum McKinnis
 */
public class EditorComponentFactory {
  private static final String editorComponentPropertiesFilepath = "/oogasalad/screens/editorScene.properties";
  private static final Properties editorComponentProperties = new Properties();

  private EditorGameView gameView;
  private EntityObjectPlacementTool entityTool;
  private EnemyObjectPlacementTool enemyTool;
  private ObjectPlacementTool currentTool;

  private EditorDataAPI editorDataAPI;
  private InputTabController inputTabController;
  private EditorAppAPI editorAppAPI;

  // Keep track of the currently selected object
  private UUID selectedObjectId;

  /**
   * Load the EditorComponentFactory
   *
   * @param editorDataAPI The API for interacting with editor data
   */
  public EditorComponentFactory(EditorDataAPI editorDataAPI) {
    this.editorDataAPI = editorDataAPI;
    this.inputTabController = new InputTabController(editorDataAPI);
    this.editorAppAPI = new EditorAppAPI(inputTabController);

    try {
      InputStream stream = getClass().getResourceAsStream(editorComponentPropertiesFilepath);
      editorComponentProperties.load(stream);
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Default constructor for backward compatibility
   */
  public EditorComponentFactory() {
    this(new EditorDataAPI());
  }

  /**
   * Create the editor scene consisting of the interactive map on the left and the
   * properties/input component menu on the right
   *
   * @return Scene for the interactive map and editable components
   */
  public Scene createEditorScene() {
    BorderPane root = new BorderPane();
    int editorWidth = Integer.parseInt(editorComponentProperties.getProperty("editor.width"));
    int editorHeight = Integer.parseInt(editorComponentProperties.getProperty("editor.height"));

    Pane mapPane = createMapPane(editorHeight);
    Pane componentsPane = createComponentPane(editorHeight);

    EditorAppAPI editorAppAPI = new EditorAppAPI(inputTabController);

    root.setLeft(mapPane);
    root.setRight(componentsPane);

    return new Scene(root, editorWidth, editorHeight);
  }

  /**
   * Create the map pane where game objects are placed and visualized
   *
   * @param height The height of the pane
   * @return The map pane
   */
  private Pane createMapPane(int height) {
    BorderPane mapPane = new BorderPane();
    int mapPaneWidth = Integer.parseInt(editorComponentProperties.getProperty("editor.map.width"));
    mapPane.setPrefWidth(mapPaneWidth);
    mapPane.setPrefHeight(height);
    mapPane.setStyle("-fx-background-color: lightblue;");

    Label mapLabel = new Label("Game Map");
    mapLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    HBox toolbarBox = createToolbar();

    int cellSize = 32;
    gameView = new EditorGameView(
        mapPaneWidth - 40,
        height - 150,
        cellSize,
        editorDataAPI,
        editorAppAPI);

    entityTool = new EntityObjectPlacementTool(gameView, editorDataAPI, "PLAYER", "/path/to/player.png");
    enemyTool = new EnemyObjectPlacementTool(gameView, editorDataAPI, "ENEMY", "/path/to/enemy.png");

    currentTool = entityTool;
    gameView.setCurrentTool(currentTool);

    VBox mapContent = new VBox(10);
    mapContent.setPadding(new Insets(20));
    mapContent.setAlignment(Pos.TOP_CENTER);
    mapContent.getChildren().addAll(mapLabel, toolbarBox, gameView);

    mapPane.setCenter(mapContent);

    return mapPane;
  }

  /**
   * Creates and returns an HBox toolbar containing toggle buttons
   * for selecting different tools in the game editor.
   *
   * <p>The toolbar consists of two toggle buttons:
   * one for adding entities and another for adding enemies.
   * The buttons are grouped using a ToggleGroup to ensure that only
   * one tool is selected at a time. The currently selected tool
   * is updated when a button is clicked.</p>
   *
   * @return an HBox containing the toolbar with tool selection buttons
   */
  private HBox createToolbar() {
    HBox toolbar = new HBox(10);
    toolbar.setPadding(new Insets(10));
    toolbar.setAlignment(Pos.CENTER);

    ToggleGroup toolGroup = new ToggleGroup();

    ToggleButton entityButton = new ToggleButton("Add Entity");
    entityButton.setToggleGroup(toolGroup);
    entityButton.setSelected(true);
    entityButton.setOnAction(e -> {
      currentTool = entityTool;
      gameView.setCurrentTool(currentTool);
    });

    ToggleButton enemyButton = new ToggleButton("Add Enemy");
    enemyButton.setToggleGroup(toolGroup);
    enemyButton.setOnAction(e -> {
      currentTool = enemyTool;
      gameView.setCurrentTool(currentTool);
    });

    toolbar.getChildren().addAll(entityButton, enemyButton);
    return toolbar;
  }


  /**
   * Create the component pane that holds property and input tabs
   *
   * @param height The height of the pane
   * @return The component pane
   */
  private Pane createComponentPane(int height) {
    BorderPane componentPane = new BorderPane();
    int componentPaneWidth = Integer.parseInt(editorComponentProperties.getProperty("editor.component.width"));
    componentPane.setPrefWidth(componentPaneWidth);
    componentPane.setPrefHeight(height);
    componentPane.setStyle("-fx-background-color: white;");

    Label componentsLabel = new Label("Object Properties");
    componentsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    TabPane tabPane = new TabPane();

    Tab propertiesTab = new Tab("Properties");
    propertiesTab.setClosable(false);
    Pane propertiesPane = createPropertiesPane();
    propertiesTab.setContent(propertiesPane);

    Tab inputTab = new Tab("Input");
    inputTab.setClosable(false);
    Pane inputPane = inputTabController.getInputTabPane();
    inputTab.setContent(inputPane);

    tabPane.getTabs().addAll(propertiesTab, inputTab);

    VBox componentContent = new VBox(10);
    componentContent.setPadding(new Insets(20));
    componentContent.getChildren().addAll(componentsLabel, tabPane);

    componentPane.setCenter(componentContent);

    return componentPane;
  }

  /**
   * Create the properties pane for editing object properties
   *
   * @return The properties pane
   */
  private Pane createPropertiesPane() {
    VBox propertiesPane = new VBox(10);
    propertiesPane.setPadding(new Insets(20));

    Label placeholderLabel = new Label("Object Properties Panel");
    placeholderLabel.setStyle("-fx-font-style: italic;");

    propertiesPane.getChildren().add(placeholderLabel);
    return propertiesPane;
  }
}