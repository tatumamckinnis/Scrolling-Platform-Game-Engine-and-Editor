# GameAppView

## Overview
Serves as the primary interface between the game engine and the visual representation displayed to the user.

### Design Goals
- **Separation of Concerns**: This API focuses solely on view-related operations, separating the visual representation from game logic.
- **Extensibility**: Allows for different view implementations without requiring changes to the core game engine.
- **Abstraction**: The API abstracts the complexities of rendering, providing a simple interface for the game engine to update the visual state.

```java
package engine.view.api;
 
import engine.model.GameObject;
import java.util.List;
 
/**
 * Represents the primary view interface for the game application.
 * Responsible for rendering game objects and updating the visual state.
 * This interface serves as the boundary between the game engine and the visual representation,
 * allowing different rendering implementations to be used with the same game logic.
 */
public interface GameAppView {
   
    /**
     * Updates the visual representation based on the current game state.
     * This method should be called whenever the game state changes and needs
     * to be reflected visually to the user.
     *
     * Extensibility:
     * - Implementations can define different rendering strategies (2D, 3D, isometric, etc.)
     * - Can be extended to support various visual effects or animations during updates
     * - May implement caching or optimization strategies for better performance
     *
     * @throws RenderingException if there is an error during the rendering process, such as
     *      resource loading failures, graphics context issues, or display errors
     */
    void updateView() throws RenderingException;
   
    /**
     * Initializes the view with the specified parameters.
     * Should be called before any rendering operations.
     *
     * Extensibility:
     * - Implementations can support additional initialization parameters
     * - Can be extended to handle different display configurations (fullscreen, multiple monitors)
     * - May be overridden to load custom resources or configure rendering pipelines
     *
     * @param title the title of the game window
     * @param width the width of the game window
     * @param height the height of the game window
     * @throws ViewInitializationException if the view cannot be initialized due to
     *      missing resources, hardware limitations, or window creation failures
     */
    void initialize(String title, int width, int height) throws ViewInitializationException;
   
    /**
     * Renders a list of game objects to the view.
     * This method is typically called by the game engine after updating game objects.
     *
     * Extensibility:
     * - Can be implemented to support different rendering techniques
     * - Allows for custom sorting or filtering of objects before rendering
     * - Can be extended to implement layer-based rendering or special effects
     *
     * @param gameObjects the list of game objects to render
     * @throws RenderingException if there is an error during the rendering process,
     *      such as invalid sprite resources, rendering context errors, or memory limitations
     */
    void renderGameObjects(List<GameObject> gameObjects) throws RenderingException;
   
    /**
     * Changes the current view mode.
     * Different view modes might include normal gameplay, pause menu, inventory screen, etc.
     *
     * Extensibility:
     * - New view modes can be added to the ViewMode enum without changing this method
     * - Implementations can define custom transitions between view modes
     * - Can be extended to support nested or composite view modes
     *
     * @param viewMode the new view mode
     * @throws IllegalStateException if the requested view mode transition is not allowed
     *      in the current state
     */
    void setViewMode(ViewMode viewMode) throws IllegalStateException;
   
    /**
     * Retrieves the current user inputs.
     * This allows the game engine to respond to user interactions.
     *
     * Extensibility:
     * - Implementations can support various input devices (keyboard, mouse, gamepad)
     * - Can be extended to provide input context or state information
     * - May implement input mapping or configuration options
     *
     * @return a list of inputs currently active
     * @throws InputException if there is an error accessing input devices
     *      or processing input events
     */
    List<String> getCurrentInputs() throws InputException;
   
}
 
/**
 * Exception thrown when there is an error during the rendering process.
 * Provides detailed information about rendering failures.
 */
class RenderingException extends Exception {
    public RenderingException(String message) {
        super(message);
    }
}
 
/**
 * Exception thrown when the view cannot be initialized.
 * Indicates issues with setting up the display environment.
 */
class ViewInitializationException extends Exception {
    public ViewInitializationException(String message) {
        super(message);
    }
}
 
/**
 * Exception thrown when there is an error processing user input.
 * Indicates issues with input devices or event handling.
 */
class InputException extends Exception {
    public InputException(String message) {
        super(message);
    }
}
```

### Details
- **Game rendering**: Primary functionality is to render game objects to the screen. The game engine calls `renderGameObjects()` with the current list of game objects that need to be displayed.
- **User input handling**: The API provides a method to retrieve current user inputs (`getCurrentInputs()`), allowing the game engine to process user interactions.
- **State Changes**: The API supports different view modes through `setViewMode()`, enabling transitions between gameplay, menus, and other screens.

### Collaboration
- **GameControllerAPI**: Receives commands to update the view based on game state changes.
- **InputControllerAPI**: Provides input information that the controller uses to update the game state.
- **GameObjectAPI**: Renders visual representations of game objects.

### Considerations
- **Input Handling**: We debated whether input handling should be a part of the view API or a separate API. We included it here since inputs are typically captured through the view components (keyboard, mouse, etc.), but this could be separated into its own API if needed.
- The API has a direct dependency on the `GameObject` class/interface, creating tight coupling between the view and the game model.