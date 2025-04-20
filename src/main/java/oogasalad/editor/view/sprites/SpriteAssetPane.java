package oogasalad.editor.view.sprites;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.SpriteSheetLibrary;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * UI that lives inside the “Sprites” tab. Implements the buttons for the spritesheet editor and the
 * sprite creation.
 *
 * @author Jacob You
 */
public class SpriteAssetPane extends BorderPane {

  private static final Logger LOG = LogManager.getLogger(SpriteAssetPane.class);

  private static final double BUTTON_SPACING = 5;

  public SpriteAssetPane(EditorController editorController, Window ownerWindow) {
    Button importButton = new Button("Import Sheet");
    Button newSpriteButton = new Button("New Sprite");

    importButton.getStyleClass().add("small-button");
    newSpriteButton.getStyleClass().add("small-button");

    importButton.setOnAction(e ->
        new ProcessSpriteSheetComponent(editorController, ownerWindow).show()
    );

    newSpriteButton.setOnAction(e -> {
      SpriteSheetLibrary library =
          editorController.getEditorDataAPI().getSpriteLibrary();

      SpriteTemplateComponent dialog =
          new SpriteTemplateComponent(editorController, ownerWindow, library);

      dialog.showAndWait();
      SpriteTemplate result = dialog.getResult();

      if (result != null) {
        editorController.getEditorDataAPI().addSpriteTemplate(result);
        LOG.info("Added new sprite template: {}", result.getName());
      }
    });

    HBox header = new HBox(BUTTON_SPACING, importButton, newSpriteButton);
    header.setPadding(new Insets(6));
    setTop(header);
  }
}
