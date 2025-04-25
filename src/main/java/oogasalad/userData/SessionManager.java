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
 */
public class SessionManager {
    private static final Logger LOG = LogManager.getLogger();
    private static final String SESSION_FILE = "data/userData/session.properties";
    private static final String USERNAME_KEY = "last.username";
    private static final String PASSWORD_KEY = "last.password";
    
    private Properties sessionProperties;
    
    /**
     * Initializes the SessionManager and loads any existing session data.
     */
    public SessionManager() {
        sessionProperties = new Properties();
        loadSession();
    }
    
    /**
     * Saves the user's login credentials for future sessions.
     * 
     * @param username The user's username
     * @param password The user's password
     * @return true if saved successfully, false otherwise
     */
    public boolean saveSession(String username, String password) {
        try {
            // Create directory if it doesn't exist
            File sessionFile = new File(SESSION_FILE);
            File parent = sessionFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            // Store credentials
            sessionProperties.setProperty(USERNAME_KEY, username);
            sessionProperties.setProperty(PASSWORD_KEY, password);
            
            // Save to file
            try (FileOutputStream out = new FileOutputStream(sessionFile)) {
                sessionProperties.store(out, "OogaSalad Session Data");
                LOG.info("Session saved for user: " + username);
                return true;
            }
        } catch (IOException e) {
            LOG.error("Failed to save session", e);
            return false;
        }
    }
    
    /**
     * Clears the stored session data (used for logout).
     * 
     * @return true if cleared successfully, false otherwise
     */
    public boolean clearSession() {
        try {
            File sessionFile = new File(SESSION_FILE);
            if (sessionFile.exists()) {
                sessionProperties.clear();
                try (FileOutputStream out = new FileOutputStream(sessionFile)) {
                    sessionProperties.store(out, "OogaSalad Session Data (cleared)");
                    LOG.info("Session cleared");
                    return true;
                }
            }
            return true;
        } catch (IOException e) {
            LOG.error("Failed to clear session", e);
            return false;
        }
    }
    
    /**
     * Gets the stored username if available.
     * 
     * @return the username or null if not found
     */
    public String getSavedUsername() {
        return sessionProperties.getProperty(USERNAME_KEY);
    }
    
    /**
     * Gets the stored password if available.
     * 
     * @return the password or null if not found
     */
    public String getSavedPassword() {
        return sessionProperties.getProperty(PASSWORD_KEY);
    }
    
    /**
     * Checks if there is a saved session.
     * 
     * @return true if a session exists, false otherwise
     */
    public boolean hasActiveSession() {
        return getSavedUsername() != null && getSavedPassword() != null;
    }
    
    /**
     * Loads session data from the properties file.
     */
    private void loadSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            try (FileInputStream in = new FileInputStream(sessionFile)) {
                sessionProperties.load(in);
                LOG.info("Session loaded for user: " + getSavedUsername());
            } catch (IOException e) {
                LOG.error("Failed to load session", e);
            }
        }
    }
} 