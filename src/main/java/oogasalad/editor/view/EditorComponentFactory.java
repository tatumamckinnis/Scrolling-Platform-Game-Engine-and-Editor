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

/**
 * The EditorComponentFactory creates UI panes for the editor manager to load.
 * This class has been extended to include property and input tab panels.
 *
 * @author Luke Nam,Tatum McKinnis
 */
public class EditorComponentFactory {
  private static final String editorComponentPropertiesFilepath = "/oogasalad/screens/editorScene.properties";
  private static final Properties editorComponentProperties = new Properties();

  private EditorDataAPI editorAPI;
  private InputTabController inputTabController;

  // Keep track of the currently selected object
  private UUID selectedObjectId;

  /**
   * Load the EditorComponentFactory
   *
   * @param editorAPI The API for interacting with editor data
   */
  public EditorComponentFactory(EditorDataAPI editorAPI) {
    this.editorAPI = editorAPI;
    this.inputTabController = new InputTabController(editorAPI);

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

    // Create the game view
    int cellSize = 32; // Default cell size
    EditorGameView gameView = new EditorGameView(
        mapPaneWidth - 40, // Leave some margin
        height - 100, // Leave some margin
        cellSize);

    // Add selection handler
    gameView.setOnGridClick(event -> {
      int gridX = (int)(event.getX() / gameView.getCellSize());
      int gridY = (int)(event.getY() / gameView.getCellSize());

      // This would normally create or select an object at this position
      // For demo purposes, we'll just use a dummy UUID
      UUID selectedId = UUID.randomUUID();
      setSelectedObject(selectedId);
    });

    VBox mapContent = new VBox(10);
    mapContent.setPadding(new Insets(20));
    mapContent.setAlignment(Pos.TOP_CENTER);
    mapContent.getChildren().addAll(mapLabel, gameView);

    mapPane.setCenter(mapContent);

    return mapPane;
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

  /**
   * Set the currently selected game object
   *
   * @param objectId The UUID of the selected object
   */
  public void setSelectedObject(UUID objectId) {
    this.selectedObjectId = objectId;
    inputTabController.setSelectedObject(objectId);

    // This would also update other components that depend on the selection
  }
}