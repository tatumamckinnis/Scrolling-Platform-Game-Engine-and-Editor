/**
 * Use Case: Add a new object to the editor's scene with specific default properties.
 * <p>
 * This use case demonstrates how the EditorManagerAPI communicates with the view and the model. It
 * allows the user to interact with the screen by adding a new object and populating it visually
 * and in memory.
 * <p>
 * API Collaboration:
 * 1) EditorFileAPI is used to access any file operations such as save or load.
 * 2) GameEditorView is used to populate the game object visually.
 * 3) EditorLevel is communicated with to add the user's game object to the list of EditorGameObjects.
 * 4) EditorObjectPropertiesView is displayed when the new object is placed, allowing users to edit the object.
 *
 * @author Aksel Bell
 */

public class EditorAddObjectUseCase {

  private GameEditorView editorView;
  private EditorManagerAPI editorManager;
  private EditorLevel editorLevel;
  private EditorFileAPI fileAPI;

  /**
   * Initialize the editor environment
   */
  public void setup() {
    editorView = new MockGameEditorView();
    editorManager = new MockEditorManager();
    editorLevel = new MockEditorLevel();
    fileAPI = new MockEditorFileAPI();
    createEditorUI();
  }

  /**
   * Create UI components for adding a new object
   */
  private void createEditorUI() {
    Button addObjectButton = new Button("Add Object");
    addObjectButton.setOnAction(e -> addNewObject());
    editorView.addButton(addObjectButton);
  }

  /**
   * Adds a new object with default properties to the editor scene.
   */
  public void addNewObject() {
    EditorGameObject newObject = new EditorGameObject("DefaultType", 100, 100);
    editorLevel.addGameObject(newObject);
    editorView.renderGameObject(newObject);
    displayObjectProperties(newObject);
  }

  /**
   * Display the object properties editor for customization.
   *
   * @param object - the newly added game object
   */
  private void displayObjectProperties(EditorGameObject object) {
    EditorObjectPropertiesView propertiesView = new EditorObjectPropertiesView(object);
    propertiesView.show();
  }

  /**
   * Mock implementation of EditorGameObject
   */
  private class EditorGameObject {
    String type;
    int x, y;

    EditorGameObject(String type, int x, int y) {
      this.type = type;
      this.x = x;
      this.y = y;
    }
  }
}
