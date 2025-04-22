package oogasalad.editor.view.panes.spriteCreation;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Skeleton window opened by the “New Sprite” button.
 */
public class CreateSpriteComponent extends Stage {

  /**
   * Constructs a new modal window for creating a sprite component. This window is owned by the
   * provided parent window and blocks interaction with it until closed.
   *
   * @param owner the parent window that owns this modal dialog
   */
  public CreateSpriteComponent(Window owner) {
    initOwner(owner);
    initModality(Modality.APPLICATION_MODAL);
    setTitle("Create New Sprite");

    VBox root = new VBox(10,
        new Label("TODO: build UI for creating a single sprite frame."));
    root.setPadding(new Insets(15));

    setScene(new Scene(root));
    setResizable(false);
  }
}
