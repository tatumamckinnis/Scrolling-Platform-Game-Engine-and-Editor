package oogasalad.fileparser.records;

import java.util.List;

/**
 * Represents the event data parsed from an input source.
 * <p>
 * This record encapsulates all relevant information
 * for an event including the event's type,
 * its unique identifier, a list of condition sets
 * (each represented as a list of {@link ConditionData}),
 * and a list of outcomes represented by {@link OutcomeData}.
 * </p>
 *
 * @param type       the type of the event.
 * @param eventId    the unique identifier for the event.
 * @param conditions a list of condition sets,
 * each corresponding to a group of conditions.
 * @param outcomes   the list of outcomes associated with the event.
 *
 * @author Billy
 */
public record EventData(
    String type,
    String eventId,
    List<List<ConditionData>> conditions,
    List<OutcomeData> outcomes
) {

  /**
   * Returns the list of condition groups for this event.
   * This is an alias for the record's conditions component.
   * Each inner list represents a group of conditions to be evaluated together.
   *
   * @return a list of condition groups
   */
  public List<List<ConditionData>> conditionGroups() {
    return conditions;
  }

  /**
   * Returns the list of outcomes associated with this event.
   * This is an alias for the record's outcomes component.
   * These outcomes will be executed when the event's conditions are met.
   *
   * @return a list of outcome data
   */
  public List<OutcomeData> outcomes() {
    return outcomes;
  }
}
