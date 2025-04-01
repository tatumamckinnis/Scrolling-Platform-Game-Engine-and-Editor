package oogasalad;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.view.GameAppView;

public class GameSceneApplicationTest extends Application {
  GameAppView myCurrentView = new GameAppView();

  @Override
  public void start(Stage primaryStage) throws ViewInitializationException {
    myCurrentView.initialize("Splash Screen");
    Scene scene = myCurrentView.getCurrentScene();
    primaryStage.setScene(scene);
    primaryStage.show();

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

  private void step() throws RenderingException {
    myCurrentView.renderGameObjects(null);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
