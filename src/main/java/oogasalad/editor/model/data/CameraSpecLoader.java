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
 */
public final class CameraSpecLoader {

  private static final Logger LOG = LogManager.getLogger(CameraSpecLoader.class);
  private static final String RESOURCE = "/oogasalad/shared/Camera.properties";

  private final Map<String, Specifications> specMap = new HashMap<>();

  public record Specifications(List<String> strParams, List<String> dblParams) {

  }

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

  private List<String> splitList(String csv) {
    return csv.isBlank() ? List.of() :
        Arrays.stream(csv.split("\\s*,\\s*")).filter(s -> !s.isBlank()).toList();
  }

  public Set<String> getCameraTypes() {
    return specMap.keySet();
  }

  public Specifications getSpecifications(String type) {
    return specMap.get(type);
  }
}
