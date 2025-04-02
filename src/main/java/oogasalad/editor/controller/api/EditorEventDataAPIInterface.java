package oogasalad.editor.controller.api;

import java.util.List;
import java.util.UUID;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;

public interface EditorEventDataAPIInterface {
  public void addEvent(UUID id, String eventID);
  public void removeEvent(UUID id, String eventID);

  public void addEventCondition(UUID id, String eventID, ConditionType condition);
  public void addEventOutcome(UUID id, String eventID, OutcomeType outcome);
  public void removeEventCondition(UUID id, String eventID, ConditionType condition);
  public void removeEventOutcome(UUID id, String eventID, OutcomeType outcome);

  public List<ConditionType> getEventConditions(UUID id, String eventID);
  public List<OutcomeType> getEventOutcomes(UUID id, String eventID);
}
