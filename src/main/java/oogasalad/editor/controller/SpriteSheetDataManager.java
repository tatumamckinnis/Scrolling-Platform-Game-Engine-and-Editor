package oogasalad.editor.controller;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Rectangle2D;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.SpriteSheetAtlas;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.saver.SpriteSheetSaver;
import oogasalad.editor.view.sprites.SpriteRegion;
import oogasalad.exceptions.SpriteSheetSaveException;
import oogasalad.filesaver.savestrategy.SaverStrategy;

public class SpriteSheetDataManager {

  private final SpriteSheetSaver saver;
  private final SaverStrategy strategy;
  private final EditorLevelData levelData;

  public SpriteSheetDataManager(EditorLevelData levelData, SpriteSheetSaver spriteSheetSaver,
      SaverStrategy saverStrategy) {
    this.saver = spriteSheetSaver;
    this.strategy = saverStrategy;
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

    SpriteSheetAtlas atlas = new SpriteSheetAtlas(
        atlasName,
        sheetURL,
        sheetWidth,
        sheetHeight,
        frames
    );

    levelData.getSpriteLibrary().addAtlas(atlasName, atlas);

    saver.save(sheetURL, sheetWidth, sheetHeight, frames, outputFile, strategy);
  }
}
