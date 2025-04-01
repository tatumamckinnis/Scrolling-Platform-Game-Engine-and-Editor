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
  private Map<OutcomeType, String> parameters;

  public EditorEvent(List<ConditionType> conditions, List<OutcomeType> outcomes) {
    this.conditions = conditions;
    this.outcomes = outcomes;
    this.parameters = new HashMap<>();
  }

  public EditorEvent() {
    this.conditions = new ArrayList<>();
    this.outcomes = new ArrayList<>();
  }
  /**
   * Get the parameter for a specific outcome
   * @param outcome The outcome to get the parameter for
   * @return The parameter value, or null if no parameter exists
   */
  public String getParameter(OutcomeType outcome) {
    return parameters.get(outcome);
  }

  /**
   * Set the parameter for a specific outcome
   * @param outcome The outcome to set the parameter for
   * @param parameter The parameter value
   */
  public void setParameter(OutcomeType outcome, String parameter) {
    if (outcomes.contains(outcome)) {
      parameters.put(outcome, parameter);
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
