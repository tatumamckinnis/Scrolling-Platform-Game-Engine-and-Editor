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
    layer = new Layer("Background");
    anotherLayer = new Layer("Foreground");
    anotherLayer.setPriority(10);
  }

  /**
   * Tests the Layer constructor with a valid name.
   */
  @Test
  void constructor_whenValidName_shouldInitializeFieldsProperly() {
    assertEquals("Background", layer.getName());
    assertNotNull(layer.getInteractingLayers());
    assertTrue(layer.getInteractingLayers().contains(layer));
    assertEquals(0, layer.getPriority());
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

  /**
   * Tests addInteractingLayer with a valid Layer object.
   */
  @Test
  void addInteractingLayer_whenValidLayer_shouldAddToInteractingLayers() {
    layer.addInteractingLayer(anotherLayer);
    assertTrue(layer.getInteractingLayers().contains(anotherLayer));
  }

  /**
   * Tests addInteractingLayer with null.
   */
  @Test
  void addInteractingLayer_whenNull_shouldNotThrowExceptionAndShouldAddNull() {
    layer.addInteractingLayer(null);
    assertTrue(layer.getInteractingLayers().contains(null));
  }

  /**
   * Tests removeInteractingLayer with a Layer that exists.
   */
  @Test
  void removeInteractingLayer_whenLayerExists_shouldRemoveIt() {
    layer.addInteractingLayer(anotherLayer);
    layer.removeInteractingLayer(anotherLayer);
    assertFalse(layer.getInteractingLayers().contains(anotherLayer));
  }

  /**
   * Tests removeInteractingLayer with null.
   */
  @Test
  void removeInteractingLayer_whenNull_shouldNotThrowException() {
    layer.removeInteractingLayer(null);
    assertTrue(layer.getInteractingLayers().contains(layer));
  }

  /**
   * Tests removeInteractingLayer when removing self.
   */
  @Test
  void removeInteractingLayer_whenRemovingSelf_shouldNotRemoveSelf() {
    layer.removeInteractingLayer(layer);
    assertTrue(layer.getInteractingLayers().contains(layer));
  }

  /**
   * Tests getInteractingLayers returns a modifiable list.
   */
  @Test
  void getInteractingLayers_whenCalled_shouldReturnModifiableList() {
    List<Layer> layers = layer.getInteractingLayers();
    layers.add(anotherLayer);
    assertTrue(layers.contains(anotherLayer));
  }
}

