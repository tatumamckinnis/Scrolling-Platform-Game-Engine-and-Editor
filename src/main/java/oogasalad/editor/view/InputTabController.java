package oogasalad.editor.view;

import java.util.UUID;
import javafx.scene.layout.Pane;
import oogasalad.editor.controller.EditorDataAPI;
import old_editor_example.DynamicVariableContainer;
import old_editor_example.EditorObject;

/**
 * Controller class for the Input tab in the editor.
 * This class connects the input tab UI with the underlying API and data.
 * It handles selection of game objects and updates the UI accordingly.
 * This implementation supports dynamic variables for parameters.
 *
 * @author Tatum McKinnis
 */
public class InputTabController {
  private final EditorDataAPI editorAPI;
  private final InputTabComponentFactory componentFactory;
  private UUID currentObjectId;
  private EditorObject currentObject;

  /**
   * Create a new input tab controller
   *
   * @param editorAPI The API for accessing editor data
   */
  public InputTabController(EditorDataAPI editorAPI) {
    this.editorAPI = editorAPI;
    this.componentFactory = new InputTabComponentFactory(
        editorAPI.getInputDataAPI(),
        new DynamicVariableContainer() // Default empty container
    );
  }

  /**
   * Get the root pane for the input tab
   *
   * @return The pane containing all input tab components
   */
  public Pane getInputTabPane() {
    return componentFactory.createInputTabPanel();
  }

  public void setCurrentObjectId(UUID objectId) {
    this.currentObject = currentObject;
    componentFactory.setCurrentObjectId(objectId);
  }
}
