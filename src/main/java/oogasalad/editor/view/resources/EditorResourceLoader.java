package oogasalad.editor.view.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for loading resources like Properties files and ResourceBundles for the editor
 * view. Centralizes resource loading and error handling. (DESIGN-15, DESIGN-14, DESIGN-21)
 *
 * @author Tatum McKinnis
 */
public final class EditorResourceLoader {

  private static final Logger LOG = LogManager.getLogger(EditorResourceLoader.class);
  private static final String DEFAULT_BUNDLE_BASE_PATH = "/oogasalad/config/editor/resources/";

  /**
   * Loads properties from a given classpath resource path. Logs errors if the file is not found or
   * cannot be read.
   *
   * @param resourcePath The absolute path within the classpath (e.g.,
   *                     "/oogasalad/screens/editorScene.properties").
   * @return A Properties object loaded from the file. Returns an empty Properties object on error.
   */
  public static Properties loadProperties(String resourcePath) {
    Properties properties = new Properties();
    if (resourcePath == null || resourcePath.trim().isEmpty()) {
      LOG.error("Resource path cannot be null or empty.");
      return properties;
    }
    try (InputStream stream = EditorResourceLoader.class.getResourceAsStream(resourcePath)) {
      if (stream != null) {
        properties.load(stream);
        LOG.debug("Successfully loaded properties from: {}", resourcePath);
      } else {
        LOG.error("Could not find properties file at classpath resource path: {}", resourcePath);
      }
    } catch (IOException e) {
      LOG.error("Error loading properties from path: {}", resourcePath, e);
    } catch (IllegalArgumentException e) {
      LOG.error("Invalid properties file format or content at path: {}", resourcePath, e);
    }
    return properties;
  }

  /**
   * Loads a ResourceBundle for internationalization using a base name. The base name should
   * correspond to properties files in the classpath, following standard ResourceBundle naming
   * conventions (e.g., "EditorUI" for EditorUI_en.properties). The path is constructed relative to
   * DEFAULT_BUNDLE_BASE_PATH.
   *
   * @param bundleBaseName The base name of the bundle (e.g., "EditorUI").
   * @return The loaded ResourceBundle.
   * @throws MissingResourceException if the bundle cannot be found (indicates configuration
   *                                  error).
   */
  public static ResourceBundle loadResourceBundle(String bundleBaseName) {
    if (bundleBaseName == null || bundleBaseName.trim().isEmpty()) {
      throw new IllegalArgumentException("Bundle base name cannot be null or empty.");
    }
    String bundlePathForLookup =
        DEFAULT_BUNDLE_BASE_PATH.substring(1).replace('/', '.') + bundleBaseName;
    LOG.debug("Attempting to load resource bundle with base name: {}", bundlePathForLookup);
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(bundlePathForLookup);
      LOG.info("Successfully loaded resource bundle: {}", bundleBaseName);
      return bundle;
    } catch (MissingResourceException e) {
      LOG.error("Could not load resource bundle with base name: {}", bundlePathForLookup, e);
      throw e;
    }
  }

  /**
   * Private constructor to prevent instantiation of this utility class. This class is not meant to
   * be instantiated as it provides only static methods for resource loading.
   */
  private EditorResourceLoader() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}