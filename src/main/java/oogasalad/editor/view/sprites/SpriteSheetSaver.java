package oogasalad.editor.view.sprites;

import java.io.File;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TODO: Put this into the saver function and then into the fileSaver class
 * Serialises a sprite‑sheet cut into an XML atlas file.
 * @author Jacob You
 */
public final class SpriteSheetSaver {

  private SpriteSheetSaver() {
  }

  /**
   * Saves the list of sprite regions to disk.
   *
   * @param sheetImage the original sheet image
   * @param regions    list produced by {@link SpriteSheetProcessorPane}
   * @param atlasFile  where the <spriteFile …/> XML will be written
   */
  public static void save(Image sheetImage,
      List<SpriteRegion> regions,
      File atlasFile) throws Exception {

    Document doc = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .newDocument();

    Element root = doc.createElement("spriteFile");
    root.setAttribute("imagePath",
        sheetImage.getUrl().substring(sheetImage.getUrl().lastIndexOf('/') + 1));
    root.setAttribute("width", Integer.toString((int) sheetImage.getWidth()));
    root.setAttribute("height", Integer.toString((int) sheetImage.getHeight()));
    doc.appendChild(root);

    for (SpriteRegion r : regions) {
      Rectangle2D b = r.getBounds();
      Element e = doc.createElement("sprite");
      e.setAttribute("name", r.getName());
      e.setAttribute("x", Integer.toString((int) b.getMinX()));
      e.setAttribute("y", Integer.toString((int) b.getMinY()));
      e.setAttribute("width", Integer.toString((int) b.getWidth()));
      e.setAttribute("height", Integer.toString((int) b.getHeight()));
      root.appendChild(e);
    }

    Transformer tf = TransformerFactory.newInstance().newTransformer();
    tf.setOutputProperty(OutputKeys.INDENT, "yes");
    tf.transform(new DOMSource(doc), new StreamResult(atlasFile));
  }
}
