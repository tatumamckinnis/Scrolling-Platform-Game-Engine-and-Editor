package oogasalad.fileparser.records;

import java.util.List;
import java.util.Map;

/**
 * @author Billy McCune
 */
public record EventData(
    String type,
    String eventId,
    List<List<ConditionData>> conditions,
    List<OutcomeData> outcomes
) {

}
