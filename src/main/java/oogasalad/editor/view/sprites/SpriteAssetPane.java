package oogasalad.editor.view.sprites;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;

/**
 * UI that lives inside the “Sprites” tab.
 *
 * @author Jacob You
 */
public class SpriteAssetPane extends BorderPane {

  private static final double BUTTON_SPACING = 5;

  public SpriteAssetPane(EditorController editorController, Window ownerWindow) {

    Button importBtn = new Button("Import Sheet");
    Button newBtn = new Button("New Sprite");

    importBtn.getStyleClass().add("small-button");
    newBtn.getStyleClass().add("small-button");

    importBtn.setOnAction(
        e -> new ProcessSpriteSheetComponent(editorController, getScene().getWindow()).show());
    newBtn.setOnAction(e -> new CreateSpriteComponent(ownerWindow).showAndWait());

    HBox header = new HBox(BUTTON_SPACING, importBtn, newBtn);
    header.setPadding(new Insets(6));

    ScrollPane centre = new ScrollPane(
        new Label("No sprites loaded yet – import a sheet to begin."));
    centre.setFitToWidth(true);

    setTop(header);
    setCenter(centre);
  }
}
