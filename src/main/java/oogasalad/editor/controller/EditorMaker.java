package oogasalad.editor.controller;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import oogasalad.editor.view.EditorApplication;
import oogasalad.editor.view.EditorComponentFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EditorMaker {

  private static final Logger LOG = LogManager.getLogger(EditorMaker.class);

  private String gameDirectoryPath;

  public EditorMaker(Stage primaryStage) {
    initialize(primaryStage);
  }

  private void initialize(Stage primaryStage) {
    LOG.info("Starting Editor Application...");
    try {
      // --- Dependency Setup ---

      // 2. Instantiate the concrete controller, passing its dependencies
      EditorDataAPI editorDataAPI = new EditorDataAPI();
      EditorController editorController = new ConcreteEditorController(
          editorDataAPI); // Corrected line
      LOG.info("ConcreteEditorController created.");

      // 1. Instantiate the backend facade/data manager required by the controller
      // Corrected line
      LOG.debug("EditorDataAPI instance created.");

      // Set the game directory path
      if (gameDirectoryPath != null && !gameDirectoryPath.isEmpty()) {
        editorDataAPI.setCurrentGameDirectoryPath(gameDirectoryPath);
        LOG.info("Game directory path set to: {}", gameDirectoryPath);
      } else {
        LOG.warn("Game directory path is not provided. Prefab sprite resolution may fail.");
        // Optionally, set a default or handle this more gracefully
        editorDataAPI.setCurrentGameDirectoryPath(
            "data/gameData/unknown_game"); // Default or error handling
      }

      // --- End Dependency Setup ---

      // 3. Create the view factory, passing the controller
      // Assumes EditorComponentFactory constructor now only requires EditorController
      EditorComponentFactory factory = new EditorComponentFactory(editorController);
      LOG.info("EditorComponentFactory created.");

      // 4. Create the scene using the factory
      Scene editorScene = factory.createEditorScene();
      LOG.info("Editor scene created.");

      // 5. Setup and show the stage
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
   * Shows an error dialog during application startup.
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
