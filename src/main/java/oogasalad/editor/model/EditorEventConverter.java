package oogasalad.editor.model;

import java.util.ArrayList;
import java.util.List;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.event.CustomEventData;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.engine.model.event.Event;
import oogasalad.engine.model.event.condition.EventCondition;
import oogasalad.engine.model.event.outcome.EventOutcome;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.OutcomeData;

/**
 * EditorEventConverter takes LevelData EventData and converts it to a EditorData Event object
 *
 * @author Alana Zinkin
 */
public class EditorEventConverter {

  /**
   * converts EventData record into Editor Event object
   *
   * @param gameObjectData GameObjectData to convert
   * @param editorObject   the Editor Object being creating
   * @param blueprintData  the blueprint data for the editor object
   * @return a new CustomEventData object
   */
  public static CustomEventData convertEventData(GameObjectData gameObjectData,
      EditorObject editorObject, BlueprintData blueprintData) {
    List<EventData> eventDataList = blueprintData.eventDataList();
    CustomEventData customEventData = new CustomEventData();

    for (EventData event : eventDataList) {
      if (event == null) {
        continue;
      }
      EditorEvent e = makeEditorEventObject(event, editorObject);
      customEventData.addEvent(event.eventId(), e);
    }
    return customEventData;
  }

  /**
   * Converts a single {@link EventData} record into an {@link Event} object.
   *
   * @param eventData  the raw data for a single event
   * @param gameObject the object that owns the event
   * @return a constructed {@link Event} instance
   */
  private static EditorEvent makeEditorEventObject(EventData eventData, EditorObject gameObject) {
    List<List<ExecutorData>> eventConditions = makeEventConditions(eventData);
    List<ExecutorData> eventOutcomes = makeEventOutcomes(eventData);
    return new EditorEvent(eventConditions, eventOutcomes);
  }

  /**
   * Parses the list of condition strings from an {@link EventData} and converts them into a list of
   * {@link EventCondition} objects.
   *
   * @param eventData the raw event data containing conditions
   * @return a list of parsed {@link EventCondition} instances
   */
  private static List<List<ExecutorData>> makeEventConditions(EventData eventData) {
    List<List<ExecutorData>> eventConditions = new ArrayList<>();
    for (List<ConditionData> eventCondition : eventData.conditions()) {
      List<ExecutorData> conditions = new ArrayList<>();
      for (ConditionData condition : eventCondition) {
        ExecutorData newCondition = new ExecutorData(condition.name(), condition.stringProperties(),
            condition.doubleProperties());
        conditions.add(newCondition);
      }
      eventConditions.add(conditions);
    }
    return eventConditions;
  }

  /**
   * Parses the list of outcome strings from an {@link EventData} and converts them into a list of
   * {@link EventOutcome} objects.
   *
   * @param eventData the raw event data containing outcomes
   * @return a list of parsed {@link EventOutcome} instances
   */
  private static List<ExecutorData> makeEventOutcomes(EventData eventData) {
    List<ExecutorData> eventOutcomes = new ArrayList<>();
    for (OutcomeData outcome : eventData.outcomes()) {
      ExecutorData newOutcome = new ExecutorData(outcome.name(),
          outcome.stringProperties(), outcome.doubleProperties());
      eventOutcomes.add(newOutcome);
    }
    return eventOutcomes;
  }

}
