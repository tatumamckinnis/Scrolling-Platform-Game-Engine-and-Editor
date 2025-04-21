package oogasalad.editor.view.tools;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.view.EditorGameView;

/**
 * Tool for clearing all the objects from the editor view
 */
public class ClearAllTool implements OnClickTool {

  private final EditorGameView editorView;
  private final EditorController editorController;

  /**
   * constructor for making a tool that clears all the objects from the editor view
   *
   * @param editorView       the EditorGameView instance; must not be null.
   * @param editorController the controller to use for selection notifications; must not be null.
   */
  public ClearAllTool(EditorGameView editorView, EditorController editorController) {
    this.editorView = Objects.requireNonNull(editorView, "EditorGameView cannot be null.");
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");
  }

  @Override
  public void execute() {
    Map<UUID, EditorObject> editorObjects = editorController.getEditorDataAPI().getObjectDataMap();
    List<UUID> objectUUIDs = editorObjects.keySet().stream().toList();

    for (UUID uuid : objectUUIDs) {
      if (uuid != null) {
        editorController.requestObjectRemoval(uuid);
      }
    }
  }
}
