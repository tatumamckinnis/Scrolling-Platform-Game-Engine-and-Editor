package oogasalad;

import java.util.Locale;

/**
 * The {@code ResourceManagerAPI} interface defines methods for managing localized text and
 * configuration properties in the application. It supports dynamic locale changes and retrieval of
 * internationalized strings and configuration values from resource bundles.
 *
 * @author Alana Zinkin
 */
public interface ResourceManagerAPI {

  /**
   * Sets the current locale for retrieving localized resources.
   *
   * @param language the {@link Locale} to use for resource bundle lookups
   */
  void setLocale(Locale language);

  /**
   * Retrieves a localized string from an internationalization (i18n) resource bundle.
   *
   * @param component the name of the component (e.g., "editor", "engine", "common") used to
   *                  identify the appropriate bundle
   * @param key       the key for the desired string within the resource bundle
   * @return the localized string associated with the key
   */
  String getText(String component, String key);

  /**
   * Retrieves a configuration value from a non-localized configuration resource bundle.
   *
   * @param configFile the name of the config file (without extension or base path)
   * @param key        the key for the desired configuration value
   * @return the configuration value associated with the key
   */
  String getConfig(String configFile, String key);
}

