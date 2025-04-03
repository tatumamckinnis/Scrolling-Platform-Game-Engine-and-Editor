package oogasalad;


import java.util.logging.Level;
import javafx.application.Application;
import javafx.stage.Stage;
import oogasalad.engine.controller.DefaultEngineFileConverter;
import oogasalad.engine.controller.DefaultGameController;
import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.GameManagerAPI;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.view.GameAppView;
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
