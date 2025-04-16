package oogasalad.editor.view.sprites;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Skeleton window opened by the “Import Sheet” button. */
public class ProcessSpriteSheetComponent extends Stage {

  public ProcessSpriteSheetComponent(Window owner) {
    initOwner(owner);
    initModality(Modality.APPLICATION_MODAL);
    setTitle("Process Sprite Sheet");

    VBox root = new VBox(10,
        new Label("TODO: build UI for selecting & slicing a sprite sheet."));
    root.setPadding(new Insets(15));

    setScene(new Scene(root));
    setResizable(false);
  }
}
