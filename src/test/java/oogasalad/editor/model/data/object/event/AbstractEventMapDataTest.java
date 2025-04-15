package oogasalad.editor.model.data.object.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AbstractEventMapData.
 *
 * @author Jacob You
 */
class AbstractEventMapDataTest {

  private AbstractEventMapData eventMap;
  private EditorEvent sampleEvent;

  /**
   * Sets up a concrete instance of AbstractEventMapData and a sample event.
   */
  @BeforeEach
  void setUp() {
    eventMap = new AbstractEventMapData() {
    };
    sampleEvent = new EditorEvent();
  }

  /**
   * Tests addEvent with valid inputs.
   */
  @Test
  void addEvent_ValidInputs_ShouldAddEvent() {
    eventMap.addEvent("evt1", sampleEvent);
    assertEquals(sampleEvent, eventMap.getEvent("evt1"));
    assertEquals(1, eventMap.getEvents().size());
  }

  /**
   * Tests addEvent overwrites an existing event with the same ID.
   */
  @Test
  void addEvent_DuplicateId_ShouldOverwrite() {
    EditorEvent first = new EditorEvent();
    EditorEvent second = new EditorEvent();
    eventMap.addEvent("dup", first);
    eventMap.addEvent("dup", second);
    assertEquals(second, eventMap.getEvent("dup"));
    assertEquals(1, eventMap.getEvents().size());
  }

  /**
   * Tests addEvent with null ID.
   */
  @Test
  void addEvent_NullId_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> eventMap.addEvent(null, sampleEvent));
  }

  /**
   * Tests addEvent with empty ID.
   */
  @Test
  void addEvent_EmptyId_ShouldThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> eventMap.addEvent("  ", sampleEvent));
  }

  /**
   * Tests addEvent with null event.
   */
  @Test
  void addEvent_NullEvent_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> eventMap.addEvent("evt2", null));
  }

  /**
   * Tests removeEvent when event exists.
   */
  @Test
  void removeEvent_EventExists_ShouldReturnTrueAndRemove() {
    eventMap.addEvent("evt3", sampleEvent);
    assertTrue(eventMap.removeEvent("evt3"));
    assertNull(eventMap.getEvent("evt3"));
  }

  /**
   * Tests removeEvent when event does not exist.
   */
  @Test
  void removeEvent_EventNotExists_ShouldReturnFalse() {
    assertFalse(eventMap.removeEvent("missing"));
  }

  /**
   * Tests removeEvent with null ID.
   */
  @Test
  void removeEvent_NullId_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> eventMap.removeEvent(null));
  }

  /**
   * Tests getEvent retrieves existing event.
   */
  @Test
  void getEvent_EventExists_ShouldReturnEvent() {
    eventMap.addEvent("evt4", sampleEvent);
    assertEquals(sampleEvent, eventMap.getEvent("evt4"));
  }

  /**
   * Tests getEvent with null ID.
   */
  @Test
  void getEvent_NullId_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> eventMap.getEvent(null));
  }
}

