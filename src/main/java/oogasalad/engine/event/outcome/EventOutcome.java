package oogasalad.engine.event.outcome;

/**
 * Defines valid event outcomes and associated parameters
 *
 * @author Gage Garcia
 */
public record EventOutcome(EventOutcome.OutcomeType outcomeType) {

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
