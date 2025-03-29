# GameManagerAPI

## Design Goals
- Manages the timeline and orchestrates interactions between the game model and the view.
- Fetches updated game objects from a GameStateManager or model layer, then instructs a GameAppView to render them.
- This interface focuses on orchestrating the game loop and overall time flow of a game.
- Future expansions (e.g., multiple levels, difficulty modes) can be added by extending or implementing new classes without altering the core methods.
- Additional methods (e.g., saveGame(), changeSpeed()) can be easily introduced in specialized implementations.

### Developer Usage
- Use `play()`, `pause()`, or `restartGame()` to control the loop.
- `loadLevel()` typically calls underlying file APIs or engine logic to swap in new game data.
- By calling something like `view.renderUpdatedObjects()` each frame or on demand.
- The manager can call `view.getCurrentInputs()` to read user actions.

## Classes

```java
package engine.manager.api;

import java.util.List;

public interface GameManagerAPI {

    /**
     * Initiates or resumes the game loop, updating the model
     * and rendering the view at regular intervals.
     */
    void play();

    /**
     * Suspends the game loop, freezing updates and rendering.
     */
    void pause();

    /**
     * Restarts the current game from the beginning (or last checkpoint),
     * resetting all necessary model data.
     */
    void restartGame();

    /**
     * Loads a new level or game scene, possibly by calling into
     * file loaders, parsing game data, and updating the current model.
     */
    void loadLevel();

    /**
     * Renders the current state of the game objects in the view,
     * typically by retrieving a list of updated objects from the model
     * and calling render logic on the view.
     */
    void renderUpdatedObjects();

    /**
     * Retrieves the current player inputs, typically by calling
     * methods in the view or input handler.
     *
     * @return a list of strings representing user input actions
     */
    List<String> getCurrentInputs();

    /**
     * Retrieves a list of updated GameObjects from the model,
     * for use in rendering or game logic updates.
     *
     * @return a list of updated game objects
     */
    List<GameObject> getUpdatedObjects();
}
```

### Details
- **User Presses “Play”**: `GameManagerAPI.play()` starts or resumes the main loop.
  - The manager, on each tick, gets the current inputs, then requests updated objects and calls `renderUpdatedObjects()`.
- **Loading a New Level**: `GameManagerAPI.loadLevel()` triggers a new level load, possibly leveraging EngineFileAPI or GameFileParserAPI. Once loaded, the loop can continue from this new state.
- **Pausing the Game**: `pause()` halts the update cycle while maintaining the current state. The user can later call `play()` to pick up where they left off.

### Collaborations
- **GameStateManager** or other engine classes that store/handle game objects and logic.
- **GameAppView** or other view classes for rendering objects.
- **EngineFileAPI** or GameFileParserAPI for loading levels.

### Considerations
- **Threading**: Real-time games often run on a separate thread. The interface does not specify concurrency requirements.
- **Scalability**: For complex multi-level systems, additional methods or interactions might be needed (e.g., LevelAPI).
- **Assumptions**: The GameManagerAPI can assume the model and view are already initialized. The actual creation or injection of references is done externally.