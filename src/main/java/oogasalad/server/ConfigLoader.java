package oogasalad.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class loads in a server URL from the properties file.
 *
 * @author Aksel Bell
 */
public class ConfigLoader {
  private static final Properties props = new Properties();

  static {
    try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("oogasalad/server/Server.properties")) {
      if (input == null) {
        throw new FileNotFoundException("Server.properties not found");
      }
      props.load(input);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load configuration", ex);
    }
  }

  /**
   * Returns the serverURL
   * @param key the key to the server url path in properties
   * @return the link to the server.
   */
  public static String getServerURL(String key) {
    return props.getProperty(key);
  }
}

