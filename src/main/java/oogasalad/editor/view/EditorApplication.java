//  oogasalad/editor/view/EditorApplication.java
package oogasalad.editor.view;

import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import oogasalad.editor.controller.ConcreteEditorController;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.controller.EditorDataAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple JavaFX Application entry point for launching and testing the Editor UI independently.
 * Instantiates the EditorComponentFactory with a ConcreteEditorController.
 *
 * @author Tatum McKinnis
 */
public class EditorApplication extends Application {

  private static final Logger LOG = LogManager.getLogger(EditorApplication.class);

  private String gameDirectoryPath;

  /**
   * Starts the JavaFX application, creates the editor scene, and shows the primary stage. Includes
   * basic initialization error handling.
   *
   * @param primaryStage The primary stage for this application.
   */
  @Override
  public void start(Stage primaryStage) {
    LOG.info("Starting Editor Application...");

    try {
      // --- Dependency Setup ---

      // 2. Instantiate the concrete controller, passing its dependencies
      EditorDataAPI editorDataAPI = new EditorDataAPI();
      EditorController editorController = new ConcreteEditorController(editorDataAPI); // Corrected line
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
        editorDataAPI.setCurrentGameDirectoryPath("data/gameData/unknown_game"); // Default or error handling
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

  /**
   * Main method to launch the standalone editor application.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void init() throws Exception {
    super.init();
    Parameters params = getParameters();
    List<String> raw = params.getRaw();
    if (raw.size() > 0) {
      gameDirectoryPath = raw.get(0);
    }
  }
}
