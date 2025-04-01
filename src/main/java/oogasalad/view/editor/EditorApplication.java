package oogasalad.view.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * EditorApplication is an isolated test that demonstrates the functionality of the
 * EditorComponentFactory without other files in the project
 *
 * @author Luke Nam
 */
public class EditorApplication extends Application {
  @Override
  public void start(Stage primaryStage) {
    EditorComponentFactory factory = new EditorComponentFactory();
    Scene editorScene = factory.createEditorScene();
    primaryStage.setScene(editorScene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

