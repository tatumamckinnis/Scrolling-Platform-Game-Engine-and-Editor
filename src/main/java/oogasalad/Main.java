package oogasalad;


import java.io.IOException;
import java.net.URISyntaxException;
import javafx.application.Application;
import javafx.stage.Stage;
import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.api.GameManagerAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class of application
 *
 * @author Alana Zinkin, Billy McCune, Jacob You, Tatum McKinnis, Aksel Bell, Gage Garcia, Luke Nam
 */
public class Main extends Application {

  private static final Logger LOG = LogManager.getLogger();

  /**
   * @param primaryStage the primary stage for this application, onto which the application scene
   *                     can be set. Applications may create other stages, if needed, but they will
   *                     not be primary stages.
   */
  @Override
  public void start(Stage primaryStage)
      throws URISyntaxException, IOException, InterruptedException {
    try {
      GameManagerAPI manager = new DefaultGameManager();
      LOG.info("Starting game...");
    } catch (Exception e) {
      e.printStackTrace();
      LOG.warn("Error starting main.", e.getMessage());
    }
  }

  /**
   * main method of application
   *
   * @param args
   */
  public static void main(String[] args) {
    launch(args); // Ensures JavaFX starts on the JavaFX Application Thread
  }
}
