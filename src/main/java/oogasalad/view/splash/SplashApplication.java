package oogasalad.view.splash;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * SplashApplication is a temporary file used to demonstrate that the splash screen works in
 * isolation before integrating it with the rest of the project. The buttons still need to be
 * linked to the backend through the Controller.
 *
 * @author Luke Nam
 */
public class SplashApplication extends Application {
  @Override
  public void start(Stage primaryStage) {
    SplashComponentFactory factory = new SplashComponentFactory();
    Scene splashScene = factory.createSplashScene();
    primaryStage.setScene(splashScene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
