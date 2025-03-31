package oogasalad.editor.model.data.object;

import java.util.List;
import java.util.Map;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.EventType;
import oogasalad.editor.model.data.event_enum.OutcomeType;

public record CollisionData(double x, double y, double width, double height, String shape,
                            Map<EventType, Map<List<ConditionType>, List<OutcomeType>>> events) {
}
