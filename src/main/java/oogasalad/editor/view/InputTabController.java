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

  /**
   * Set the currently selected game object
   *
   * @param objectId The UUID of the selected object
   * @param object The editor object that was selected
   */
  public void setSelectedObject(UUID objectId, EditorObject object) {
    currentObjectId = objectId;
    currentObject = object;

    if (objectId != null) {
      editorAPI.getInputDataAPI().createInputData(objectId);
    }

    if (object != null) {
      componentFactory.setCurrentObject(objectId, object.getDynamicVariables());
    } else {
      componentFactory.setCurrentObject(objectId, new DynamicVariableContainer());
    }
  }

  /**
   * Set the currently selected game object by ID only
   * This is a compatibility method for when we don't have the EditorObject reference
   *
   * @param objectId The UUID of the selected object
   */
  public void setSelectedObject(UUID objectId) {
    currentObjectId = objectId;

    if (objectId != null) {
      editorAPI.getInputDataAPI().createInputData(objectId);
    }

    componentFactory.setCurrentObject(objectId, new DynamicVariableContainer());
  }

  /**
   * Clear the current selection
   */
  public void clearSelection() {
    currentObjectId = null;
    currentObject = null;
    componentFactory.setCurrentObject(null, null);
  }
}
