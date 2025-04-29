package oogasalad.userData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages user session persistence by storing and retrieving login credentials.
 * This allows users to stay logged in between application sessions.
 * Operates on a session file located under the current working directory.
 *
 * @author Billy McCune
 */
public class SessionManager {
    private static final Logger LOG = LogManager.getLogger();
    private static final String SESSION_FILE_PATH = "data/userData/session.properties";
    private static final String USERNAME_KEY = "last.username";
    private static final String PASSWORD_KEY = "last.password";

    private final Properties sessionProperties;

    /**
     * Initializes the SessionManager and loads any existing session data from
     * the session file under the working directory.
     */
    public SessionManager() {
        this.sessionProperties = new Properties();
        loadSession();
    }

    /**
     * Saves the user's login credentials for future sessions.
     * Creates necessary directories under the working directory.
     *
     * @param username the user's username
     * @param password the user's password
     * @return true if saved successfully, false otherwise
     */
    public boolean saveSession(String username, String password) {
        try {
            File sessionFile = getSessionFile();
            if (!ensureSessionDirectoryExists(sessionFile)) {
                return false;
            }
            
            // Store credentials
            sessionProperties.setProperty(USERNAME_KEY, username);
            sessionProperties.setProperty(PASSWORD_KEY, password);
            
            // Save to file
            return savePropertiesToFile(sessionFile, username);
        } catch (IOException e) {
            LOG.error("Failed to save session", e);
            return false;
        }
    }

    /**
     * Ensures the session directory exists and is a valid directory.
     * 
     * @param sessionFile the session file
     * @return true if the directory exists or was created successfully
     */
    private boolean ensureSessionDirectoryExists(File sessionFile) {
        File parent = sessionFile.getParentFile();
        if (parent == null) {
            return true;
        }
        
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                LOG.error("Failed to create session directory: {}", parent);
                return false;
            }
        } else if (!parent.isDirectory()) {
            LOG.error("Session path parent is not a directory: {}", parent);
            return false;
        }
        return true;
    }
    
    /**
     * Saves the session properties to the specified file.
     * 
     * @param sessionFile the file to save to
     * @param username the username (for logging)
     * @return true if saved successfully
     * @throws IOException if an I/O error occurs
     */
    private boolean savePropertiesToFile(File sessionFile, String username) throws IOException {
        try (FileOutputStream out = new FileOutputStream(sessionFile)) {
            sessionProperties.store(out, "OogaSalad Session Data");
            LOG.info("Session saved for user: {}", username);
            return true;
        }
    }

    /**
     * Clears the stored session data (used for logout).
     * Overwrites the session file under the working directory.
     *
     * @return true if cleared successfully or file did not exist, false on error
     */
    public boolean clearSession() {
        try {
            File sessionFile = getSessionFile();
            if (sessionFile.exists()) {
                sessionProperties.clear();
                try (FileOutputStream out = new FileOutputStream(sessionFile)) {
                    sessionProperties.store(out, "OogaSalad Session Data (cleared)");
                    LOG.info("Session cleared");
                }
            }
            return true;
        } catch (IOException e) {
            LOG.error("Failed to clear session", e);
            return false;
        }
    }

    /**
     * Retrieves the stored username, if any.
     *
     * @return the username or null if none saved
     */
    public String getSavedUsername() {
        return sessionProperties.getProperty(USERNAME_KEY);
    }

    /**
     * Retrieves the stored password, if any.
     *
     * @return the password or null if none saved
     */
    public String getSavedPassword() {
        return sessionProperties.getProperty(PASSWORD_KEY);
    }

    /**
     * Checks if both username and password are saved in the session.
     *
     * @return true if a complete session exists, false otherwise
     */
    public boolean hasActiveSession() {
        return getSavedUsername() != null && getSavedPassword() != null;
    }

    /**
     * Loads session data from the session file under the working directory.
     * Silently logs errors without throwing.
     */
    private void loadSession() {
        File sessionFile = getSessionFile();
        if (sessionFile.exists()) {
            try (FileInputStream in = new FileInputStream(sessionFile)) {
                sessionProperties.load(in);
                LOG.info("Session loaded for user: {}", getSavedUsername());
            } catch (IOException e) {
                LOG.error("Failed to load session", e);
            }
        }
    }

    /**
     * Constructs the File object for the session properties file,
     * resolving relative to the current working directory.
     *
     * @return the session properties File
     */
    private File getSessionFile() {
        String workingDir = System.getProperty("user.dir");
        return new File(workingDir, SESSION_FILE_PATH);
    }
}
