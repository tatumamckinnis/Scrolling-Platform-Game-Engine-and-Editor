package oogasalad.editor.model.data;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads src/main/resources/oogasalad/shared/Camera.properties and turns it into an easy-to-query
 * structure.
 *
 * @author Jacob You
 */
public final class CameraSpecLoader {

  private static final Logger LOG = LogManager.getLogger(CameraSpecLoader.class);
  private static final String RESOURCE = "/oogasalad/shared/Camera.properties";

  private final Map<String, Specifications> specMap = new HashMap<>();

  /**
   * Represents the specifications for a camera type, containing lists of expected string and double
   * parameters.
   *
   * @param strParams list of expected string parameter names
   * @param dblParams list of expected double parameter names
   */
  public record Specifications(List<String> strParams, List<String> dblParams) {

  }

  /**
   * Constructs a {@code CameraSpecLoader} and loads camera specifications from the
   * {@code Camera.properties} file.
   *
   * <p>If the properties file cannot be read, an error is logged and the loader remains empty.</p>
   */
  public CameraSpecLoader() {
    Properties p = new Properties();
    try (InputStream is = CameraSpecLoader.class.getResourceAsStream(RESOURCE)) {
      p.load(is);
    } catch (Exception e) {
      LOG.error("Could not read {}", RESOURCE, e);
      return;
    }

    String[] types = p.getProperty("CameraTypes", "").split("\\s*,\\s*");
    for (String t : types) {
      if (t.isBlank()) {
        continue;
      }
      List<String> str = splitList(p.getProperty(t + "StringParameters", ""));
      List<String> dbl = splitList(p.getProperty(t + "DoubleParameters", ""));
      specMap.put(t, new Specifications(str, dbl));
    }
    LOG.info("Loaded camera spec: {}", specMap.keySet());
  }

  /**
   * Splits a comma-separated string into a list of trimmed non-blank strings.
   *
   * @param csv the comma-separated string
   * @return a list of individual non-blank entries
   */
  private List<String> splitList(String csv) {
    return csv.isBlank() ? List.of()
        : Arrays.stream(csv.split("\\s*,\\s*")).filter(s -> !s.isBlank()).toList();
  }

  /**
   * Returns the set of available camera types defined in the specifications.
   *
   * @return a set of camera type names
   */
  public Set<String> getCameraTypes() {
    return specMap.keySet();
  }

  /**
   * Retrieves the {@link Specifications} for a given camera type.
   *
   * @param type the camera type to look up
   * @return the specifications for the given type, or {@code null} if the type is not found
   */
  public Specifications getSpecifications(String type) {
    return specMap.get(type);
  }
}
