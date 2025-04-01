package oogasalad.editor.model.data.object;

import java.util.ArrayList;
import java.util.List;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;

public class EditorEvent {
  private List<ConditionType> conditions;
  private List<OutcomeType> outcomes;

  public EditorEvent(List<ConditionType> conditions, List<OutcomeType> outcomes) {
    this.conditions = conditions;
    this.outcomes = outcomes;
  }

  public EditorEvent() {
    this.conditions = new ArrayList<>();
    this.outcomes = new ArrayList<>();
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
