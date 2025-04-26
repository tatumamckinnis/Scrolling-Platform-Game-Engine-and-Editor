package oogasalad.filesaver.xmlcomponents;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteData;

public class XmlSpriteWriter implements XmlComponentWriter {

  private static final String ROOT = "data/gameData/gameSpriteSheetData";

  private final String gameName;
  private final SpriteData sprite;
  private String spriteFileName;

  public XmlSpriteWriter(String gameName, SpriteData sprite) {
    this.gameName = Objects.requireNonNull(gameName);
    this.sprite = Objects.requireNonNull(sprite);
  }

  @Override
  public void write() throws IOException {

    this.spriteFileName = sprite.spriteFile().getName()
        .replaceFirst("\\.[^.]+$", "") + ".xml";

    Path outDir = Paths.get(ROOT, gameName, sprite.name().toLowerCase());
    Files.createDirectories(outDir);
    Path outFile = outDir.resolve(spriteFileName);

    try (BufferedWriter bw = Files.newBufferedWriter(outFile)) {

      bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      bw.write(String.format("""
          <!--
            Auto-generated sprite sheet for %s (%s)
          -->
          """, sprite.name(), gameName));

      bw.write(String.format(
          "<spriteFile imagePath=\"%s\">\n",
          sprite.spriteFile().getName()
      ));

      /* <sprite …> tag --------------------------------------------------- */
      bw.write(
          String.format("  <sprite name=\"%s\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\">\n",
              sprite.name(),
              sprite.baseFrame().x(), sprite.baseFrame().y(),
              sprite.baseFrame().width(), sprite.baseFrame().height()
          ));

      /* frames ---------------------------------------------------------- */
      bw.write("    <frames>\n");
      for (FrameData f : sprite.frames()) {
        bw.write(String.format(
            "      <frame name=\"%s\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"/>\n",
            f.name(), f.x(), f.y(), f.width(), f.height()));
      }
      bw.write("    </frames>\n");

      /* animations ------------------------------------------------------ */
      bw.write("    <animations>\n");
      for (AnimationData a : sprite.animations()) {
        bw.write(String.format(
            "      <animation name=\"%s\" frameLen=\"%s\" frames=\"%s\"/>\n",
            a.name(), a.frameLength(),
            String.join(",", a.frameNames())));
      }
      bw.write("    </animations>\n");

      /* close sprite + spriteFile -------------------------------------- */
      bw.write("  </sprite>\n");
      bw.write("</spriteFile>\n");
    }
  }

  public String getSpriteFileName() {
    return spriteFileName;
  }
}
