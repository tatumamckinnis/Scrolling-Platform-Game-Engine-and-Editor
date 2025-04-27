package oogasalad.editor.view.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for loading configuration values from various sources with fallback options.
 * Order of precedence for configuration values:
 * 1. System environment variables
 * 2. Java system properties
 * 3. Config properties file (config.properties)
 * 4. .env file (if available)
 */
public class ConfigLoader {
  private static final Logger LOG = LogManager.getLogger(ConfigLoader.class);
  private static final String CONFIG_FILE = "config.properties";
  private static final String ENV_FILE = ".env";
  private static ConfigLoader instance;
  private final Properties configProperties = new Properties();
  private final Map<String, String> envProperties = new HashMap<>();
  
  /**
   * Private constructor to enforce singleton pattern.
   * Loads configuration from all available sources.
   */
  private ConfigLoader() {
    loadConfigProperties();
    loadEnvProperties();
  }
  
  /**
   * Returns the singleton instance of ConfigLoader.
   *
   * @return the ConfigLoader instance
   */
  public static synchronized ConfigLoader getInstance() {
    if (instance == null) {
      instance = new ConfigLoader();
    }
    return instance;
  }
  
  /**
   * Gets a configuration value by key, checking sources in order of precedence.
   *
   * @param key the configuration key
   * @return the configuration value or null if not found
   */
  public String getProperty(String key) {
    // Check environment variables first
    String value = System.getenv(key);
    if (value != null && !value.isEmpty()) {
      LOG.info("Found {} in environment variables", key);
      return value;
    }
    
    // Check Java system properties
    value = System.getProperty(key);
    if (value != null && !value.isEmpty()) {
      LOG.info("Found {} in system properties", key);
      return value;
    }
    
    // Check config.properties
    value = configProperties.getProperty(key);
    if (value != null && !value.isEmpty()) {
      LOG.info("Found {} in {}", key, CONFIG_FILE);
      return value;
    }
    
    // Finally check .env file
    value = envProperties.get(key);
    if (value != null && !value.isEmpty()) {
      LOG.info("Found {} in {}", key, ENV_FILE);
      return value;
    }
    
    LOG.warn("Could not find property: {} in any configuration source", key);
    return null;
  }
  
  /**
   * Provides debug information about the configuration environment.
   * This should be used for troubleshooting only.
   * 
   * @return A string containing diagnostic information
   */
  public String getDebugInfo() {
    StringBuilder debug = new StringBuilder();
    debug.append("ConfigLoader Debug Information:\n");
    
    // Check if config files exist
    Path configPath = Paths.get(CONFIG_FILE);
    debug.append(CONFIG_FILE + " exists: " + Files.exists(configPath) + "\n");
    
    Path envPath = Paths.get(ENV_FILE);
    debug.append(ENV_FILE + " exists: " + Files.exists(envPath) + "\n");
    
    // Show loaded properties (excluding sensitive values)
    debug.append("\nLoaded properties from " + CONFIG_FILE + ": " + 
                (configProperties.isEmpty() ? "None" : configProperties.size() + " properties") + "\n");
    
    debug.append("Loaded properties from " + ENV_FILE + ": " + 
                (envProperties.isEmpty() ? "None" : envProperties.size() + " properties") + "\n");
    
    // Check available environment variables for our keys
    debug.append("\nAvailable in environment: " + 
                (System.getenv("OPENAI_API_KEY") != null ? "OPENAI_API_KEY" : "No OPENAI_API_KEY") + "\n");
    
    debug.append("Available in system properties: " + 
                (System.getProperty("OPENAI_API_KEY") != null ? "OPENAI_API_KEY" : "No OPENAI_API_KEY") + "\n");
    
    // Working directory info
    debug.append("\nCurrent working directory: " + System.getProperty("user.dir") + "\n");
    
    // Class path info
    debug.append("Classpath resources:\n");
    try {
      InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
      debug.append("  - " + CONFIG_FILE + " in classpath: " + (resourceStream != null) + "\n");
      if (resourceStream != null) {
        resourceStream.close();
      }
    } catch (Exception e) {
      debug.append("  - Error checking classpath: " + e.getMessage() + "\n");
    }
    
    return debug.toString();
  }
  
  /**
   * Loads properties from config.properties file if it exists.
   */
  private void loadConfigProperties() {
    try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
      if (input != null) {
        configProperties.load(input);
        LOG.info("Loaded configuration from {} in classpath", CONFIG_FILE);
      } else {
        // Try to load from file system if not in classpath
        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
          try (FileInputStream fileInput = new FileInputStream(configPath.toFile())) {
            configProperties.load(fileInput);
            LOG.info("Loaded configuration from file system: {}", CONFIG_FILE);
          }
        } else {
          LOG.warn("{} not found in classpath or file system", CONFIG_FILE);
        }
      }
    } catch (IOException e) {
      LOG.warn("Could not load {}", CONFIG_FILE, e);
    }
  }
  
  /**
   * Loads properties from .env file if it exists.
   */
  private void loadEnvProperties() {
    Path path = Paths.get(ENV_FILE);
    if (Files.exists(path)) {
      try {
        for (String line : Files.readAllLines(path)) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("#")) continue;
          int eq = line.indexOf('=');
          if (eq > 0) {
            String key = line.substring(0, eq).trim();
            String val = line.substring(eq+1).trim();
            // Strip optional quotes
            if ((val.startsWith("\"") && val.endsWith("\"")) ||
                (val.startsWith("'") && val.endsWith("'"))) {
              val = val.substring(1, val.length()-1);
            }
            envProperties.put(key, val);
          }
        }
        LOG.info("Loaded environment variables from {}: {} properties", ENV_FILE, envProperties.size());
      } catch (IOException e) {
        LOG.warn("Could not load {}", ENV_FILE, e);
      }
    } else {
      LOG.warn("{} not found", ENV_FILE);
    }
  }
} 