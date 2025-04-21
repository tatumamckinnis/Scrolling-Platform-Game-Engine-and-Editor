package oogasalad.editor.view.panes.spriteCreation;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;

/**
 * Skeleton window opened by the “Import Sheet” button.
 *
 * @author Jacob You
 */
public class ProcessSpriteSheetComponent extends Stage {

  public ProcessSpriteSheetComponent(EditorController editorController, Window owner) {
    initOwner(owner);
    initModality(Modality.APPLICATION_MODAL);
    setTitle("Process Sprite Sheet");

    SpriteSheetProcessorPane content = new SpriteSheetProcessorPane(editorController, owner);

    setScene(new Scene(content, 840, 650));
  }
}
