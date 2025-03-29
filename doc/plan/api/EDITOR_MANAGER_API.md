# EditorManagerAPI

## Overview
Game Editor Back-end → Game Editor Front-end

### Design Goals
- Provide methods to add objects, fetch objects by coordinates, retrieve or set properties, etc.
- Focus on managing Editor data structures and logic.
- If you add new property types or object behaviors, you can expand or replace implementations without changing the interface.

### Developer Usage
- **Adding/Modifying Objects**: When the user clicks “Add Object” in the UI, call `addObject(...)`.
- **Retrieving Objects**: When the user clicks on a grid cell, call `getObject(x, y)` to see if something is there.
- **Property Management**: Expose getters/setters for things like name, position, and other object attributes.
- **Delegation**: For saving, it can call `EditorDataSaver.save()` when the save button is pressed.

## Classes

```java
package api.editor.manager;

import EditorObject;

public interface EditorManagerAPI {

    /**
     * Adds a new object to the editor's scene with the specified default properties.
     */
    void addObject(EditorObject editorObject);

    /**
     * Returns the EditorObject at the specified coordinates if one exists,
     * otherwise null or an empty Optional.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the object at (x, y), or null/Optional.empty if none
     */
    EditorObject getObject(double x, double y);

    /**
     * Gets the x-coordinate for the given EditorObject.
     *
     * @param object the EditorObject whose x-coordinate is requested
     * @return the x-coordinate
     */
    double getX(EditorObject object);

    /**
     * Gets the name or label for the given EditorObject.
     *
     * @param object the EditorObject whose name is requested
     * @return the object's name
     */
    String getName(EditorObject object);

    /**
     * Saves the current editor data (e.g., calls EditorFileAPI behind the scenes).
     */
    void save();

    // Additional getters, setters, or property-based methods could be placed here,
    // like setPosition(EditorObject, double x, double y) or setProperty(EditorObject, String propertyName, Object value).
}
```

### Details
- **Adding a New Object to the Scene**: The UI calls `editorManager.addObject(someEditorObject)`. EditorManagerAPI updates the in-memory data structure representing the scene. The front-end can then re-render the updated scene.
- **User Clicks on a Coordinate**: `editorManager.getObject(x, y)` returns the clicked object if one exists. The UI can display its properties in a property pane.
- **Saving**: `editorManager.save()` delegates to the underlying `EditorFileAPI.saveEditorDataToFile()`, which converts the in-memory scene to a standardized format.

### Collaborations
- **EditorFileAPI**: For file operations.
- **Front-End Editor View**: The UI that calls these methods based on user actions.
- In-memory lists or maps of `EditorObject` to track positions and properties.

### Considerations
- **Coordinate Precision**: Since x and y are doubles, we need a tolerance for object detection or a snapping grid.
- **Error Handling**: We need to decide what happens if no object is found at (x, y). Return null or some signal that nothing is there.
- **Assumptions**: The front-end will handle user interactions and pass the correct parameters. The object coordinates or properties are validated before calling these methods.