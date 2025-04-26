package oogasalad.userData;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for SessionManager, ensuring clean session state across tests.
 * <p>This test class follows the guidelines:
 * <ul>
 *   <li>Each test method is annotated with @Test.</li>
 *   <li>Method names follow the [MethodName_StateUnderTest_ExpectedBehavior] format.</li>
 *   <li>Variable names in tests reflect input and expected state.</li>
 *   <li>The "ZOMBIES" acronym (Zero, One, Many, Boundary, Invalid, Exception, Stress) guides testing scenarios.</li>
 * </ul>
 * A @BeforeEach method resets the session file location under a temporary directory,
 * guaranteeing isolation and preventing stale data from affecting test outcomes.
 */
public class SessionManagerTest {

  @TempDir
  Path tempDir;
  private Path sessionFile;

  @BeforeEach
  void setup() throws IOException {
    // Redirect working directory for relative session file
    System.setProperty("user.dir", tempDir.toString());
    // Determine session properties file path and remove if exists
    sessionFile = tempDir.resolve("data/userData/session.properties");
    Files.deleteIfExists(sessionFile);
  }

  @Test
  void constructor_NoSessionFile_HasNoActiveSession() {
    SessionManager manager = new SessionManager();
    assertFalse(manager.hasActiveSession(), "Expected no active session when no file exists");
    assertNull(manager.getSavedUsername(), "Expected null username when no session saved");
    assertNull(manager.getSavedPassword(), "Expected null password when no session saved");
  }

  @Test
  void saveSession_ValidCredentials_SavesAndLoads() {
    SessionManager manager = new SessionManager();
    boolean saved = manager.saveSession("alice", "secret");
    assertTrue(saved, "Expected saveSession to return true for valid credentials");
    assertTrue(manager.hasActiveSession(), "Expected active session after saving");
    assertEquals("alice", manager.getSavedUsername(), "Saved username mismatch");
    assertEquals("secret", manager.getSavedPassword(), "Saved password mismatch");

    // New instance should load the saved session
    SessionManager reloaded = new SessionManager();
    assertTrue(reloaded.hasActiveSession(), "Expected loaded session to be active");
    assertEquals("alice", reloaded.getSavedUsername(), "Reloaded username mismatch");
    assertEquals("secret", reloaded.getSavedPassword(), "Reloaded password mismatch");
  }

  @Test
  void clearSession_WithActiveSession_ClearsSession() {
    SessionManager manager = new SessionManager();
    manager.saveSession("bob", "pwd");
    assertTrue(manager.hasActiveSession(), "Expected active session before clearing");

    boolean cleared = manager.clearSession();
    assertTrue(cleared, "Expected clearSession to return true");
    assertFalse(manager.hasActiveSession(), "Expected no active session after clearing");
    assertNull(manager.getSavedUsername(), "Expected null username after clearing");
    assertNull(manager.getSavedPassword(), "Expected null password after clearing");
  }

  @Test
  void clearSession_NoSessionFile_ReturnsTrue() {
    SessionManager manager = new SessionManager();
    // No prior saveSession call
    boolean cleared = manager.clearSession();
    assertTrue(cleared, "Expected clearSession to return true when no file existed");
    assertFalse(manager.hasActiveSession(), "Expected no active session after clearSession");
  }

  @Test
  void saveSession_InvalidDirectory_ReturnsFalse() throws IOException {
    // Simulate a file at the directory path to prevent mkdirs
    Path dataDir = tempDir.resolve("data");
    Files.createFile(dataDir);

    SessionManager manager = new SessionManager();
    boolean saved = manager.saveSession("x", "y");
    assertFalse(saved, "Expected saveSession to fail when directory creation blocked");
    assertFalse(manager.hasActiveSession(), "Expected no active session after failed save");
  }

  @Test
  void saveSession_StressMultipleTimes_HandlesAll() {
    SessionManager manager = new SessionManager();
    for (int i = 0; i < 100; i++) {
      String user = "u" + i;
      String pass = "p" + i;
      assertTrue(manager.saveSession(user, pass), "saveSession failed on iteration " + i);
      assertTrue(manager.hasActiveSession(), "hasActiveSession false on iteration " + i);
      assertEquals(user, manager.getSavedUsername(), "Username mismatch on iteration " + i);
      assertEquals(pass, manager.getSavedPassword(), "Password mismatch on iteration " + i);
    }
  }
}
