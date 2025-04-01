package oogasalad.editor.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * The EditorComponentFactory creates UI panes for the editor manager to load.
 * @author Luke Nam
 */
public class EditorComponentFactory {
  private static final String editorComponentPropertiesFilepath = "/oogasalad/screens/editorScene.properties";
  private static final Properties editorComponentProperties = new Properties();

  /**
   * Load the EditorComponentFactory given an input stream
   */
  public EditorComponentFactory() {
    try {
      InputStream stream = getClass().getResourceAsStream(editorComponentPropertiesFilepath);
      editorComponentProperties.load(stream);
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Create the editor scene consisting of the interactive map on the left and the
   * properties/input component menu on the right
   * @return menu for the interactive map and editable components
   */
  public Scene createEditorScene() {
    HBox root = new HBox();
    int editorWidth = Integer.parseInt(editorComponentProperties.getProperty("editor.width"));
    int editorHeight = Integer.parseInt(editorComponentProperties.getProperty("editor.height"));

    Pane mapPane = createMapPane(editorHeight);
    Pane componentsPane = createComponentPane(editorHeight);

    root.getChildren().addAll(mapPane, componentsPane);
    return new Scene(root, editorWidth, editorHeight);
  }

  private Pane createMapPane(int splashHeight) {
    Pane mapPane = new Pane();
    int mapPaneWidth = Integer.parseInt(editorComponentProperties.getProperty("editor.map.width"));
    mapPane.setPrefSize(mapPaneWidth, splashHeight);
    mapPane.setStyle("-fx-background-color: lightblue;");
    return mapPane;
  }

  private Pane createComponentPane(int splashHeight) {
    Pane componentPane = new Pane();
    int componentPaneWidth = Integer.parseInt(editorComponentProperties.getProperty("editor.component.width"));
    componentPane.setPrefSize(componentPaneWidth, splashHeight);
    componentPane.setStyle("-fx-background-color: lightgreen;");
    return componentPane;
  }
}

