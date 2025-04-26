package oogasalad.editor.view.eventui;

/**
 * Functional interface for handling the modification of a parameter associated with an existing
 * condition within the editor view, typically used by {@link ConditionsSectionBuilder}.
 *
 * @author Tatum McKinnis
 */
@FunctionalInterface
public interface EditConditionParamHandler {

  /**
   * Handles the request to edit a condition parameter.
   *
   * @param groupIndex     The index of the condition group containing the condition.
   * @param conditionIndex The index of the condition within the group.
   * @param paramName      The name of the parameter being edited.
   * @param value          The new value for the parameter (can be String or Double, implementation
   *                       should handle casting).
   */
  void handle(int groupIndex, int conditionIndex, String paramName, Object value);
}
