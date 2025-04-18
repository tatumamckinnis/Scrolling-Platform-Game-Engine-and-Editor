package oogasalad.editor.view.sprites;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.SpriteSheetLibrary;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;

/**
 * UI that lives inside the “Sprites” tab. Implements the buttons for the spritesheet editor and the
 * sprite creation.
 *
 * @author Jacob You
 */
public class SpriteAssetPane extends BorderPane {

  private static final double BUTTON_SPACING = 5;

  public SpriteAssetPane(EditorController editorController, Window ownerWindow) {
    Button importButton = new Button("Import Sheet");
    Button newSpriteButton = new Button("New Sprite");

    importButton.getStyleClass().add("small-button");
    newSpriteButton.getStyleClass().add("small-button");

    // 1) Import a sprite‑sheet
    importButton.setOnAction(e ->
        new ProcessSpriteSheetComponent(editorController, ownerWindow).show()
    );

    // 2) Create a new SpriteTemplate
    newSpriteButton.setOnAction(e -> {
      // grab your library from the controller
      SpriteSheetLibrary library =
          editorController.getEditorDataAPI().getSpriteLibrary();

      // open the SpriteTemplate dialog
      SpriteTemplateComponent dialog =
          new SpriteTemplateComponent(ownerWindow, library);

      dialog.showAndWait();
      SpriteTemplate result = dialog.getResult();

      if (result != null) {
        editorController.getEditorDataAPI().addSpriteTemplate(result);
      }
    });

    HBox header = new HBox(BUTTON_SPACING, importButton, newSpriteButton);
    header.setPadding(new Insets(6));
    setTop(header);
  }
}
