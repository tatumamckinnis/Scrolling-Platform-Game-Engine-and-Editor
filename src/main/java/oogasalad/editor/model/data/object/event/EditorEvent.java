package oogasalad.editor.model.data.object.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents an event associated with an editor object, encapsulating its conditions, outcomes, and
 * corresponding parameter mappings.
 *
 * @author Jacob You
 */
public class EditorEvent {

  private List<List<ExecutorData>> conditions;
  private List<ExecutorData> outcomes;

  /**
   * Constructs an EditorEvent with the specified lists of conditions and outcomes.
   *
   * @param conditions the list of conditions for this event
   * @param outcomes   the list of outcomes for this event
   */
  public EditorEvent(List<List<ExecutorData>> conditions, List<ExecutorData> outcomes) {
    this.conditions = conditions;
    this.outcomes = outcomes;
  }

  /**
   * Constructs an EditorEvent with empty lists for conditions and outcomes.
   */
  public EditorEvent() {
    this.conditions = new ArrayList<>(new ArrayList<>());
    this.outcomes = new ArrayList<>();
  }



  public void addOutcome(ExecutorData outcome) {
    outcomes.add(outcome);
  }

  /**
   * Retrieves the parameter associated with the given outcome.
   *
   * @param index the index for which to retrieve the outcome data
   * @return the {@link ExecutorData}, or null if no parameter exists
   */
  public ExecutorData getOutcomeData(int index) {
    if (index < outcomes.size()) {
      return outcomes.get(index);
    }
    return null;
  }

  /**
   * Retrieves the executor associated with the given condition.
   *
   * @param groupIndex the index of the conditionGroup to get the condition from
   * @param index      the index inside the conditionGroup for which to retrieve the condition data
   * @return the {@link ExecutorData}, or null if no condition exists.
   */
  public ExecutorData getConditionData(int groupIndex, int index) {
    if (groupIndex >= conditions.size()) {
      return null;
    }
    List<ExecutorData> conditionGroup = conditions.get(groupIndex);
    if (conditionGroup != null) {
      if (index < conditionGroup.size()) {
        return conditionGroup.get(index);
      }
    }
    return null;
  }

  /**
   * Sets the string parameter associated with the given name to the specified value for the outcome
   * at the specified index.
   *
   * @param index     the index in the outcome list for the specified outcome
   * @param paramName the name of the parameter to change
   * @param value     the value to change the parameter to
   */
  public void setOutcomeStringParameter(int index, String paramName, String value) {
    ExecutorData outcome = getOutcomeData(index);
    if (outcome != null && paramName != null) {
      outcome.setStringParam(paramName, value);
    }
  }

  /**
   * Sets the double parameter associated with the given name to the specified value for the outcome
   * at the specified index.
   *
   * @param index     the index in the outcome list for the specified outcome
   * @param paramName the name of the parameter to change
   * @param value     the value to change the parameter to
   */
  public void setOutcomeDoubleParameter(int index, String paramName, Double value) {
    ExecutorData outcome = getOutcomeData(index);
    if (outcome != null && paramName != null) {
      outcome.setDoubleParam(paramName, value);
    }
  }

  /**
   * Sets the string parameter associated with the given name to the specified value for the
   * condition at the specified index.
   *
   * @param index     the index in the outcome list for the specified condition
   * @param paramName the name of the parameter to change
   * @param value     the value to change the parameter to
   */
  public void setConditionStringParameter(int groupIndex, int index, String paramName,
      String value) {
    ExecutorData condition = getConditionData(groupIndex, index);
    if (condition != null) {
      condition.setStringParam(paramName, value);
    }
  }

  /**
   * Sets the double parameter associated with the given name to the specified value for the
   * condition at the specified index.
   *
   * @param index     the index in the outcome list for the specified condition
   * @param paramName the name of the parameter to change
   * @param value     the value to change the parameter to
   */
  public void setConditionDoubleParameter(int groupIndex, int index, String paramName,
      Double value) {
    ExecutorData condition = getConditionData(groupIndex, index);
    if (condition != null) {
      condition.setDoubleParam(paramName, value);
    }
  }

  /**
   * Adds a condition of a specified type to the specified group.
   *
   * @param groupIndex    The index of the group to add the event to
   * @param conditionType The name of the conditionType to add
   */
  public void addCondition(int groupIndex, String conditionType) {
    List<ExecutorData> conditionGroup = conditions.get(groupIndex);
    conditionGroup.add(new ExecutorData(conditionType, new HashMap<>(),
        new HashMap<>())); // TODO: Check if name exists, then autogenerate params
  }

  /**
   * Adds a conditionGroup to the event.
   */
  public void addConditionGroup() {
    conditions.add(new ArrayList<>());
  }

  /**
   * Adds an outcome to the event.
   *
   * @param outcomeType The name of the outcomeType to add
   */
  public void addOutcome(String outcomeType) {
    outcomes.add(new ExecutorData(outcomeType, new HashMap<>(),
        new HashMap<>())); // TODO: Check if name exists, then autogenerate params
  }

  /**
   * Removes a specified condition from the specified group.
   *
   * @param groupIndex The group to remove the condition from
   * @param index The index of the condition inside the group to remove
   */
  public void removeCondition(int groupIndex, int index) {
    List<ExecutorData> conditionGroup = conditions.get(groupIndex);
    if (conditionGroup != null) {
      if (conditionGroup.size() > index) {
        conditionGroup.remove(index);
      }
    }
  }

  /**
   * Removes the condition group at the specified index.
   *
   * @param groupIndex the index of the condition group to remove
   */
  public void removeConditionGroup(int groupIndex) {
    if (conditions.size() > groupIndex) {
      conditions.remove(groupIndex);
    }
  }

  /**
   * Removes a specified outcome.
   *
   * @param index The index of the outcome to remove
   */
  public void removeOutcome(int index) {
    if (outcomes.size() > index) {
      outcomes.remove(index);
    }
  }

  /**
   * Returns the list of conditions groups for this event.
   *
   * @return the list of lists of conditions for the event
   */
  public List<List<ExecutorData>> getConditions() {
    return conditions;
  }

  /**
   * Returns the list of conditions inside a condition group for this event.
   *
   * @return the list of conditions in the specified group
   */
  public List<ExecutorData> getConditionGroup(int groupIndex) {
    if (conditions.size() > groupIndex) {
      return conditions.get(groupIndex);
    }
    return null;
  }

  /**
   * Returns the list of outcomes for this event.
   *
   * @return the list of outcomes
   */
  public List<ExecutorData> getOutcomes() {
    return outcomes;
  }

  /**
   * Adds a conditionGroup to the event.
   */
  public void addConditionGroup(List<ExecutorData> group) {
    conditions.add(group);
  }
}
