package oogasalad.editor.model.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

/**
 * Unit tests for the Layer class.
 * @author Jacob You
 */
class LayerTest {

  private Layer layer;
  private Layer anotherLayer;

  /**
   * Sets up the test environment.
   */
  @BeforeEach
  void setUp() {
    layer = new Layer("Background", 1);
    anotherLayer = new Layer("Foreground", 2);
    anotherLayer.setPriority(10);
  }

  /**
   * Tests setName with a valid name.
   */
  @Test
  void setName_whenValidName_shouldUpdateNameField() {
    layer.setName("Midground");
    assertEquals("Midground", layer.getName());
  }

  /**
   * Tests setPriority with a valid integer.
   */
  @Test
  void setPriority_whenValidInteger_shouldUpdatePriorityField() {
    layer.setPriority(5);
    assertEquals(5, layer.getPriority());
  }
}

