package oogasalad.filesaver.xmlcomponents;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteSheetData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class writes the sprite sheet atlas in an XML format given a sprite sheet data
 *
 * @author Jacob You
 */
public class XmlSpriteSheetWriter implements XmlComponentWriter {

  private String imagePath;
  private int sheetWidth;
  private int sheetHeight;
  private List<FrameData> frames;
  File outputFile;

  /**
   * Given sprite sheet data and a file, writes the data in a easily readable format in an XML
   * format.
   *
   * @param spriteSheetData The data for the sprite sheet to save
   * @param outputFile      The file location to save the data
   */
  public XmlSpriteSheetWriter(SpriteSheetData spriteSheetData,
      File outputFile) {
    imagePath = spriteSheetData.imagePath();
    sheetWidth = spriteSheetData.sheetWidth();
    sheetHeight = spriteSheetData.sheetHeight();
    frames = spriteSheetData.frames();
    this.outputFile = outputFile;
  }

  @Override
  public void write() throws IOException {
    try {
      Document doc = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder()
          .newDocument();

      Element root = doc.createElement("spriteFile");
      root.setAttribute("imagePath", imagePath);
      root.setAttribute("width", Integer.toString(sheetWidth));
      root.setAttribute("height", Integer.toString(sheetHeight));
      doc.appendChild(root);

      for (FrameData frame : frames) {
        Element e = doc.createElement("sprite");
        e.setAttribute("name", frame.name());
        e.setAttribute("x", Integer.toString(frame.x()));
        e.setAttribute("y", Integer.toString(frame.y()));
        e.setAttribute("width", Integer.toString(frame.width()));
        e.setAttribute("height", Integer.toString(frame.height()));
        root.appendChild(e);
      }

      Transformer tf = TransformerFactory.newInstance().newTransformer();
      tf.setOutputProperty(OutputKeys.INDENT, "yes");
      tf.transform(new DOMSource(doc), new StreamResult(outputFile));
    } catch (ParserConfigurationException | TransformerException e) {
      throw new IOException(e.getMessage(), e);
    }
  }
}
