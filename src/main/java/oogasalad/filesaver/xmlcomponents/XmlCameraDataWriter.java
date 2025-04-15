package oogasalad.filesaver.xmlcomponents;

import java.io.BufferedWriter;
import java.io.IOException;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.LevelData;

/**
 * This class writes the camera data components tags of the XML file.
 *
 * @author Aksel Bell
 */
public class XmlCameraDataWriter {
  private static final String INDENT = "  ";
  private final BufferedWriter writer;
  private final LevelData data;

  /**
   * Instantiates a writer.
   * @param writer writer to write to.
   * @param data level data containing necessary data.
   */
  public XmlCameraDataWriter(BufferedWriter writer, LevelData data) {
    this.writer = writer;
    this.data = data;
  }

  /**
   * @see XmlComponentWriter#write()
   */
  public void write() throws IOException {
    CameraData camera = data.cameraData();
    if (camera == null) {
      return;
    }

    writer.write(String.format(INDENT + "<cameraData type=\"%s\">\n", camera.type()));
    new XmlPropertiesWriter(writer, 2, camera.stringProperties(), camera.doubleProperties()).write();
    writer.write(INDENT + "</cameraData>\n");
  }
}

