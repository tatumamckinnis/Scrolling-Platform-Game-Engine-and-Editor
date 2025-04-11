package oogasalad.engine.model.event.outcome;

import java.util.Map;

/**
 * Defines valid event outcomes and associated parameters
 *
 * @author Gage Garcia
 */
public record EventOutcome(EventOutcome.OutcomeType outcomeType,
                           Map<String,String> stringProperties,
                           Map<String,Double> doubleProperties) {

  /**
   * Defines valid outcome types
   */
  public enum OutcomeType {
    MOVE_RIGHT,
    MOVE_LEFT,
    JUMP,
    APPLY_GRAVITY,
    LOSE_GAME,
    PATROL,
    DESTROY_OBJECT,
    PLATFORM_PASS_THROUGH_BEHAVIOR,
    RESTART_LEVEL
  }

  /**
   * Constructor sets outcome type
   *
   * @param outcomeType enum representing type of outcome
   */
  public EventOutcome {
  }

  /**
   * @return outcome type enum
   */
  @Override
  public OutcomeType outcomeType() {
    return outcomeType;
  }
}
