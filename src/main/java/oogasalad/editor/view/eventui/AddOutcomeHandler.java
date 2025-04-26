package oogasalad.editor.view.eventui;

/**
 * Functional interface for handling the addition of a new outcome within the editor view, typically
 * used by {@link OutcomesSectionBuilder}.
 *
 * @author Tatum McKinnis
 */
@FunctionalInterface
public interface AddOutcomeHandler {

  /**
   * Handles the request to add an outcome.
   *
   * @param outcomeType The string identifier of the outcome type being added.
   */
  void handle(String outcomeType);
}
