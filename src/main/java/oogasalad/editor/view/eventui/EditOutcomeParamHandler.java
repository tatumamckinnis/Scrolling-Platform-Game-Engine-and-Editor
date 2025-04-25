package oogasalad.editor.view.eventui;

/**
 * Functional interface for handling the modification of a parameter associated with an existing
 * outcome within the editor view, typically used by {@link OutcomesSectionBuilder}.
 *
 * @author Tatum McKinnis
 */
@FunctionalInterface
public interface EditOutcomeParamHandler {

  /**
   * Handles the request to edit an outcome parameter.
   *
   * @param outcomeIndex The index of the outcome whose parameter is being edited.
   * @param paramName    The name of the parameter being edited.
   * @param value        The new value for the parameter (can be String or Double, implementation
   *                     should handle casting).
   */
  void handle(int outcomeIndex, String paramName, Object value);
}
