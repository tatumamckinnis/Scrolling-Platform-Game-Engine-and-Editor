package oogasalad.fileparser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteData;
import oogasalad.fileparser.records.SpriteRequest;

/**
 * Unit tests for the {@link SpriteDataParser} class.
 *
 * <p>
 * These tests simulate the expected file structure based on the following properties:
 * <pre>
 * path.to.game.data=oogasalad_team03/data/gameData/gameObjects
 * path.to.graphics.data=oogasalad_team03/data/graphicsData
 * </pre>
 * The sprite XML file is expected at:
 * <code>{user.dir}/oogasalad_team03/data/gameData/gameObjects/[gameName]/[group]/[shape]/[spriteFile]</code>
 * and the sprite sheet image at:
 * <code>{user.dir}/oogasalad_team03/data/graphicsData/[gameName]/[spriteSheet file]</code>.
 * </p>
 *
 * @author Billy McCune
 */
public class SpriteDataParserTest {

  @TempDir
  Path tempDir;

  private File propertiesDir;
  private File gameDataDir;
  private File graphicsDir;

  @BeforeEach
  void setup() throws Exception {
    // Set the temporary directory as our user.dir.
    System.setProperty("user.dir", tempDir.toAbsolutePath().toString());

    // Create the directory for the properties file at "oogasalad/file/"
    propertiesDir = tempDir.resolve("oogasalad/file").toFile();
    propertiesDir.mkdirs();
    File propFile = new File(propertiesDir, "fileStructure.properties");
    try (FileWriter writer = new FileWriter(propFile)) {
      // Only the keys used by SpriteDataParser are needed.
      writer.write("path.to.game.data=oogasalad_team03/data/gameData/gameObjects\n");
      writer.write("path.to.graphics.data=oogasalad_team03/data/graphicsData\n");
      // The following keys can be present but are not used by SpriteDataParser:
      writer.write("path.to.level.data=/src/data/gameData/levels\n");
      writer.write("path.to.event.registry=/Users/billym./oogasalad/oogasalad_team03/data/gameData/gameObjects/dinosaurgame/blocks/tracks/dinosaurgame-track.xml\n");
    }
    // (Optional) Make sure our tempDir is on the classpath.
    System.setProperty("java.class.path", tempDir.toAbsolutePath().toString());

    // Create directories based on the properties.
    // For game data:
    // Expected path: {user.dir}/oogasalad_team03/data/gameData/gameObjects
    gameDataDir = tempDir.resolve("oogasalad_team03")
        .resolve("data")
        .resolve("gameData")
        .resolve("gameObjects")
        .toFile();
    gameDataDir.mkdirs();

    // For graphics:
    // Expected path: {user.dir}/oogasalad_team03/data/graphicsData
    graphicsDir = tempDir.resolve("oogasalad_team03")
        .resolve("data")
        .resolve("graphicsData")
        .toFile();
    graphicsDir.mkdirs();

    // For this test, we'll use a game called "TestGame" with group "group" and shape "shape".
    String gameName = "TestGame";
    String group = "group";
    String type = "type";
    String spriteFileName = "sprite.xml";

    // Create the sprite XML file under gameDataDir.
    // Full expected path:
    // {user.dir}/oogasalad_team03/data/gameData/gameObjects/TestGame/group/shape/sprite.xml
    File spriteDir = new File(gameDataDir, gameName + File.separator + group + File.separator + type);
    spriteDir.mkdirs();
    File spriteFile = new File(spriteDir, spriteFileName);
    try (FileWriter writer = new FileWriter(spriteFile)) {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<sprites imagePath=\"spritesheet.png\">\n"
          + "  <sprite name=\"hero\" x=\"10\" y=\"20\" width=\"30\" height=\"40\">\n"
          + "    <frames>\n"
          + "      <frame name=\"walk1\" x=\"0\" y=\"0\" width=\"10\" height=\"10\"/>\n"
          + "      <frame name=\"walk2\" x=\"10\" y=\"0\" width=\"10\" height=\"10\"/>\n"
          + "    </frames>\n"
          + "    <animations>\n"
          + "      <animation name=\"walk\" frameLen=\"0.2\" frames=\"walk1,walk2\"/>\n"
          + "    </animations>\n"
          + "  </sprite>\n"
          + "</sprites>\n");
    }

    // Create the sprite sheet file under graphicsDir.
    // Expected path: {user.dir}/oogasalad_team03/data/graphicsData/TestGame/spritesheet.png
    File gameGraphicsDir = new File(graphicsDir, gameName);
    gameGraphicsDir.mkdirs();
    File spriteSheet = new File(gameGraphicsDir, "spritesheet.png");
    spriteSheet.createNewFile();
  }

  @AfterEach
  void tearDown() {
    // @TempDir ensures temporary files are cleaned up automatically.
  }

  /**
   * Tests that a valid sprite XML file is parsed correctly.
   *
   * @throws Exception if an error occurs during parsing.
   */
  @Test
  void getSpriteData_ValidSprite_ReturnsCorrectSpriteData() throws Exception {
    SpriteDataParser parser = new SpriteDataParser();
    SpriteRequest request = new SpriteRequest("TestGame", "group", "type", "hero", "sprite.xml");
    SpriteData spriteData = parser.getSpriteData(request);

    assertNotNull(spriteData, "SpriteData should not be null");
    assertEquals("hero", spriteData.name(), "Sprite name should be 'hero'");

    // Validate base frame (attributes from the sprite element).
    FrameData baseFrame = spriteData.baseImage();
    assertNotNull(baseFrame, "Base frame data should not be null");
    assertEquals(10, baseFrame.x(), "Base frame x attribute");
    assertEquals(20, baseFrame.y(), "Base frame y attribute");
    assertEquals(30, baseFrame.width(), "Base frame width attribute");
    assertEquals(40, baseFrame.height(), "Base frame height attribute");

    // Validate parsed frame list.
    List<FrameData> frames = spriteData.frames();
    assertEquals(2, frames.size(), "Expected 2 frame elements");
    assertEquals("walk1", frames.get(0).name());
    assertEquals("walk2", frames.get(1).name());

    // Validate parsed animation data.
    List<AnimationData> animations = spriteData.animations();
    assertEquals(1, animations.size(), "Expected 1 animation element");
    AnimationData anim = animations.get(0);
    assertEquals("walk", anim.name());
    assertEquals(0.2, anim.frameLen());
    assertIterableEquals(List.of("walk1", "walk2"), anim.frameNames());
  }

  /**
   * Tests that if a sprite with the requested name is not found,
   * a {@link SpriteParseException} is thrown.
   *
   * @throws Exception if an error occurs during parsing.
   */
  @Test
  void getSpriteData_SpriteNotFound_ThrowsSpriteParseException() throws Exception {
    SpriteDataParser parser = new SpriteDataParser();
    // Request a sprite name ("nonexistent") that does not exist in the XML.
    SpriteRequest request = new SpriteRequest("TestGame", "group", "type", "nonexistent", "sprite.xml");
    assertThrows(SpriteParseException.class, () -> parser.getSpriteData(request));
  }

  /**
   * Tests that if the sprite XML file is missing,
   * a {@link SpriteParseException} is thrown.
   *
   * @throws Exception if an error occurs during parsing.
   */
  @Test
  void getSpriteData_FileNotFound_ThrowsSpriteParseException() throws Exception {
    SpriteDataParser parser = new SpriteDataParser();
    // Use a sprite file name that does not exist.
    SpriteRequest request = new SpriteRequest("TestGame", "group", "type", "hero", "nonexistent.xml");
    assertThrows(SpriteParseException.class, () -> parser.getSpriteData(request));
  }
}
