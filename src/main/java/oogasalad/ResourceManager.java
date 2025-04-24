package oogasalad;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Default implementation of the Resource Mananger API Used to retrieve keys from resource bundles
 * depending on if the key is considered displayed text - meaning it is displayed to the user or if
 * it is part of the configuration
 *
 * @author Alana Zinkin
 */
public class ResourceManager implements ResourceManagerAPI {

  private static final ResourceManagerAPI instance = new ResourceManager();
  private final Map<String, ResourceBundle> bundles = new HashMap<>();
  private Locale currentLocale = Locale.getDefault();

  // Base paths for different resource types
  private static final String I18N_BASE_PATH = "oogasalad.i18n.";
  private static final String CONFIG_BASE_PATH = "oogasalad.config.";

  /**
   * Private constructor to prevent instantiation of this utility class. This class is not meant to
   * be instantiated as it provides only static methods for resource loading.
   */
  private ResourceManager() {
  }

  /**
   * @return the resource manager instance
   */
  public static ResourceManagerAPI getInstance() {
    return instance;
  }

  @Override
  public void setLocale(Locale locale) {
    currentLocale = locale;
    bundles.clear(); // Clear cache when changing locale
  }

  /**
   * Get localized text from the i18n resources
   *
   * @param component The component (editor, engine, exceptions, common)
   * @param key       The resource key
   * @return The localized text
   */
  @Override
  public String getText(String component, String key) {
    String bundleName = I18N_BASE_PATH + component;
    return getResourceBundle(bundleName).getString(key);
  }

  /**
   * Get non-localized configuration value
   *
   * @param configFile The config file name without extension
   * @param key        The configuration key
   * @return The configuration value
   */
  @Override
  public String getConfig(String configFile, String key) {
    String bundleName = CONFIG_BASE_PATH + configFile;
    return getResourceBundle(bundleName).getString(key);
  }

  private ResourceBundle getResourceBundle(String bundleName) {
    if (!bundles.containsKey(bundleName)) {
      // For i18n resources, use locale; for config, use default locale
      if (bundleName.startsWith(I18N_BASE_PATH)) {
        bundles.put(bundleName, ResourceBundle.getBundle(bundleName, currentLocale));
      } else {
        bundles.put(bundleName, ResourceBundle.getBundle(bundleName));
      }
    }
    return bundles.get(bundleName);
  }
}
