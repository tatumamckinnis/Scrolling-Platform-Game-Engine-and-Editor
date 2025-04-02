package oogasalad.editor.model.data.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;

public class EditorEvent {
  private List<ConditionType> conditions;
  private List<OutcomeType> outcomes;
  private Map<OutcomeType, String> outcomeParameters;
  private Map<ConditionType, String> conditionParameters;

  public EditorEvent(List<ConditionType> conditions, List<OutcomeType> outcomes) {
    this.conditions = conditions;
    this.outcomes = outcomes;
    this.outcomeParameters = new HashMap<>();
    this.conditionParameters = new HashMap<>();
  }

  public EditorEvent() {
    this.conditions = new ArrayList<>();
    this.outcomes = new ArrayList<>();
    this.outcomeParameters = new HashMap<>();
    this.conditionParameters = new HashMap<>();
  }
  /**
   * Get the parameter for a specific outcome
   * @param outcome The outcome to get the parameter for
   * @return The parameter value, or null if no parameter exists
   */
  public String getOutcomeParameter(OutcomeType outcome) {
    return outcomeParameters.get(outcome);
  }

  public String getConditionParameter(ConditionType condition) {
    return conditionParameters.get(condition);
  }

  /**
   * Set the parameter for a specific outcome
   * @param outcome The outcome to set the parameter for
   * @param parameter The parameter value
   */
  public void setOutcomeParameter(OutcomeType outcome, String parameter) {
    if (outcomes.contains(outcome)) {
      outcomeParameters.put(outcome, parameter);
    }
  }

  public void setConditionParameter(ConditionType condition, String parameter) {
    if (conditionParameters.containsKey(condition)) {
      conditionParameters.put(condition, parameter);
    }
  }

  public void addCondition(ConditionType condition) {
    conditions.add(condition);
  }

  public void addOutcome(OutcomeType outcome) {
    outcomes.add(outcome);
  }

  public void removeCondition(ConditionType condition) {
    conditions.remove(condition);
  }

  public void removeOutcome(OutcomeType outcome) {
    outcomes.remove(outcome);
  }

  public List<ConditionType> getConditions() {
    return conditions;
  }

  public List<OutcomeType> getOutcomes() {
    return outcomes;
  }


}
