package oogasalad.filesaver.xmlcomponents;

import java.io.IOException;

/**
 * This class writes a component of the XML file.
 *
 * @author Aksel Bell
 */
public interface XmlComponentWriter {

  /**
   * One method which writes the specific component. Is implemented differently based on the
   * component.
   *
   * @throws IOException if error writing to file.
   */
  void write() throws IOException;
}