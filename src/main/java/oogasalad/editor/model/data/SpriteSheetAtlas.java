package oogasalad.editor.model.data;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import oogasalad.editor.model.data.object.sprite.FrameData;

/**
 * All data extracted from an atlas XML file.
 *
 * @param atlasName The name of the atlas file
 * @param imagePath The URL of the image path
 * @param width     The width of the image sprite
 * @param height    The height of the image sprite
 * @param frames    The list of {@link FrameData}
 * @author Jacob You
 */
public record SpriteSheetAtlas(String atlasName, String imagePath, String gameName, int width,
                               int height,
                               List<FrameData> frames) {

  /**
   * Absolute path to the atlas XML file.
   */
  public Path getXmlPath() {
    return Paths.get("")                     // current working dir
        .toAbsolutePath()
        .resolve("data")
        .resolve("gameData")
        .resolve(gameName)
        .resolve(atlasName + ".xml");
  }

  /** Absolute path to the sprite-sheet PNG file. */
  public Path getImagePath() {
    return Paths.get("")
        .toAbsolutePath()
        .resolve("data")
        .resolve("graphicsData")
        .resolve(gameName)
        .resolve(imagePath);
  }

  /** Convenience wrappers if you need a File (e.g. for JavaFX Image). */
  public File getXmlFile()   { return getXmlPath().toFile();   }

  /** Convenience wrappers if you need a File (e.g. for JavaFX Image). */
  public File getImageFile() { return getImagePath().toFile(); }
}
