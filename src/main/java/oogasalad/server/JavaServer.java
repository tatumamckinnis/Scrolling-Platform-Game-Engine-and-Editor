package oogasalad.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a javascript server instance that can receive connection and messages through websockets.
 *
 * @author Aksel Bell
 */
public class JavaServer {
  private final Process jsServerProcess;
  private static final Logger LOG = LogManager.getLogger();

  /**
   * Starts the javascript server.
   * @throws IOException if error calling javascript class.
   */
  public JavaServer(int port, String gamePath) throws IOException {
    ProcessBuilder builder = new ProcessBuilder("node", "src/main/server/server.js", String.valueOf(port), gamePath);
    jsServerProcess = builder.start();
    logServerOutput();
  }

  /**
   * Method to send server outputs to the log.
   */
  private void logServerOutput() {
    new Thread(() -> {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(jsServerProcess.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOG.info(line);
        }
      } catch (IOException e) {
        LOG.warn("Error: {}", e.getMessage());
      }
    }).start();
  }
}
