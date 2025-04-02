package oogasalad;


import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;
import oogasalad.engine.controller.DefaultEngineFileConverter;
import oogasalad.engine.controller.DefaultGameController;
import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.GameManagerAPI;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.view.GameAppView;

/**
 * Feel free to completely change this code or delete it entirely. 
 */
public class Main extends Application {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    @Override
    public void start(Stage primaryStage) {
        try {
            GameManagerAPI manager = new DefaultGameManager(new DefaultGameController());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        launch(args); // Ensures JavaFX starts on the JavaFX Application Thread
    }
}
