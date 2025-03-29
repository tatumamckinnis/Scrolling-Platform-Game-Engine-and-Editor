# EngineFileAPI

## Overview
Facilitates the translation and sending of data back and forth between the GameFileParser and the engine’s data storage system.

### Design Goals
- **Runtime-Specific Translation**: Focuses on live engine data and transforms it into a standardized model for GameFileParserAPI.
- Takes care of solely bridging between the running game objects and the parser.
- New ways of storing engine data can be added without changing the Engine’s core logic.
- The engine can remain agnostic of how or where the data is obtained and stored in file format; it only calls these methods when needed.
- If you introduce new gameplay systems or multiple save slots, only the EngineFileAPI implementation changes to account for the new data. The engine itself stays the same.

### Developer Usage
- **When Saving**: The engine calls `saveLevelStatus()` to persist the current game progress or state.
- **When Loading**: The engine calls `loadFileToEngine()` to load an entire level or to resume progress.
- **Implementation**: Must handle the transformation from internal engine objects to the parser’s standardized format, then delegate the actual read/write to GameFileParserAPI.

## Classes

```java
package engine.model.file.api;

import java.io.IOException;

public interface EngineFileAPI {

    /**
     * Saves the current game or level status by:
     * 1) Gathering current state from the Engine (objects, progress, scores)
     * 2) Converting them into a parser-compatible data structure
     * 3) Delegating the final write operation to GameFileParserAPI
     *
     * @throws IOException if underlying file operations fail
     * @throws DataFormatException if the data cannot be translated into the parser's model
     */
    void saveLevelStatus() throws IOException, DataFormatException;

    /**
     * Loads a new level or resumes saved progress by:
     * 1) Calling GameFileParserAPI to parse the file into a standardized data structure
     * 2) Translating that structure into the Engine’s runtime objects
     * 3) Updating the current Engine state
     *
     * @throws IOException if the file cannot be read
     * @throws DataFormatException if the file's data cannot be interpreted into Engine objects
     */
    void loadFileToEngine() throws IOException, DataFormatException;
}
```

### Details
- **Saving Mid-Game Progress**: The engine calls `saveLevelStatus()` after pausing or hitting a checkpoint. The implementation extracts relevant data (e.g., positions, scores, power-ups) and constructs the standardized model (like `LevelData` + game state info). Finally, it invokes `GameFileParserAPI.saveLevelToFile(...)`.
- **Loading a New Level**: The engine calls `loadFileToEngine()` with a path to the level file. The parser (GameFileParserAPI) produces a universal data structure. This is translated into engine-specific objects (e.g., `GameObject`, `Camera`, `PhysicsManager`). The engine populates or replaces the current state accordingly.

### Collaborations
- **GameFileParserAPI**: For actual read/write operations.
- **GameController**: Provides the engine’s current game objects and retrieves updated data from the parser.
- Relies on domain classes like `GameObject`, to gather or reconstruct the runtime state.

### Considerations
- `saveLevelStatus()` will likely be expanded into incremental or partial saves in future.
- **Performance**: Large states might require partial or asynchronous I/O.
- **Assumptions**: The engine is responsible for deciding when to call these methods; the interface just ensures a consistent contract.