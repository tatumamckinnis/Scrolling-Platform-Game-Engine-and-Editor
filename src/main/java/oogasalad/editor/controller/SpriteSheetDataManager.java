package oogasalad.editor.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Rectangle2D;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.SpriteSheetAtlas;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.loader.SpriteSheetLoader;
import oogasalad.editor.model.saver.SpriteSheetSaver;
import oogasalad.editor.view.panes.spriteCreation.SpriteRegion;
import oogasalad.exceptions.SpriteSheetLoadException;
import oogasalad.exceptions.SpriteSheetSaveException;
import oogasalad.fileparser.DefaultFileParser;
import oogasalad.fileparser.FileParserApi;
import oogasalad.filesaver.savestrategy.SaverStrategy;
import oogasalad.filesaver.savestrategy.XmlStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the loading and saving of sprite sheet atlases within the editor. This class interacts
 * with the editor's level data and sprite library, and delegates persistence logic to saver and
 * loader components using defined strategies.
 *
 * @author Jacob You
 */
public class SpriteSheetDataManager {

  /**
   * Default strategy for saving sprite sheets (e.g., XML format).
   */
  private static final SaverStrategy DEFAULT_SAVER_STRATEGY = new XmlStrategy();

  /**
   * Default file parser used when loading sprite sheet data.
   */
  private static final FileParserApi DEFAULT_FILE_PARSER = new DefaultFileParser();

  /**
   * Logger for internal status and error messages.
   */
  private static final Logger LOG = LogManager.getLogger(SpriteSheetDataManager.class);

  private final EditorLevelData levelData;
  private final SpriteSheetSaver saver;
  private final SaverStrategy saverStrategy;
  private final SpriteSheetLoader loader;
  private final FileParserApi fileParser;

  /**
   * Constructs a new {@code SpriteSheetDataManager} with default saving/loading strategies.
   *
   * @param levelData the editor level data object used to update sprite library contents
   */
  public SpriteSheetDataManager(EditorLevelData levelData) {
    this.saver = new SpriteSheetSaver();
    this.loader = new SpriteSheetLoader();
    this.saverStrategy = DEFAULT_SAVER_STRATEGY;
    this.fileParser = DEFAULT_FILE_PARSER;
    this.levelData = levelData;
  }

  /**
   * Saves a sprite sheet to the specified output file using the provided sprite region data.
   *
   * @param sheetURL    the path to the original sprite sheet image file
   * @param sheetWidth  the width of the full sprite sheet image
   * @param sheetHeight the height of the full sprite sheet image
   * @param regions     the list of {@link SpriteRegion} defining individual sprite bounds
   * @param outputFile  the file to which the sprite sheet atlas should be saved
   * @throws SpriteSheetSaveException if an error occurs during saving
   */
  public void saveSpriteSheet(String sheetURL, int sheetWidth, int sheetHeight,
      List<SpriteRegion> regions, File outputFile)
      throws SpriteSheetSaveException {

    List<FrameData> frames =
        regions.stream().map(r -> {
          Rectangle2D bounds = r.getBounds();
          return new FrameData(
              r.getName(),
              (int) bounds.getMinX(),
              (int) bounds.getMinY(),
              (int) bounds.getWidth(),
              (int) bounds.getHeight()
          );
        }).collect(Collectors.toList());

    String filename = outputFile.getName();
    String atlasName = filename.contains(".")
        ? filename.substring(0, filename.lastIndexOf('.'))
        : filename;

    String pngFile = Paths.get(sheetURL).getFileName().toString();

    SpriteSheetAtlas atlas = new SpriteSheetAtlas(
        atlasName,
        pngFile,
        sheetWidth,
        sheetHeight,
        frames
    );

    levelData.getSpriteLibrary().addAtlas(atlasName, atlas);
    LOG.info("Added sprite sheet {}", atlasName);

    saver.save(pngFile, sheetWidth, sheetHeight, frames, outputFile, saverStrategy);
    LOG.info("Saved {} to {}", atlasName, outputFile.getAbsolutePath());
  }

  /**
   * Loads a sprite sheet atlas from a given file and adds it to the sprite library.
   *
   * @param sheetFile the path to the file containing sprite sheet atlas data
   * @return the loaded {@link SpriteSheetAtlas}
   * @throws SpriteSheetLoadException if an error occurs during loading
   */
  public SpriteSheetAtlas loadSpriteSheet(String sheetFile)
      throws SpriteSheetLoadException {
    SpriteSheetAtlas atlas = loader.load(sheetFile, fileParser);
    levelData.getSpriteLibrary().addAtlas(atlas.atlasName(), atlas);
    LOG.info("Loaded sprite sheet {}", sheetFile);
    return atlas;
  }
}
