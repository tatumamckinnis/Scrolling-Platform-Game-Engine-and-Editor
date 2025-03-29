/**
 * Use Case: Add a new object to the editor's scene with specific default properties.
 * <p>
 * This use case demonstrates how the EditorManagerAPI communicates with the view and the model. It
 * allows the user to interact with the screen by adding a new object and populating it visiually
 * and in memory.
 * <p>
 * API Collaboration: 1) EditorFileAPI is used to access any file operations such as save or load
 * 2) GameEditorView is used to populate the game object visually 3) EditorLevel is communicated with
 * to add the user's game object to the list of EditorGameObjects 4) EditorObjectPropertiesView is
 * displayed when the new object is placed allowing users to edit the object
 *
 * @author Aksel Bell
 */

