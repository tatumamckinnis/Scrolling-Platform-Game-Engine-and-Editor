package oogasalad;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.view.GameAppView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a test class showing how to use the splash screen.
 */
public class GameSceneApplicationTest extends Application {
  private GameAppView myCurrentView;
  private static final Logger LOG = LogManager.getLogger();

  /**
   * First initialize the view as a GameAppView with the stage passed in. Then call initialize.
   * Finally, set the stage's scene and show it.
   */
  @Override
  public void start(Stage primaryStage) throws ViewInitializationException {
    myCurrentView = new GameAppView(primaryStage);
    myCurrentView.initialize("Splash Screen");
    primaryStage.setScene(myCurrentView.getCurrentScene());
    primaryStage.show();

    LOG.info("Starting GameSceneApplicationTest");
    startGameLoop();
  }

  private void startGameLoop() {
    AnimationTimer gameLoop = new AnimationTimer() {
      @Override
      public void handle(long now) {
        try {
          step();
        } catch (RenderingException e) {
          throw new RuntimeException(e);
        }
      }
    };
    gameLoop.start();
  }

  /**
   * In the step function, call renderGameObjects on the view
   */
  private void step() throws RenderingException {
    myCurrentView.renderGameObjects(null);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
