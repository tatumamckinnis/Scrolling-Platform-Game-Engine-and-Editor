package oogasalad;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link ResourceManager} class.
 */
class ResourceManagerTest {

  private ResourceManagerAPI resourceManager;

  @BeforeEach
  void setUp() {
    resourceManager = ResourceManager.getInstance();
  }

  @Test
  void getText_SpriteTextExists_NotNull() {
    // Assumes you have a resource bundle at: resources/oogasalad/i18n/common.properties
    String SpriteNotFound = resourceManager.getText("exceptions", "SpriteNotFound");
    assertNotNull(SpriteNotFound);
    assertFalse(SpriteNotFound.isEmpty());
  }

  @Test
  void getConfig_FramesPerSecondExists_NotNull() {
    String gridColor = resourceManager.getConfig("engine.controller.gamemanager", "framesPerSecond");
    assertNotNull(gridColor);
    assertFalse(gridColor.isEmpty());
  }

  @Test
  void getText_LocaleChangedToFrench_NotNull() {
    resourceManager.setLocale(Locale.FRENCH);
    String frenchWelcome = resourceManager.getText("displayedText", "splash.button.gameType.text");
    assertNotNull(frenchWelcome);
    assertFalse(frenchWelcome.isEmpty());
    assertTrue(frenchWelcome.equalsIgnoreCase("Sélectionner le type de jeu"));
    // You might check for a known French translation if you want
    // assertEquals("Bienvenue", frenchWelcome);
  }

  @Test
  void testGetTextThrowsIfMissingKey() {
    assertThrows(MissingResourceException.class, () -> {
      resourceManager.getText("common", "nonexistent.key");
    });
  }

  @Test
  void testGetConfigThrowsIfMissingKey() {
    assertThrows(MissingResourceException.class, () -> {
      resourceManager.getConfig("editor/view", "nonexistent.key");
    });
  }
}
