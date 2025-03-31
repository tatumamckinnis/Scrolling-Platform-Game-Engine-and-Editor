package parser.mock;

import parser.api.GameFileParserAPI;
import parser.api.DataFormatException;
import parser.model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * Use Case 1: Load a level file
 * - This mock implementation simulates reading a level file using helper classes.
 * - `MockLevelFileParser` builds the overall LevelData structure.
 * - `MockSpriteFileParser` provides mock sprite sheet and animation data.
 * - `MockEventFileParser` provides mock event chains.
 * - `validateFormat(...)` ensures the file format is structurally valid before parsing.
 *
 *
 * @author Billy McCune
 */
public class MockGameFileParser implements GameFileParserAPI {

  private final MockSpriteFileParser spriteParser = new MockSpriteFileParser();
  private final MockEventFileParser eventParser = new MockEventFileParser();
  private final MockLevelFileParser levelParser = new MockLevelFileParser();

  @Override
  public LevelData parseLevelFile(File file) throws IOException, DataFormatException {
    // Use Case 1: Load a level file
    // Step 1: Validate the file structure (mocked)
    List<String> validationErrors = validateFormat(file);
    if (!validationErrors.isEmpty()) {
      throw new DataFormatException("File validation failed: " + validationErrors);
    }

    // Step 2: Build the LevelData using mock parsers
    return levelParser.buildMockLevel(spriteParser, eventParser);
  }

  @Override
  public void saveLevelToFile(LevelData level, File file) throws IOException {
    // Simulate save
    levelParser.saveMockLevel(level, file);
  }

  @Override
  public List<String> validateFormat(File file) {
    // Simulate format validation
    return levelParser.validateMockLevel(file);
    return List.of(); // Assume always valid in mock
  }
}

// Usage Example
// --- Mock Support Classes ---

class MockLevelFileParser {
  public LevelData buildMockLevel(MockSpriteFileParser spriteParser, MockEventFileParser eventParser) {
    GameObjectData player = new GameObjectData(
        1, "player", "entities", "Dino", "dinosaur.xml", 100, 200, 1,
        Map.of("velocity.x", "0", "velocity.y", "0"),
        Map.of("W", "jumpDino"),
        Map.of("entities/enemies", "event1"),
        Map.of("jumpVelocity.y", "5")
    );

    EventChainData eventChain = eventParser.mockEventChain();
    SpriteSheetData spriteSheet = spriteParser.mockSpriteSheet();

    return new LevelData("mock-level", List.of(player), List.of(eventChain), List.of(spriteSheet));
  }

  public void saveMockLevel(LevelData level, File file) {
    System.out.println("Saving level to: " + file.getName());
    System.out.println("Objects: " + level.gameObjects().size());
  }

  public List<String> validateMockLevel(File file) {
    return List.of();
  }
}

class MockSpriteFileParser {
  public SpriteSheetData mockSpriteSheet() {
    FrameData frame = new FrameData("DinoStart", 0, 0, 97, 101);
    AnimationData animation = new AnimationData("walk", 0.15, List.of("DinoRun1", "DinoRun2"));
    SpriteData sprite = new SpriteData("Dino", 0, 0, 97, 101, "dino_base.png", List.of(frame), List.of(animation));
    return new SpriteSheetData("dinosaurgame-sprites.png", 1770, 101, List.of(sprite));
  }
}

class MockEventFileParser {
  public EventChainData mockEventChain() {
    EventData jump = new EventData(0, "setVariableToVariable", List.of("velocity.y", "jumpVelocity.y"));
    return new EventChainData("jumpDino", List.of(jump));
  }
}


class MockParserTest {
  public static void main(String[] args) throws Exception {
    GameFileParserAPI parser = new MockGameFileParser();
    File dummyFile = new File("level1.xml");

    // Parse
    LevelData level = parser.parseLevelFile(dummyFile);

    // Save
    parser.saveLevelToFile(level, new File("level1-out.xml"));

    // Validate
    List<String> errors = parser.validateFormat(dummyFile);
    System.out.println("Validation errors: " + errors);
  }
}




package parser.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The GameFileParserAPI defines a consistent interface for reading,
 * validating, and writing level files used by both the Editor and Engine.
 */
public interface GameFileParserAPI {

  /**
   * Parses the given XML file into a LevelData object representation.
   * This includes reading associated sprite files and event chains.
   *
   * @param file The level XML file to parse.
   * @return A populated LevelData instance representing the level.
   * @throws IOException If the file cannot be accessed or read.
   * @throws DataFormatException If the XML format is invalid or incomplete.
   */
  LevelData parseLevelFile(File file) throws IOException, DataFormatException;

  /**
   * Saves the given LevelData to the specified file in XML format.
   * This includes writing referenced sprites and event chains.
   *
   * @param level A LevelData object to serialize.
   * @param file The file to write the data to.
   * @throws IOException If writing to the file fails.
   */
  void saveLevelToFile(LevelData level, File file) throws IOException;

  /**
   * Validates the basic structure of the given file.
   * This may include tag checks, attribute presence, or schema conformity.
   *
   * @param file The XML file to validate.
   * @return A list of validation error messages, or an empty list if valid.
   */
  List<String> validateFormat(File file);
}

// --- Supporting Record/Data Classes ---

package parser.model;

import java.util.List;
import java.util.Map;

public record LevelData(
    String name,
    List<GameObjectData> gameObjects,
    List<EventChainData> eventChains,
    List<SpriteSheetData> spriteSheets
) {}

public record GameObjectData(
    int id,
    String type,
    String group, //entities, blocks, backgrounds
    String spriteName,
    String spriteFile,
    int x,
    int y,
    int layer, //z-layer or draw layer for background/foreground ordering
    Map<String, String> physicsProperties,
    Map<String, String> inputProperties,
    Map<String, String> collisionProperties,
    Map<String, String> variables
) {}


public record EventChainData(
    String id,
    List<EventData> events
) {}

public record EventData(
    int order,
    String eventId,
    List<String> parameters
) {}

public record SpriteSheetData(
    String imagePath,
    int width,
    int height,
    List<SpriteData> sprites
) {}

public record SpriteData(
    String name,
    int x,
    int y,
    int width,
    int height,
    String baseImage, // NEW: path or reference to base sprite image
    List<FrameData> frames,
    List<AnimationData> animations
) {}

public record FrameData(
    String name,
    int x,
    int y,
    int width,
    int height
) {}

public record AnimationData(
    String name,
    double frameLen,
    List<String> frameNames
) {}

// --- Exception Class ---

package parser.api;

// Used to check our own defined data formating exceptions

public class DataFormatException extends Exception {
  public DataFormatException(String message) {
    super(message);
  }
}




