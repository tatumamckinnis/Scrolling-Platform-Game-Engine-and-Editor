package oogasalad;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.GameScene;

public class GameSceneApplicationTest extends Application {
  GameScene myCurrentView = new GameScene();

  @Override
  public void start(Stage primaryStage) {

    Group root = new Group();

    root.getChildren().add(myCurrentView);
    myCurrentView.render();

    Scene scene = new Scene(root, 500, 500);
    primaryStage.setScene(scene);
    primaryStage.show();

    startGameLoop();
  }

  private void startGameLoop() {
    AnimationTimer gameLoop = new AnimationTimer() {
      @Override
      public void handle(long now) {
        step();
      }
    };
    gameLoop.start();
  }

  private void step() {
//    myCurrentView.renderGameObjects(null);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
