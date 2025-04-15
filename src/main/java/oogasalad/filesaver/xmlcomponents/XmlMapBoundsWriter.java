package oogasalad.filesaver.xmlcomponents;

import java.io.IOException;
import java.io.Writer;
import oogasalad.fileparser.records.LevelData;

/**
 * This class writes the XML component of the map tag.
 *
 * @author Aksel Bell
 */
public class XmlMapBoundsWriter implements XmlComponentWriter {
  private final Writer writer;
  private final LevelData data;

  /**
   * Instantiates a writer.
   * @param writer writer to write to.
   * @param data level data containing necessary data.
   */
  public XmlMapBoundsWriter(Writer writer, LevelData data) {
    this.writer = writer;
    this.data = data;
  }

  /**
   * @see XmlComponentWriter#write()
   */
  public void write() throws IOException {
    writer.write(String.format(
        "<map minX=\"%d\" minY=\"%d\" maxX=\"%d\" maxY=\"%d\">\n",
        data.minX(), data.minY(), data.maxX(), data.maxY()
    ));
  }
}

