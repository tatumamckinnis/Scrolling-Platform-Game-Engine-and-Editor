package oogasalad.editor.model.data.object.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

/**
 * Comprehensive unit tests for EditorEvent (full coverage).
 * Author: Jacob
 */
class EditorEventTest {

  private EditorEvent emptyEvent;
  private EditorEvent populatedEvent;

  /**
   * Sets up fresh empty and populated EditorEvent instances.
   */
  @BeforeEach
  void setUp() {
    emptyEvent = new EditorEvent();
    emptyEvent.addConditionGroup();

    ExecutorData condExec = new ExecutorData("CondExec", new HashMap<>(), new HashMap<>());
    ExecutorData outExec = new ExecutorData("OutExec", new HashMap<>(), new HashMap<>());
    List<List<ExecutorData>> conds = new ArrayList<>();
    conds.add(new ArrayList<>(List.of(condExec)));
    List<ExecutorData> outs = new ArrayList<>(List.of(outExec));
    populatedEvent = new EditorEvent(conds, outs);
  }

  /**
   * Tests addConditionGroup increases the condition group count.
   */
  @Test
  void addConditionGroup_ValidCall_IncreasesGroupCount() {
    int before = emptyEvent.getConditions().size();
    emptyEvent.addConditionGroup();
    assertEquals(before + 1, emptyEvent.getConditions().size());
  }

  /**
   * Tests addCondition inserts an executor into a valid group.
   */
  @Test
  void addCondition_ValidGroup_AddsExecutor() {
    emptyEvent.addCondition(0, "CollisionExec");
    assertEquals("CollisionExec", emptyEvent.getConditionGroup(0).get(0).getExecutorName());
  }

  /**
   * Tests addOutcome appends an executor.
   */
  @Test
  void addOutcome_ValidCall_AddsExecutor() {
    emptyEvent.addOutcome("ScoreExec");
    assertEquals("ScoreExec", emptyEvent.getOutcomeData(0).getExecutorName());
  }

  /**
   * Tests setOutcomeStringParameter updates a string parameter.
   */
  @Test
  void setOutcomeStringParameter_ExistingParam_UpdatesValue() {
    emptyEvent.addOutcome("ScoreExec");
    emptyEvent.setOutcomeStringParameter(0, "points", "100");
    assertEquals("100", emptyEvent.getOutcomeData(0).getStringParams().get("points"));
  }

  /**
   * Tests setOutcomeDoubleParameter updates a double parameter.
   */
  @Test
  void setOutcomeDoubleParameter_ExistingParam_UpdatesValue() {
    emptyEvent.addOutcome("DamageExec");
    emptyEvent.setOutcomeDoubleParameter(0, "amount", 25.0);
    assertEquals(25.0, emptyEvent.getOutcomeData(0).getDoubleParams().get("amount"));
  }

  /**
   * Tests setConditionStringParameter updates a string parameter.
   */
  @Test
  void setConditionStringParameter_ExistingParam_UpdatesValue() {
    emptyEvent.addCondition(0, "KeyExec");
    emptyEvent.setConditionStringParameter(0, 0, "key", "W");
    assertEquals("W", emptyEvent.getConditionGroup(0).get(0).getStringParams().get("key"));
  }

  /**
   * Tests removeCondition removes an executor.
   */
  @Test
  void removeCondition_ValidIndex_RemovesExecutor() {
    emptyEvent.addCondition(0, "TempExec");
    emptyEvent.removeCondition(0, 0);
    assertTrue(emptyEvent.getConditionGroup(0).isEmpty());
  }

  /**
   * Tests removeConditionGroup deletes a group.
   */
  @Test
  void removeConditionGroup_ValidIndex_RemovesGroup() {
    emptyEvent.addConditionGroup();
    int before = emptyEvent.getConditions().size();
    emptyEvent.removeConditionGroup(1);
    assertEquals(before - 1, emptyEvent.getConditions().size());
  }

  /**
   * Tests removeOutcome deletes an executor.
   */
  @Test
  void removeOutcome_ValidIndex_RemovesExecutor() {
    emptyEvent.addOutcome("TempOut");
    emptyEvent.removeOutcome(0);
    assertTrue(emptyEvent.getOutcomes().isEmpty());
  }

  /**
   * Tests getOutcomeData invalid index returns null.
   */
  @Test
  void getOutcomeData_InvalidIndex_ReturnsNull() {
    assertNull(emptyEvent.getOutcomeData(5));
  }

  /**
   * Tests getConditionData invalid indices return null.
   */
  @Test
  void getConditionData_InvalidIndices_ReturnNull() {
    assertNull(emptyEvent.getConditionData(0, 1));
    assertNull(emptyEvent.getConditionData(9, 0));
  }

  /**
   * Tests addCondition invalid group index throws exception.
   */
  @Test
  void addCondition_InvalidGroup_ThrowsIndexOutOfBounds() {
    assertThrows(IndexOutOfBoundsException.class, () -> emptyEvent.addCondition(5, "BadExec"));
  }

  /**
   * Tests setConditionStringParameter invalid group does nothing.
   */
  @Test
  void setConditionStringParameter_InvalidGroup_NoChange() {
    emptyEvent.setConditionStringParameter(3, 0, "k", "v");
    assertTrue(emptyEvent.getConditionGroup(0).isEmpty());
  }

  /**
   * Tests setOutcomeStringParameter invalid index does nothing.
   */
  @Test
  void setOutcomeStringParameter_InvalidIndex_NoChange() {
    populatedEvent.setOutcomeStringParameter(5, "foo", "bar");
    assertNull(populatedEvent.getOutcomeData(0).getStringParams().get("foo"));
  }

  /**
   * Tests removeConditionGroup invalid index leaves list unchanged.
   */
  @Test
  void removeConditionGroup_InvalidIndex_NoEffect() {
    int before = populatedEvent.getConditions().size();
    populatedEvent.removeConditionGroup(7);
    assertEquals(before, populatedEvent.getConditions().size());
  }

  /**
   * Tests removeOutcome invalid index leaves list unchanged.
   */
  @Test
  void removeOutcome_InvalidIndex_NoEffect() {
    int before = populatedEvent.getOutcomes().size();
    populatedEvent.removeOutcome(9);
    assertEquals(before, populatedEvent.getOutcomes().size());
  }

  /**
   * Tests getters return live lists that reflect external mutation.
   */
  @Test
  void getters_ReturnLiveLists_ReflectMutation() {
    List<ExecutorData> outs = populatedEvent.getOutcomes();
    outs.clear();
    assertTrue(populatedEvent.getOutcomes().isEmpty());
  }
}