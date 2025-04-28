package oogasalad.filesaver.xmlcomponents;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

/**
 * This class writes the String and Double tag of the XML file.
 *
 * @author Aksel Bell, Billy McCune
 */
public class XmlPropertiesWriter implements XmlComponentWriter{
  private static final String INDENT = "  ";
  private final BufferedWriter writer;
  private final int indentLevel;
  private final Map<String, String> stringProps;
  private final Map<String, Double> doubleProps;
  private final String insideName;
  private final String outsideName;

  /**
   * Instantiates a writer.
   * @param writer writer to write to.
   * @param indentLevel level of base indent.
   * @param stringProps mapping of string properties the tag should contain.
   * @param doubleProps mapping of double properties the tag should contain.
   */
  public XmlPropertiesWriter(BufferedWriter writer, int indentLevel, Map<String, String> stringProps, Map<String, Double> doubleProps, String outSideName, String insideName) {
    this.writer = writer;
    this.indentLevel = indentLevel;
    this.stringProps = stringProps;
    this.doubleProps = doubleProps;
    this.outsideName = outSideName;
    this.insideName = insideName;
  }

  /**
   * @see XmlComponentWriter#write()
   */
  public void write() throws IOException {
    String indent = INDENT.repeat(indentLevel);
    String inner = INDENT.repeat(indentLevel + 1);

    writer.write(indent + String.format("<string%s>\n", outsideName));
    if (stringProps != null) {
      for (var entry : stringProps.entrySet()) {
        writer.write(String.format(inner + "<%s name=\"%s\" value=\"%s\"/>\n", insideName, entry.getKey(), entry.getValue()));
      }
    }
    writer.write(indent + String.format("</string%s>\n", outsideName));

    writer.write(indent + String.format("<double%s>\n", outsideName));
    if (doubleProps != null) {
      for (var entry : doubleProps.entrySet()) {
        writer.write(String.format(inner + "<%s name=\"%s\" value=\"%s\"/>\n", insideName, entry.getKey(), entry.getValue()));
      }
    }
    writer.write(indent + String.format("</double%s>\n", outsideName));
  }
}
