package oogasalad;


import javafx.application.Application;
import javafx.stage.Stage;
import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.api.GameManagerAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Feel free to completely  change this code or delete it entirely.
 */
public class Main extends Application {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) {
        try {
            GameManagerAPI manager = new DefaultGameManager();
            LOG.info("Starting game...");
        } catch (Exception e) {
            LOG.warn("Error starting main");
        }
    }

    public static void main(String[] args) {
        launch(args); // Ensures JavaFX starts on the JavaFX Application Thread
    }
}
