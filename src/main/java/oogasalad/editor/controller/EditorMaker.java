package oogasalad.editor.controller;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import oogasalad.editor.view.EditorComponentFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for initializing and launching the Editor application. Sets up the MVC dependencies
 * (data, controller, view), configures the scene, and displays the main editor window.
 *
 * <p>This class is typically instantiated in the main application entry point
 * to launch the editor interface for a specific game or configuration.
 *
 * <p>If a {@code gameDirectoryPath} is provided, it will be used to set up game-specific data;
 * otherwise, a default fallback path is used.
 *
 * @author Alana Zinkin
 */
public class EditorMaker {

  private static final Logger LOG = LogManager.getLogger(EditorMaker.class);

  private String gameDirectoryPath;

  /**
   * Constructs an EditorMaker and initializes the editor UI using the provided primary stage.
   *
   * @param primaryStage the JavaFX {@link Stage} to attach the editor scene to
   */
  public EditorMaker(Stage primaryStage) {
    initialize(primaryStage);
  }

  /**
   * Initializes the core components of the editor application, including the data backend,
   * controller, view factory, and main scene. Also configures the primary stage.
   *
   * @param primaryStage the stage on which the editor UI should be displayed
   */
  private void initialize(Stage primaryStage) {
    LOG.info("Starting Editor Application...");
    try {
      // --- Dependency Setup ---

      // 1. Instantiate the backend data manager
      EditorDataAPI editorDataAPI = new EditorDataAPI();
      LOG.debug("EditorDataAPI instance created.");

      // 2. Create the editor controller
      EditorController editorController = new ConcreteEditorController(editorDataAPI);
      LOG.info("ConcreteEditorController created.");

      // Set the game directory path
      if (gameDirectoryPath != null && !gameDirectoryPath.isEmpty()) {
        editorDataAPI.setCurrentGameDirectoryPath(gameDirectoryPath);
        LOG.info("Game directory path set to: {}", gameDirectoryPath);
      } else {
        LOG.warn("Game directory path is not provided. Prefab sprite resolution may fail.");
        editorDataAPI.setCurrentGameDirectoryPath("data/gameData/unknown_game");
      }

      // 3. Create the view factory using the controller
      EditorComponentFactory factory = new EditorComponentFactory(editorController);
      LOG.info("EditorComponentFactory created.");

      // 4. Build the editor scene
      Scene editorScene = factory.createEditorScene();
      LOG.info("Editor scene created.");

      // 5. Attach scene to stage and show it
      primaryStage.setTitle("OOGA Salad Game Editor");
      primaryStage.setScene(editorScene);
      primaryStage.show();
      LOG.info("Primary stage configured and shown.");

    } catch (Exception e) {
      LOG.fatal("Failed to initialize and start Editor Application", e);
      showInitializationError(e);
    }
  }

  /**
   * Displays a modal error dialog with details about a critical initialization failure.
   *
   * @param e the exception that caused the startup failure
   */
  private void showInitializationError(Exception e) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Application Initialization Error");
    alert.setHeaderText("A critical error occurred during editor startup.");
    String message = (e.getMessage() != null) ? e.getMessage() : "An unknown error occurred.";
    alert.setContentText("Failed to initialize the editor application:\n" + message +
        "\n\nPlease check the logs for more details.");
    alert.showAndWait();
  }
}
