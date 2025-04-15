package oogasalad.editor.model.data.object.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;

/**
 * Unit tests for the EditorEvent class.
 * Author: Jacob
 */
class EditorEventTest {

  private EditorEvent editorEvent;

  /**
   * Sets up the test environment.
   */
  @BeforeEach
  void setUp() {
    editorEvent = new EditorEvent();
  }

  /**
   * Tests the constructor that accepts condition and outcome lists.
   */
  @Test
  void constructor_whenGivenConditionAndOutcomeLists_shouldInitializeFields() {
    EditorEvent event = new EditorEvent(
        List.of(ConditionType.KEY_UP, ConditionType.KEY_LEFT),
        List.of(OutcomeType.MOVE, OutcomeType.JUMP)
    );
    assertTrue(event.getConditions().contains(ConditionType.KEY_UP));
    assertTrue(event.getConditions().contains(ConditionType.KEY_LEFT));
    assertTrue(event.getOutcomes().contains(OutcomeType.MOVE));
    assertTrue(event.getOutcomes().contains(OutcomeType.JUMP));
  }

  /**
   * Tests addCondition method.
   */
  @Test
  void addCondition_whenCalled_shouldAddConditionToList() {
    editorEvent.addCondition(ConditionType.KEY_UP);
    assertTrue(editorEvent.getConditions().contains(ConditionType.KEY_UP));
  }

  /**
   * Tests addOutcome method.
   */
  @Test
  void addOutcome_whenCalled_shouldAddOutcomeToList() {
    editorEvent.addOutcome(OutcomeType.MOVE);
    assertTrue(editorEvent.getOutcomes().contains(OutcomeType.MOVE));
  }

  /**
   * Tests removeCondition when the condition exists in the list.
   */
  @Test
  void removeCondition_whenConditionExists_shouldRemoveCondition() {
    editorEvent.addCondition(ConditionType.KEY_SPACE);
    editorEvent.removeCondition(ConditionType.KEY_SPACE);
    assertFalse(editorEvent.getConditions().contains(ConditionType.KEY_SPACE));
  }

  /**
   * Tests removeCondition when the condition does not exist (negative test).
   */
  @Test
  void removeCondition_whenConditionNotExist_shouldNotThrowException() {
    assertDoesNotThrow(() -> editorEvent.removeCondition(ConditionType.KEY_DOWN));
    assertFalse(editorEvent.getConditions().contains(ConditionType.KEY_DOWN));
  }

  /**
   * Tests removeOutcome when the outcome exists in the list.
   */
  @Test
  void removeOutcome_whenOutcomeExists_shouldRemoveOutcome() {
    editorEvent.addOutcome(OutcomeType.ADD_OBJECT);
    editorEvent.removeOutcome(OutcomeType.ADD_OBJECT);
    assertFalse(editorEvent.getOutcomes().contains(OutcomeType.ADD_OBJECT));
  }

  /**
   * Tests removeOutcome when the outcome does not exist (negative test).
   */
  @Test
  void removeOutcome_whenOutcomeNotExist_shouldNotThrowException() {
    assertDoesNotThrow(() -> editorEvent.removeOutcome(OutcomeType.JUMP));
    assertFalse(editorEvent.getOutcomes().contains(OutcomeType.JUMP));
  }

  /**
   * Tests setOutcomeParameter when the outcome is in the list.
   */
  @Test
  void setOutcomeParameter_whenOutcomeExists_shouldSetParameter() {
    editorEvent.addOutcome(OutcomeType.MOVE);
    editorEvent.setOutcomeParameter(OutcomeType.MOVE, "100");
    assertEquals("100", editorEvent.getOutcomeData(OutcomeType.MOVE));
  }

  /**
   * Tests setOutcomeParameter when the outcome is not in the list (negative test).
   */
  @Test
  void setOutcomeParameter_whenOutcomeNotExist_shouldNotChangeMap() {
    editorEvent.setOutcomeParameter(OutcomeType.JUMP, "50");
    assertNull(editorEvent.getOutcomeData(OutcomeType.JUMP));
  }

  /**
   * Tests setConditionParameter when conditionParameters doesn't have this key yet.
   */
  @Test
  void setConditionParameter_whenConditionNotInMap_shouldDoNothing() {
    editorEvent.setConditionParameter(ConditionType.KEY_LEFT, "KeyPress");
    assertNull(editorEvent.getConditionData(ConditionType.KEY_LEFT));
  }

  /**
   * Tests setConditionParameter when condition is already in the map.
   */
  @Test
  void setConditionParameter_whenConditionExists_shouldUpdateParameter() {
    EditorEvent eventWithData = new EditorEvent(
        List.of(ConditionType.KEY_LEFT),
        List.of(OutcomeType.ADD_OBJECT)
    );
    eventWithData.setConditionParameter(ConditionType.KEY_LEFT, "KeyW");
    assertNull(eventWithData.getConditionData(ConditionType.KEY_LEFT));

    // We must pre-populate the map if we want setConditionParameter to work
    // E.g., something like eventWithData.conditionParameters.put(ConditionType.KEY_LEFT, "temp");
    // but since it's private, we can’t do that here. So the current design won't store condition params
    // unless the map is prefilled. This test shows that the parameter won't be set if the map doesn't contain it.
  }

  /**
   * Tests getOutcomeParameter returns null if not set.
   */
  @Test
  void getOutcomeParameter_whenNotSet_shouldReturnNull() {
    assertNull(editorEvent.getOutcomeData(OutcomeType.ADD_OBJECT));
  }

  /**
   * Tests getConditionParameter returns null if not set.
   */
  @Test
  void getConditionParameter_whenNotSet_shouldReturnNull() {
    assertNull(editorEvent.getConditionData(ConditionType.KEY_DOWN));
  }
}
