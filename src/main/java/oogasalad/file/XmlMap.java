package oogasalad.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Creates a mapping of XML names to their file paths. Useful for constructing and traversing a
 * tree-like structure that classifies the specific XML file. For example, cacti.xml is a enemy in
 * entities in the dinosaur game.
 *
 * @author Aksel Bell
 */
public class XmlMap {
  private final Map<String, List<String>> xmlPathMap;

  /**
   * Builds the mapping from XML file names to their paths.
   *
   * @param rootDir The root directory to start searching from (for example, "gameObjects").
   */
  public XmlMap(String rootDir) {
    xmlPathMap = new HashMap<>();
    buildMap(new File(rootDir));
  }

  private void buildMap(File dir) {
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }

    for (File file : Objects.requireNonNull(dir.listFiles())) {
      if (file.isDirectory()) {
        buildMap(file);
      } else if (file.getName().endsWith(".xml")) {
        String fileName = file.getName();

        // ChatGPT helped use the File.separator
        String relativePath = File.separator + fileName;
        xmlPathMap.computeIfAbsent(fileName, k -> new ArrayList<>()).add(relativePath);
      }
    }
  }

  /**
   * Returns the list of paths corresponding to a given XML filename.
   *
   * @param fileName The desire XML filename.
   * @return A list of relative paths to the file within the directory tree.
   */
  public List<String> getPaths(String fileName) {
    return xmlPathMap.getOrDefault(fileName, new ArrayList<>());
  }

  /**
   * Returns the complete mapping from XML filenames to their paths.
   *
   * @return Filenames as keys and lists of file paths as values.
   */
  public Map<String, List<String>> getXmlPathMap() {
    return xmlPathMap;
  }
}
