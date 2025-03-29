# GameControllerAPI

## Overview
The controller of the Game Engine.

### Design Goals
- Orchestrates the overall update cycle by delegating to multiple phase controllers.
- Responds to the live game loop actions (when paused, don’t call) and ensures the game’s state is advanced on each update.
- Implementation details like how the phase controllers are implemented are hidden behind this interface.
- Additional phases can be plugged in without altering the interface.

### Developer Usage
- **Update Cycle**: Call `updateGameState()` periodically (e.g., once per frame) to progress the game logic.
- **Access Updated Objects**: `getUpdatedObjects()` provides a snapshot of game objects that changed during the last update, for rendering or other external processing.
- **Load Level**: Pass in the new level data to remove all old data and parse new data.

## Classes

```java
package engine.api;

public interface GameControllerAPI {

    /**
     * Returns a list of game objects that have been updated or changed
     * since the last call (e.g., positions, states, or visual representation).
     * Often used by the rendering system to determine which objects to draw.
     *
     * @return a List of changed/updated GameObjects
     */
    List<GameObject> getUpdatedObjects();

    /**
     * Advances the game state by one "tick" or step, typically by:
     * 1) Calling each phase controller (input, physics, collision, etc.) 
     * 2) Resolving any post-update tasks (e.g. removing destroyed objects)
     * 3) Tracking which objects have changed for rendering 
     */
    void updateGameState();

    /**
     * Loads a new level or scene, potentially calling file loaders to 
     * retrieve data and re-initializing internal structures (objects, 
     * controllers, etc.).
     */
    void loadLevel(LevelData data);
}
```

### Details
- **Main Game Loop Execution**: A scheduler (or main thread) calls `updateGameState()` on each frame or a fixed interval. Inside, the implementation iterates through phase controllers (input → physics → collision) and modifies objects accordingly.
- **Rendering**: After `updateGameState()` finishes, the view can call `getUpdatedObjects()` to determine which objects moved or changed. The rendering logic can then redraw those objects.
- **loadLevel** could call an API or take in data to retrieve new level data.

### Collaborations
- **GameObject**: The fundamental data structure updated by each phase controller.
- **Phase Controllers**: The engine typically holds references to them internally.
- **EngineFileAPI**: `loadLevel()` might invoke file loading logic.
- **View or Renderer**: Calls `getUpdatedObjects()` to see what changed.

### Considerations
- **Performance**: Large sets of GameObjects might require special structures for collision checks. That’s handled internally, not visible to the API.
- **State Persistence**: `saveGame()` or other persistence methods can be introduced later when needed.
- **Assumptions**: The calling code regularly invokes `updateGameState()`. This API needs to be called for any model side to run.
- The internal logic ensures consistent updates (i.e., objects aren’t half-updated when `getUpdatedObjects()` is called).