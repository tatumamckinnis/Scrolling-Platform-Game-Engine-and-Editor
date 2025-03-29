# GameFileParserAPI (FileParserAPI - Billy)

## Design Goals
- Serve as the single point in the system that knows how to read (parse) and write (save) levels in a standardized file format (e.g., XML).
- Provide a clean interface used by both the Editor (via EditorFileAPI) and Engine (via EngineFileAPI) for consistent file handling.
- Allow new file formats or improved validation logic without changing dependent code.
- Focuses strictly on parsing/saving and not on how the data is used.
- You could introduce different implementations for different files if you want alternate formats.

### Developer Usage
- **Parsing**: Call `parseLevelFile(file)` to turn a file into a format that both the editor and engine file APIs can read.
- **Saving**: Call `saveLevelToFile(levelData, file)` to write out the level data to a file.
- **Validation**: `validateFormat(file)` can be used to check if the file meets the basic required structure (XML tags, schema, etc.) before loading.

## Classes

```java
package parser.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface GameFileParserAPI {

    /**
     * Parses the given file into a LevelData object representation.
     *
     * @param file The file to parse.
     * @return A LevelData instance containing the data from the file.
     * @throws IOException If the file cannot be accessed or read.
     * @throws DataFormatException If the file is not in the expected format
     *                             (e.g., missing tags, invalid values).
     */
    LevelData parseLevelFile(File file) throws IOException, DataFormatException;

    /**
     * Saves the given LevelData object to a file in the standardized format.
     *
     * @param level A LevelData instance containing the data to save.
     * @param file The file to which the data should be written.
     * @throws IOException If the file cannot be written to or created.
     */
    void saveLevelToFile(LevelData level, File file) throws IOException;

    /**
     * Validates the format and structure of the given file without fully
     * parsing it, returning a list of any errors or warnings found.
     *
     * @param file The file to validate.
     * @return A list of validation error messages, or an empty list if valid.
     */
    List<String> validateFormat(File file);
}
```

### Details
- **Editor Saving a New Level**: `EditorFileAPI.saveEditorDataToFile()` calls `GameFileParserAPI.saveLevelToFile()`. It converts the editor’s domain objects into a `LevelData` structure, then the parser writes to XML.
- **Engine Loading a Level**: `EngineFileAPI.loadFileToEngine()` calls `parseLevelFile()`. If successful, it returns a `LevelData` object that the engine translates into runtime objects.
- **Preemptive Format Validation**: Before loading or saving, an API calls `validateFormat(file)` to gather any structural errors. A list of strings can be displayed to the user or logged for debugging.

### Collaboration
- **EditorFileAPI** and **EngineFileAPI** depend on `GameFileParserAPI` for the actual file reading/writing logic.
- EditorManagerAPI or Engine code triggers these calls indirectly through their respective file APIs.
- `LevelData` is the shared in-memory representation that both the editor and engine can interpret.

### Considerations
- **Performance**: Large or complex files could benefit from streaming or partial parsing.
- **Error Handling**: The interface can throw `IOException` and `DataFormatException` for basic error scenarios, but advanced features might require more specialized exceptions. `validateFormat(...)` gives an opportunity to provide user-friendly or developer-friendly error messages rather than failing abruptly.
- **Assumptions**: `LevelData` is assumed to be a well-defined domain class with everything needed to represent a playable level.