package oogasalad.editor.model.loader;

import java.util.ArrayList;
import java.util.List;
import oogasalad.editor.model.data.SpriteSheetAtlas;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.exceptions.SpriteSheetLoadException;
import oogasalad.fileparser.FileParserApi;
import oogasalad.fileparser.records.SpriteSheetData;

/**
 * Loads a sprite‑sheet atlas XML file and constructs a {@link SpriteSheetAtlas}
 * containing the image metadata and its frame definitions.
 *
 * @author Jacob You
 */
public class SpriteSheetLoader {

  /**
   * Parses the specified XML file into a {@link SpriteSheetAtlas}.
   *
   * @param sheetFile the XML file defining the sprite sheet
   * @return a populated {@link SpriteSheetAtlas} with its frames
   * @throws Exception if parsing fails or data is invalid
   */
  public SpriteSheetAtlas load(String sheetFile, FileParserApi fileParser)
      throws SpriteSheetLoadException {
    SpriteSheetData recordSheetData = fileParser.parseSpriteSheet(sheetFile);

    int dot = sheetFile.lastIndexOf('.');
    String atlasName = (dot > 0) ? sheetFile.substring(0, dot) : sheetFile;

    return new SpriteSheetAtlas(
        atlasName,
        recordSheetData.imagePath(),
        recordSheetData.sheetWidth(),
        recordSheetData.sheetHeight(),
        convertFrameData(recordSheetData.frames())
    );
  }

  private List<FrameData> convertFrameData(
      List<oogasalad.fileparser.records.FrameData> frameData) {
    List<FrameData> frameDataList = new ArrayList<>();
    for (oogasalad.fileparser.records.FrameData frame : frameData) {
       frameDataList.add(new FrameData(
          frame.name(),
          frame.x(),
          frame.y(),
          frame.width(),
          frame.height()
      ));
    }
    return frameDataList;
  }
}
