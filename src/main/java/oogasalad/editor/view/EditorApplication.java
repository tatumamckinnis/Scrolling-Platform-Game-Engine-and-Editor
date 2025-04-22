package oogasalad.editor.view;

import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import oogasalad.editor.controller.ConcreteEditorController;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.controller.EditorMaker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple JavaFX Application entry point for launching and testing the Editor UI independently.
 * Instantiates the EditorComponentFactory with a ConcreteEditorController.
 *
 * @author Tatum McKinnis
 */
public class EditorApplication extends Application {

  private static final Logger LOG = LogManager.getLogger();
  private String gameDirectoryPath;

  /**
   * Starts the JavaFX application, creates the editor scene, and shows the primary stage. Includes
   * basic initialization error handling.
   *
   * @param primaryStage The primary stage for this application.
   */
  @Override
  public void start(Stage primaryStage) {
    new EditorMaker(primaryStage);
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
