package oogasalad.editor.model.data.object.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;

/**
 * Represents an event associated with an editor object, encapsulating its conditions, outcomes, and
 * corresponding parameter mappings.
 *
 * @author Jacob You
 */
public class EditorEvent {

  private List<ConditionType> conditions;
  private List<OutcomeType> outcomes;
  private Map<OutcomeType, String> outcomeParameters;
  private Map<ConditionType, String> conditionParameters;

  /**
   * Constructs an EditorEvent with the specified lists of conditions and outcomes.
   *
   * @param conditions the list of conditions for this event
   * @param outcomes   the list of outcomes for this event
   */
  public EditorEvent(List<ConditionType> conditions, List<OutcomeType> outcomes) {
    this.conditions = conditions;
    this.outcomes = outcomes;
    this.outcomeParameters = new HashMap<>();
    this.conditionParameters = new HashMap<>();
  }

  /**
   * Constructs an EditorEvent with empty lists for conditions and outcomes.
   */
  public EditorEvent() {
    this.conditions = new ArrayList<>();
    this.outcomes = new ArrayList<>();
    this.outcomeParameters = new HashMap<>();
    this.conditionParameters = new HashMap<>();
  }

  /**
   * Retrieves the parameter associated with the given outcome.
   *
   * @param outcome the outcome for which to retrieve the parameter
   * @return the parameter value, or null if no parameter exists
   */
  public String getOutcomeParameter(OutcomeType outcome) {
    return outcomeParameters.get(outcome);
  }

  /**
   * Retrieves the parameter associated with the given condition.
   *
   * @param condition the condition for which to retrieve the parameter
   * @return the parameter value, or null if no parameter exists
   */
  public String getConditionParameter(ConditionType condition) {
    return conditionParameters.get(condition);
  }

  /**
   * Sets the parameter associated with the specified outcome. This method updates the parameter
   * value only if the outcome is present in the list of outcomes.
   *
   * @param outcome   the outcome for which the parameter should be set
   * @param parameter the new parameter value
   */
  public void setOutcomeParameter(OutcomeType outcome, String parameter) {
    if (outcomes.contains(outcome)) {
      outcomeParameters.put(outcome, parameter);
    }
  }

  /**
   * Sets the parameter associated with the specified condition. The parameter is updated only if
   * the condition already exists in the parameter mapping.
   *
   * @param condition the condition for which the parameter should be set
   * @param parameter the new parameter value
   */
  public void setConditionParameter(ConditionType condition, String parameter) {
    if (conditionParameters.containsKey(condition)) {
      conditionParameters.put(condition, parameter);
    }
  }

  /**
   * Adds a condition to the event.
   *
   * @param condition the condition to add
   */
  public void addCondition(ConditionType condition) {
    conditions.add(condition);
  }

  /**
   * Adds an outcome to the event.
   *
   * @param outcome the outcome to add
   */
  public void addOutcome(OutcomeType outcome) {
    outcomes.add(outcome);
  }

  /**
   * Removes the specified condition from the event.
   *
   * @param condition the condition to remove
   */
  public void removeCondition(ConditionType condition) {
    conditions.remove(condition);
  }

  /**
   * Removes the specified outcome from the event.
   *
   * @param outcome the outcome to remove
   */
  public void removeOutcome(OutcomeType outcome) {
    outcomes.remove(outcome);
  }

  /**
   * Returns the list of conditions for this event.
   *
   * @return a list of ConditionType instances
   */
  public List<ConditionType> getConditions() {
    return conditions;
  }

  /**
   * Returns the list of outcomes for this event.
   *
   * @return a list of OutcomeType instances
   */
  public List<OutcomeType> getOutcomes() {
    return outcomes;
  }
}
