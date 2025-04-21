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

public class SpriteSheetDataManager {

  private static final SaverStrategy DEFAULT_SAVER_STRATEGY = new XmlStrategy();
  private static final FileParserApi DEFAULT_FILE_PARSER = new DefaultFileParser();
  private static final Logger LOG = LogManager.getLogger(SpriteSheetDataManager.class);

  private final EditorLevelData levelData;
  private final SpriteSheetSaver saver;
  private final SaverStrategy saverStrategy;
  private final SpriteSheetLoader loader;
  private final FileParserApi fileParser;

  public SpriteSheetDataManager(EditorLevelData levelData) {
    this.saver = new SpriteSheetSaver();
    this.loader = new SpriteSheetLoader();
    this.saverStrategy = DEFAULT_SAVER_STRATEGY;
    this.fileParser = DEFAULT_FILE_PARSER;
    this.levelData = levelData;
  }

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
    String atlasName =
        filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;

    String pngFile = Paths.get(sheetURL)
        .getFileName()
        .toString();

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

  public SpriteSheetAtlas loadSpriteSheet(String sheetFile)
      throws SpriteSheetLoadException {
    SpriteSheetAtlas atlas = loader.load(sheetFile, fileParser);
    levelData.getSpriteLibrary().addAtlas(atlas.atlasName(), atlas);
    LOG.info("Loaded sprite sheet {}", sheetFile);
    return atlas;
  }
}
